package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.exception.IdempotenciaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Idempotência dos POSTs com o cabeçalho X-Idempotency-Key.
 *
 * Problema: o cliente envia um POST, a conexão cai antes da resposta e ele não sabe se o registro
 * foi criado. Se reenviar, pode criar o registro duas vezes. Com a chave de idempotência, o
 * reenvio é seguro:
 *
 *  1. primeira requisição com a chave: é processada e, se der certo (2xx), a resposta é guardada;
 *  2. mesma requisição de novo (mesmo cliente, chave, endereço e corpo): a API devolve a resposta
 *     guardada, sem processar outra vez, com o cabeçalho X-Idempotency-Replayed: true;
 *  3. mesma chave com outro corpo ou endereço: HTTP 422;
 *  4. mesma chave enquanto a primeira ainda está em processamento: HTTP 409.
 *
 * Respostas de erro não são guardadas: o cliente pode corrigir os dados e reenviar com a mesma chave.
 * O cabeçalho é opcional. Ficam de fora o login e a geração de chave de API, cujas respostas
 * não devem ficar guardadas (a segunda traz a chave de API em texto).
 */
@Component
@Order(OrdemDosFiltros.IDEMPOTENCIA)
public class IdempotenciaFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Idempotency-Key";
    public static final String CABECALHO_REPETIDA = "X-Idempotency-Replayed";

    static final int TAMANHO_MAXIMO_DA_CHAVE = 100;
    private static final Pattern FORMATO_DA_CHAVE = Pattern.compile("[A-Za-z0-9._:-]{1," + TAMANHO_MAXIMO_DA_CHAVE + "}");

    /** POSTs que não usam idempotência. */
    static final List<String> ROTAS_EXCLUIDAS = List.of("/auth/login", "/chaves-api");

    private final RegistrosDeIdempotencia registros;
    private final ClienteDaRequisicao cliente;
    private final HandlerExceptionResolver resolvedorDeErros;

    public IdempotenciaFilter(
        RegistrosDeIdempotencia registros,
        ClienteDaRequisicao cliente,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolvedorDeErros
    ) {
        this.registros = registros;
        this.cliente = cliente;
        this.resolvedorDeErros = resolvedorDeErros;
    }

    /** Diz se um POST neste caminho aceita X-Idempotency-Key (usado também pelo Swagger). */
    public static boolean aceitaIdempotencia(String metodo, String caminho) {
        return "POST".equals(metodo) && RegrasDeAcesso.ehRotaDaApi(caminho) && !ROTAS_EXCLUIDAS.contains(caminho);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getHeader(CABECALHO) == null
            || !aceitaIdempotencia(request.getMethod(), ChaveApiFilter.caminho(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadeia)
        throws ServletException, IOException {
        String chave = request.getHeader(CABECALHO).trim();
        if (!FORMATO_DA_CHAVE.matcher(chave).matches()) {
            resolvedorDeErros.resolveException(request, response, null,
                IdempotenciaException.chaveInvalida(TAMANHO_MAXIMO_DA_CHAVE));
            return;
        }

        // O corpo é lido aqui, para compor a impressão da requisição, e entregue de novo ao controller.
        CorpoRelido requisicao = new CorpoRelido(request);
        String escopo = cliente.identificador(request) + "|" + chave;
        RegistrosDeIdempotencia.Resultado resultado = registros.iniciar(escopo, impressao(requisicao));

        switch (resultado.situacao()) {
            case NOVA -> processar(requisicao, response, cadeia, escopo);
            case REPETIDA -> repetir(resultado.resposta(), response);
            case EM_ANDAMENTO -> resolvedorDeErros.resolveException(request, response, null, IdempotenciaException.emAndamento());
            case REUTILIZADA -> resolvedorDeErros.resolveException(request, response, null, IdempotenciaException.reutilizada());
        }
    }

    private void processar(CorpoRelido request, HttpServletResponse response, FilterChain cadeia, String escopo)
        throws ServletException, IOException {
        ContentCachingResponseWrapper resposta = new ContentCachingResponseWrapper(response);
        boolean guardada = false;
        try {
            cadeia.doFilter(request, resposta);

            int status = resposta.getStatus();
            if (status >= 200 && status < 300) {
                registros.concluir(escopo, new RegistrosDeIdempotencia.RespostaGuardada(
                    status, resposta.getContentType(), resposta.getHeader(HttpHeaders.LOCATION), resposta.getContentAsByteArray()));
                guardada = true;
            }
        } finally {
            if (!guardada) {
                registros.cancelar(escopo);
            }
            resposta.copyBodyToResponse();
        }
    }

    private void repetir(RegistrosDeIdempotencia.RespostaGuardada guardada, HttpServletResponse response) throws IOException {
        response.setStatus(guardada.status());
        response.setHeader(CABECALHO_REPETIDA, "true");
        if (guardada.tipoDeConteudo() != null) {
            response.setContentType(guardada.tipoDeConteudo());
        }
        if (guardada.endereco() != null) {
            response.setHeader(HttpHeaders.LOCATION, guardada.endereco());
        }
        response.setContentLength(guardada.corpo().length);
        response.getOutputStream().write(guardada.corpo());
    }

    /** Resumo (SHA-256) do método, do endereço e do corpo: requisições iguais têm a mesma impressão. */
    private String impressao(CorpoRelido request) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update((request.getMethod() + " " + request.getRequestURI() + "?" + request.getQueryString() + "\n")
                .getBytes(StandardCharsets.UTF_8));
            sha.update(request.corpo);
            return HexFormat.of().formatHex(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível nesta JVM", e);
        }
    }

    /** Requisição cujo corpo já foi lido e pode ser lido de novo pelo controller. */
    private static final class CorpoRelido extends HttpServletRequestWrapper {

        private final byte[] corpo;

        CorpoRelido(HttpServletRequest request) throws IOException {
            super(request);
            this.corpo = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bytes = new ByteArrayInputStream(corpo);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bytes.read();
                }

                @Override
                public int read(byte[] destino, int inicio, int tamanho) {
                    return bytes.read(destino, inicio, tamanho);
                }

                @Override
                public boolean isFinished() {
                    return bytes.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Leitura assíncrona não é usada nesta API");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            String codificacao = getCharacterEncoding() == null ? "UTF-8" : getCharacterEncoding();
            return new BufferedReader(new InputStreamReader(getInputStream(), Charset.forName(codificacao)));
        }
    }
}

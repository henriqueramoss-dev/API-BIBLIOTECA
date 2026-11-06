package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.exception.LimiteDeRequisicoesException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Limite de requisições por cliente (rate limiting).
 *
 *  - Com uma chave de API válida, o cliente é a chave: limite maior.
 *  - Sem chave (ou com chave inválida), o cliente é o endereço IP: limite menor.
 *
 * Toda resposta da API informa o limite e o uso:
 *  X-RateLimit-Limit     -> requisições permitidas por minuto
 *  X-RateLimit-Remaining -> quantas ainda restam na janela atual
 *  X-RateLimit-Reset     -> segundos até a contagem recomeçar
 * Passando do limite, a resposta é 429 com o cabeçalho Retry-After (segundos para tentar de novo).
 *
 * Roda antes da autenticação, então até as tentativas com chave falsa ou senha errada contam.
 */
@Component
@Order(OrdemDosFiltros.LIMITE_DE_REQUISICOES)
public class LimiteDeRequisicoesFilter extends OncePerRequestFilter {

    private final LimitadorDeRequisicoes limitador;
    private final ClienteDaRequisicao cliente;
    private final HandlerExceptionResolver resolvedorDeErros;
    private final boolean ativo;
    private final int limiteSemChave;
    private final int limiteComChave;

    public LimiteDeRequisicoesFilter(
        LimitadorDeRequisicoes limitador,
        ClienteDaRequisicao cliente,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolvedorDeErros,
        @Value("${biblioteca.limite.ativo:true}") boolean ativo,
        @Value("${biblioteca.limite.requisicoes-por-minuto.sem-chave}") int limiteSemChave,
        @Value("${biblioteca.limite.requisicoes-por-minuto.com-chave}") int limiteComChave
    ) {
        this.limitador = limitador;
        this.cliente = cliente;
        this.resolvedorDeErros = resolvedorDeErros;
        this.ativo = ativo;
        this.limiteSemChave = limiteSemChave;
        this.limiteComChave = limiteComChave;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Swagger, /v3/api-docs e demais endereços que não são da API ficam de fora.
        return !ativo || !RegrasDeAcesso.ehRotaDaApi(ChaveApiFilter.caminho(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadeia)
        throws ServletException, IOException {
        int limite = cliente.chave(request).isPresent() ? limiteComChave : limiteSemChave;
        LimitadorDeRequisicoes.Consumo consumo = limitador.registrar(cliente.identificador(request), limite);

        response.setHeader("X-RateLimit-Limit", String.valueOf(consumo.limite()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(consumo.restantes()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(consumo.segundosParaReiniciar()));

        if (!consumo.permitido()) {
            resolvedorDeErros.resolveException(request, response, null,
                new LimiteDeRequisicoesException(limite, consumo.segundosParaReiniciar()));
            return;
        }

        cadeia.doFilter(request, response);
    }
}

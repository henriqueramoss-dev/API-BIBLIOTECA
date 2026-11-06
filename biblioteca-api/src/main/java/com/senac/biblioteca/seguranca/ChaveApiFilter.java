package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.exception.AcessoNegadoException;
import com.senac.biblioteca.exception.NaoAutenticadoException;
import com.senac.biblioteca.service.ChaveApiService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

/**
 * Autenticação pela chave de API (cabeçalho X-API-Key).
 *
 * Para cada requisição:
 *  1. se veio uma chave, confere se ela existe e está ativa (chave inválida responde 401,
 *     mesmo em rota pública, para o erro não passar despercebido);
 *  2. pergunta às RegrasDeAcesso qual nível a rota exige;
 *  3. rota protegida sem chave responde 401; chave com nível insuficiente responde 403.
 *
 * Os erros são entregues ao ApiExceptionHandler, para terem o mesmo formato JSON do resto da API.
 * A chave identificada fica guardada na requisição (ver chaveDa) para os controllers e services.
 */
@Component
@Order(OrdemDosFiltros.CHAVE_API)
public class ChaveApiFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-API-Key";

    private static final String ATRIBUTO = ChaveApiFilter.class.getName() + ".chave";

    private final ChaveApiService service;
    private final HandlerExceptionResolver resolvedorDeErros;

    public ChaveApiFilter(
        ChaveApiService service,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolvedorDeErros
    ) {
        this.service = service;
        this.resolvedorDeErros = resolvedorDeErros;
    }

    /** Chave de API usada na requisição atual, se houver. */
    public static Optional<ChaveApi> chaveDa(HttpServletRequest request) {
        return Optional.ofNullable((ChaveApi) request.getAttribute(ATRIBUTO));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadeia)
        throws ServletException, IOException {
        try {
            autorizar(request);
        } catch (NaoAutenticadoException | AcessoNegadoException erro) {
            resolvedorDeErros.resolveException(request, response, null, erro);
            return;
        }

        cadeia.doFilter(request, response);
    }

    private void autorizar(HttpServletRequest request) {
        String valor = request.getHeader(CABECALHO);
        ChaveApi chave = null;

        if (valor != null && !valor.isBlank()) {
            chave = service.identificar(valor).orElseThrow(NaoAutenticadoException::chaveInvalida);
            request.setAttribute(ATRIBUTO, chave);
            service.registrarUso(chave);
        }

        NivelAcesso exigido = RegrasDeAcesso.nivelExigido(request.getMethod(), caminho(request));
        if (exigido == null) {
            return;
        }
        if (chave == null) {
            throw NaoAutenticadoException.semChave();
        }
        if (!chave.getNivel().permite(exigido)) {
            throw new AcessoNegadoException("Esta operação exige uma chave de API de nível " + exigido
                + " ou superior. A chave usada é de nível " + chave.getNivel() + ".");
        }
    }

    /** Caminho sem o contexto da aplicação (ex.: /livros/1). */
    static String caminho(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}

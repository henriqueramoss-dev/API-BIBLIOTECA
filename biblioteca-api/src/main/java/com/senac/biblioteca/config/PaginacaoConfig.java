package com.senac.biblioteca.config;

import com.senac.biblioteca.exception.PaginacaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Limites da paginação das listagens.
 *
 * Antes de chegar ao controller, confere os parâmetros "page" e "size" da URL.
 * Valores fora dos limites geram HTTP 400 com uma mensagem explicando o limite,
 * em vez de serem ajustados em silêncio.
 * (A página além da última é tratada em PaginaExistenteAdvice.)
 */
@Configuration
public class PaginacaoConfig implements WebMvcConfigurer {

    /** Quantidade máxima de registros por página. */
    public static final int TAMANHO_MAXIMO = 100;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                validarPagina(request.getParameter("page"));
                validarTamanho(request.getParameter("size"));
                return true;
            }
        }).excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**");
    }

    private void validarPagina(String valor) {
        if (valor == null) {
            return;
        }

        Integer pagina = numero(valor);
        if (pagina == null || pagina < 0) {
            throw new PaginacaoInvalidaException(
                "A página (page) deve ser um número a partir de 0. Valor informado: " + valor + "."
            );
        }
    }

    private void validarTamanho(String valor) {
        if (valor == null) {
            return;
        }

        Integer tamanho = numero(valor);
        if (tamanho == null || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new PaginacaoInvalidaException(
                "O tamanho da página (size) deve ser entre 1 e " + TAMANHO_MAXIMO + ". Valor informado: " + valor + "."
            );
        }
    }

    private Integer numero(String valor) {
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

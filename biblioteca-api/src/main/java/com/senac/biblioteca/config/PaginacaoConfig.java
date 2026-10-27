package com.senac.biblioteca.config;

import com.senac.biblioteca.exception.PaginacaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Limites da paginação das listagens.
 *
 * Antes de chegar ao controller, confere os parâmetros "page" e "size" da URL, mas somente nas
 * rotas que recebem um Pageable. Valores fora dos limites geram HTTP 400 com uma mensagem
 * explicando o limite, em vez de serem ajustados em silêncio pelo Spring Data.
 *
 * O tamanho máximo vem de spring.data.web.pageable.max-page-size (application.properties),
 * a mesma configuração que o Spring Data usa, então existe um só lugar para mudar o limite.
 * A conversão dos números é a mesma do Spring Data (sem remover espaços), para que um
 * valor aceito aqui nunca seja trocado depois pelo valor padrão.
 */
@Configuration
public class PaginacaoConfig implements WebMvcConfigurer {

    /** Mesmo valor padrão do Spring Data, caso a propriedade não exista. */
    static final int TAMANHO_MAXIMO_PADRAO = 2000;

    private final int tamanhoMaximo;

    public PaginacaoConfig(
        @Value("${spring.data.web.pageable.max-page-size:" + TAMANHO_MAXIMO_PADRAO + "}") int tamanhoMaximo
    ) {
        this.tamanhoMaximo = tamanhoMaximo;
    }

    /** Quantidade máxima de registros por página. */
    public int getTamanhoMaximo() {
        return tamanhoMaximo;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (recebePageable(handler)) {
                    validarPagina(request.getParameter("page"));
                    validarTamanho(request.getParameter("size"));
                }
                return true;
            }
        });
    }

    private boolean recebePageable(Object handler) {
        return handler instanceof HandlerMethod metodo
            && Arrays.stream(metodo.getMethodParameters())
                .anyMatch(parametro -> Pageable.class.isAssignableFrom(parametro.getParameterType()));
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
        if (tamanho == null || tamanho < 1 || tamanho > tamanhoMaximo) {
            throw new PaginacaoInvalidaException(
                "O tamanho da página (size) deve ser entre 1 e " + tamanhoMaximo + ". Valor informado: " + valor + "."
            );
        }
    }

    private Integer numero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

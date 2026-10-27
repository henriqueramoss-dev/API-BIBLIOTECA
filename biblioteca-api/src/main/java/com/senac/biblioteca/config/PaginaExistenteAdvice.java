package com.senac.biblioteca.config;

import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Confere, em todas as listagens paginadas, se a página pedida existe.
 *
 * Exemplo: GET /livros?page=100000 quando só existe 1 página.
 * Em vez de devolver uma lista vazia, responde HTTP 404 dizendo quantas páginas existem.
 * Uma listagem sem nenhum registro continua respondendo 200 na página 0.
 */
@RestControllerAdvice
public class PaginaExistenteAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {
        if (body instanceof PagedModel<?> pagina && pagina.getMetadata() != null) {
            long numero = pagina.getMetadata().getNumber();
            long total = pagina.getMetadata().getTotalPages();

            if (total > 0 && numero >= total) {
                throw new RecursoNaoEncontradoException(
                    "A página " + numero + " não existe. Esta listagem tem " + total
                        + (total == 1 ? " página" : " páginas") + " (de 0 a " + (total - 1) + ")."
                );
            }
        }

        return body;
    }
}

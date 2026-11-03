package com.senac.biblioteca.config;

import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;

/**
 * Confere se a página pedida existe.
 *
 * Exemplo: GET /livros?page=100000 quando só existe 1 página. Em vez de devolver
 * uma lista vazia, responde HTTP 404 dizendo quantas páginas existem.
 * Uma listagem sem nenhum registro continua respondendo 200 na página 0.
 *
 * Os controllers chamam este método logo depois da consulta de cada listagem.
 */
public final class Paginas {

    private Paginas() {
    }

    public static <T> Page<T> conferir(Page<T> pagina) {
        if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {
            throw RecursoNaoEncontradoException.pagina(pagina.getNumber(), pagina.getTotalPages());
        }

        return pagina;
    }
}

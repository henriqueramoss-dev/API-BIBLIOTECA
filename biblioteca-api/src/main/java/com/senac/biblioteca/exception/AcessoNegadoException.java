package com.senac.biblioteca.exception;

/**
 * Lançada quando a chave de API é válida, mas o nível de acesso dela não permite a operação.
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 403 - Forbidden.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}

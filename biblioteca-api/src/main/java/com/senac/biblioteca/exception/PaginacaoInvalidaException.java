package com.senac.biblioteca.exception;

/**
 * Lançada quando os parâmetros de paginação estão fora dos limites
 * (por exemplo, size=100000 ou page=-1).
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 400 - Bad Request
 * com uma mensagem explicando o limite.
 */
public class PaginacaoInvalidaException extends RuntimeException {

    public PaginacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}

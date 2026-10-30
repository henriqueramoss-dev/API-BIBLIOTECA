package com.senac.biblioteca.exception;

/**
 * Lançada quando o e-mail ou a senha do login não conferem.
 *
 * A mensagem é sempre a mesma, para não revelar se o e-mail existe.
 * É tratada no ApiExceptionHandler e retorna HTTP 401 - Unauthorized.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos.");
    }
}

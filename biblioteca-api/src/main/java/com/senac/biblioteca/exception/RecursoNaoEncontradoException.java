package com.senac.biblioteca.exception;

/**
 * Lançada quando o registro pedido na URL não existe
 * (por exemplo, GET /livros/999 com um ID que não está cadastrado).
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 404 - Not Found
 * com uma mensagem dizendo qual registro não foi encontrado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

package com.senac.biblioteca.exception;

/**
 * Lançada quando a requisição referencia, por ID, um registro relacionado
 * que não existe (por exemplo, uma categoria inexistente ao cadastrar um livro).
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 400 - Bad Request
 * com uma mensagem explicando qual ID é inválido.
 */
public class RecursoRelacionadoNaoEncontradoException extends RuntimeException {

    public RecursoRelacionadoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com ID " + id + " não encontrado(a).");
    }
}

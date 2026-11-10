package com.senac.biblioteca.enums;

/**
 * Define os estados possíveis de disponibilidade de um livro.
 *
 * A situação não é gravada no livro: a API a calcula ao responder, olhando os empréstimos
 * e as reservas dele (campo "situacao" do livro).
 */
public enum StatusLivro {

    // Está na estante: pode ser reservado ou emprestado.
    DISPONIVEL,

    // Tem empréstimo ATIVO ou ATRASADO.
    EMPRESTADO,

    // Reservado por alguém, aguardando retirada na biblioteca.
    INDISPONIVEL
}

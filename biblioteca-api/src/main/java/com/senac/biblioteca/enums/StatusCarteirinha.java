package com.senac.biblioteca.enums;

/**
 * Situação da carteirinha de um usuário.
 */
public enum StatusCarteirinha {

    // Carteirinha em dia: o usuário pode pegar livros emprestados.
    ATIVA,

    // Carteirinha bloqueada temporariamente (por exemplo, por atraso).
    SUSPENSA,

    // Carteirinha encerrada.
    CANCELADA
}

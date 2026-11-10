package com.senac.biblioteca.enums;

/**
 * Define os estados permitidos para um empréstimo.
 *
 * O uso de enum evita valores livres ou inconsistentes para representar
 * a situação de um empréstimo dentro da aplicação e do banco de dados.
 */
public enum StatusEmprestimo {

    // O livro ainda está emprestado ao usuário.
    ATIVO,

    // O livro já foi devolvido.
    DEVOLVIDO,

    // O prazo de devolução foi ultrapassado.
    ATRASADO,

    // O cliente reservou o livro pelo site e ainda vai retirá-lo na biblioteca.
    RESERVADO
}

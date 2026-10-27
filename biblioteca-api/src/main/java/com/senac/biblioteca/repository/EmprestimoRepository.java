package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Camada de acesso aos dados da entidade Emprestimo.
 *
 * Como o projeto utiliza spring.jpa.open-in-view=false, os relacionamentos
 * necessários para montar o JSON precisam ser carregados durante a consulta.
 */
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    /**
     * Lista os empréstimos já trazendo usuário, livro, categoria e autores.
     */
    @Override
    @EntityGraph(attributePaths = {"usuario", "livro", "livro.categoria", "livro.autores"})
    Page<Emprestimo> findAll(Pageable pageable);

    /**
     * Busca um empréstimo por ID com os relacionamentos necessários carregados.
     */
    @Override
    @EntityGraph(attributePaths = {"usuario", "livro", "livro.categoria", "livro.autores"})
    Optional<Emprestimo> findById(Long id);

    /**
     * Busca empréstimos pelo status informado e retorna os resultados paginados.
     */
    @EntityGraph(attributePaths = {"usuario", "livro", "livro.categoria", "livro.autores"})
    Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);

    /**
     * Empréstimos de um usuário (navegação /usuarios/{id}/emprestimos).
     */
    @EntityGraph(attributePaths = {"usuario", "livro", "livro.categoria", "livro.autores"})
    Page<Emprestimo> findByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Histórico de empréstimos de um livro (navegação /livros/{id}/emprestimos).
     */
    @EntityGraph(attributePaths = {"usuario", "livro", "livro.categoria", "livro.autores"})
    Page<Emprestimo> findByLivroId(Long livroId, Pageable pageable);
}

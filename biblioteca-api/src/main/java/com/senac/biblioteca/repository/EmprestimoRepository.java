package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Camada de acesso aos dados da entidade Emprestimo.
 *
 * Como o projeto utiliza spring.jpa.open-in-view=false, os relacionamentos
 * necessários para montar o JSON precisam ser carregados durante a consulta.
 */
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    /**
     * O livro está fora da estante? Sim quando há empréstimo ATIVO ou ATRASADO, ou uma reserva
     * ainda dentro do prazo de retirada (reserva vencida deixa de bloquear o livro).
     */
    @Query("""
        select count(e) > 0 from Emprestimo e
        where e.livro.id = :livroId
          and (e.status in (com.senac.biblioteca.enums.StatusEmprestimo.ATIVO, com.senac.biblioteca.enums.StatusEmprestimo.ATRASADO)
               or (e.status = com.senac.biblioteca.enums.StatusEmprestimo.RESERVADO and e.retirarAte >= :hoje))
        """)
    boolean livroIndisponivel(@Param("livroId") Long livroId, @Param("hoje") LocalDate hoje);

    /** Entre os livros informados, os que têm empréstimo ATIVO ou ATRASADO. */
    @Query("""
        select distinct e.livro.id from Emprestimo e
        where e.livro.id in :ids
          and e.status in (com.senac.biblioteca.enums.StatusEmprestimo.ATIVO, com.senac.biblioteca.enums.StatusEmprestimo.ATRASADO)
        """)
    List<Long> idsDeLivrosEmprestados(@Param("ids") Collection<Long> ids);

    /** Entre os livros informados, os que têm reserva ainda dentro do prazo de retirada. */
    @Query("""
        select distinct e.livro.id from Emprestimo e
        where e.livro.id in :ids
          and e.status = com.senac.biblioteca.enums.StatusEmprestimo.RESERVADO and e.retirarAte >= :hoje
        """)
    List<Long> idsDeLivrosReservados(@Param("ids") Collection<Long> ids, @Param("hoje") LocalDate hoje);

    /** Reservas de um usuário que ainda estão dentro do prazo de retirada. */
    long countByUsuarioIdAndStatusAndRetirarAteGreaterThanEqual(Long usuarioId, StatusEmprestimo status, LocalDate hoje);


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

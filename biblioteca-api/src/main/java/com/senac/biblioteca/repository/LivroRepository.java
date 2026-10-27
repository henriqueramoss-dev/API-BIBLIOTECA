package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Camada de acesso aos dados da entidade Livro.
 *
 * JpaRepository fornece as operações básicas de CRUD e paginação.
 * O EntityGraph carrega junto os relacionamentos que precisam aparecer
 * no JSON do livro, evitando erro de carregamento após o fim da consulta.
 */
public interface LivroRepository extends JpaRepository<Livro, Long> {

    /**
     * Lista os livros já trazendo Categoria e Autores.
     * Isso é necessário porque o projeto está com spring.jpa.open-in-view=false.
     */
    @Override
    @EntityGraph(attributePaths = {"categoria", "autores"})
    Page<Livro> findAll(Pageable pageable);

    /**
     * Busca um livro por ID junto com sua Categoria e seus Autores.
     */
    @Override
    @EntityGraph(attributePaths = {"categoria", "autores"})
    Optional<Livro> findById(Long id);

    /**
     * Consulta personalizada por parte do título, ignorando maiúsculas
     * e minúsculas, com paginação e carregamento dos relacionamentos.
     */
    @EntityGraph(attributePaths = {"categoria", "autores"})
    Page<Livro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    /**
     * Livros de uma categoria (navegação /categorias/{id}/livros).
     */
    @EntityGraph(attributePaths = {"categoria", "autores"})
    Page<Livro> findByCategoriaId(Long categoriaId, Pageable pageable);

    /**
     * Livros de um autor (navegação /autores/{id}/livros).
     */
    @EntityGraph(attributePaths = {"categoria", "autores"})
    Page<Livro> findByAutoresId(Long autorId, Pageable pageable);
}

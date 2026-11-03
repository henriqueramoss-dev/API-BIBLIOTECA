package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.Carteirinha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Camada de acesso aos dados da entidade Carteirinha.
 *
 * JpaRepository já fornece as operações básicas de CRUD, como salvar,
 * buscar por ID, listar, atualizar e excluir registros.
 */
public interface CarteirinhaRepository extends JpaRepository<Carteirinha, Long> {

    /**
     * Consulta personalizada exigida pelo projeto.
     * Procura registros pelo número da carteirinha, ignorando diferença entre
     * maiúsculas e minúsculas, e devolve o resultado de forma paginada.
     */
    Page<Carteirinha> findByNumeroContainingIgnoreCase(String numero, Pageable pageable);
}

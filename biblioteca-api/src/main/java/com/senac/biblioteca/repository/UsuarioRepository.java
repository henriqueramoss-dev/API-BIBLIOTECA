package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Camada de acesso aos dados da entidade Usuario.
 *
 * JpaRepository já fornece as operações básicas de CRUD, como salvar,
 * buscar por ID, listar, atualizar e excluir registros.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Consulta personalizada exigida pelo projeto.
     * Procura registros pelo campo nome, ignorando diferença entre maiúsculas
     * e minúsculas, e devolve o resultado de forma paginada.
     */
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Usada no login: procura o usuário pelo e-mail, ignorando maiúsculas e minúsculas.
    Optional<Usuario> findByEmailIgnoreCase(String email);
}

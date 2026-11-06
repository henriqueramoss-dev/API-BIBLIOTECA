package com.senac.biblioteca.repository;

import com.senac.biblioteca.entity.ChaveApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso aos dados das chaves de API.
 */
public interface ChaveApiRepository extends JpaRepository<ChaveApi, Long> {

    /**
     * Procura a chave pelo hash (a chave em si nunca é gravada).
     * Já traz o usuário dono, usado nas regras de acesso.
     */
    @EntityGraph(attributePaths = "usuario")
    Optional<ChaveApi> findByHash(String hash);

    /** Chaves de um usuário, paginadas. */
    Page<ChaveApi> findByUsuarioId(Long usuarioId, Pageable pageable);
}

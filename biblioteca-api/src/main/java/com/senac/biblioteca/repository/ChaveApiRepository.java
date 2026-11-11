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
    // "usuario_id" com sublinhado: o ChaveApi tem o campo "usuario" (e só o getter getUsuarioId), então o Spring
    // precisa ser avisado de que é usuario.id. Sem isso a consulta falhava (500) para quem não é ADMIN.
    Page<ChaveApi> findByUsuario_Id(Long usuarioId, Pageable pageable);
}

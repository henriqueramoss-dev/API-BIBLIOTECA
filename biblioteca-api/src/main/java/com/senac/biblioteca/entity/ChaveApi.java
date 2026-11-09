package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senac.biblioteca.enums.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Chave de API usada no cabeçalho X-API-Key para acessar as rotas protegidas.
 *
 * A chave em si só é mostrada uma vez, no momento em que é gerada. O banco guarda apenas
 * o hash SHA-256 dela (como uma senha) e o prefixo, que ajuda a reconhecer a chave na listagem.
 */
@Entity
@Table(name = "chaves_api")
@Schema(description = "Chave de API (a chave completa nunca é devolvida depois de gerada)")
public class ChaveApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", example = "1")
    private Long id;

    @Column(nullable = false, length = 60)
    @Schema(description = "Nome para reconhecer a chave", example = "Postman do laboratório")
    private String nome;

    @Column(nullable = false, length = 12)
    @Schema(description = "Início da chave, para reconhecê-la sem mostrá-la inteira", example = "bib_3kF9xQ")
    private String prefixo;

    // Hash SHA-256 da chave (64 caracteres hexadecimais). Nunca sai da API.
    @JsonIgnore
    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Schema(description = "Nível de acesso da chave", example = "LEITURA")
    private NivelAcesso nivel;

    @Schema(description = "Se a chave ainda pode ser usada (false depois de revogada)", example = "true")
    private boolean ativa = true;

    @Column(nullable = false)
    @Schema(description = "Quando a chave foi gerada", example = "2026-11-06T10:15:30")
    private LocalDateTime criadaEm;

    @Schema(description = "Último uso da chave (atualizado no máximo uma vez por minuto)", example = "2026-11-06T10:20:00")
    private LocalDateTime ultimoUsoEm;

    @Schema(description = "Quando a chave foi revogada", example = "2026-11-07T08:00:00")
    private LocalDateTime revogadaEm;

    // Dono da chave. Fica vazio na chave de administrador criada ao iniciar a API.
    // Se o usuário for excluído, as chaves dele são excluídas junto (ON DELETE CASCADE).
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    public ChaveApi() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrefixo() {
        return prefixo;
    }

    public void setPrefixo(String prefixo) {
        this.prefixo = prefixo;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public NivelAcesso getNivel() {
        return nivel;
    }

    public void setNivel(NivelAcesso nivel) {
        this.nivel = nivel;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getUltimoUsoEm() {
        return ultimoUsoEm;
    }

    public void setUltimoUsoEm(LocalDateTime ultimoUsoEm) {
        this.ultimoUsoEm = ultimoUsoEm;
    }

    public LocalDateTime getRevogadaEm() {
        return revogadaEm;
    }

    public void setRevogadaEm(LocalDateTime revogadaEm) {
        this.revogadaEm = revogadaEm;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // Só o ID do dono aparece no JSON, sem os dados pessoais dele.
    @Schema(description = "ID do usuário dono da chave (vazio na chave de administrador inicial)", example = "1")
    public Long getUsuarioId() {
        return usuario == null ? null : usuario.getId();
    }
}

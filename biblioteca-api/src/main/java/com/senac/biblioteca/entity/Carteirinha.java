package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.senac.biblioteca.enums.StatusCarteirinha;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Carteirinha da biblioteca: cada usuário pode ter uma só.
 * Ela demonstra o relacionamento One-to-One exigido no projeto.
 */
@Entity
@Table(name = "carteirinhas")
@Schema(description = "Carteirinha da biblioteca de um usuário")
public class Carteirinha {

    // Identificador único da carteirinha.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    // Número impresso na carteirinha. É gravado em maiúsculas e não pode se repetir.
    @NotBlank(message = "O número da carteirinha é obrigatório")
    @Pattern(regexp = "(?i)[A-Z0-9-]{4,20}", message = "O número deve ter de 4 a 20 caracteres, só com letras, números e hífen")
    @Column(nullable = false, unique = true, length = 20)
    @Schema(description = "Número da carteirinha (gravado em maiúsculas)", example = "CB-000123")
    private String numero;

    @NotNull(message = "A data de emissão é obrigatória")
    @Schema(description = "Data de emissão", example = "2026-02-01")
    private LocalDate dataEmissao;

    @NotNull(message = "A data de validade é obrigatória")
    @Schema(description = "Data de validade (não pode ser anterior à emissão)", example = "2027-02-01")
    private LocalDate dataValidade;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Situação da carteirinha", example = "ATIVA")
    private StatusCarteirinha status;

    @NotNull(message = "O limite de empréstimos é obrigatório")
    @Min(value = 1, message = "O limite de empréstimos deve ser de pelo menos 1")
    @Max(value = 10, message = "O limite de empréstimos deve ser de no máximo 10")
    @Schema(description = "Quantidade de livros que o usuário pode ter emprestados ao mesmo tempo (informativo)", example = "3")
    private Integer limiteEmprestimos;

    // Relacionamento One-to-One: cada carteirinha pertence a um único usuário.
    @NotNull(message = "O usuário é obrigatório")
    // Os dados pessoais do usuário não são repetidos aqui: só aparecem nas rotas de /usuarios.
    @JsonIgnoreProperties({"cpf", "telefone", "endereco", "cidade", "estado", "cep", "dataNascimento"})
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Construtor vazio necessário para o JPA.
    public Carteirinha() {
    }

    public Long getId() {
        return id;
    }

    /**
     * Usado pela camada Service para garantir que novos cadastros
     * sempre recebam o ID gerado automaticamente pelo banco.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public StatusCarteirinha getStatus() {
        return status;
    }

    public void setStatus(StatusCarteirinha status) {
        this.status = status;
    }

    public Integer getLimiteEmprestimos() {
        return limiteEmprestimos;
    }

    public void setLimiteEmprestimos(Integer limiteEmprestimos) {
        this.limiteEmprestimos = limiteEmprestimos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}

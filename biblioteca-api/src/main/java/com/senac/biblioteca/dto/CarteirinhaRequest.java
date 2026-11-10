package com.senac.biblioteca.dto;

import com.senac.biblioteca.enums.StatusCarteirinha;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Dados recebidos para criar ou atualizar uma carteirinha.
 * O usuarioId referencia um usuário que já existe no banco.
 */
public class CarteirinhaRequest {

    @NotBlank(message = "O número da carteirinha é obrigatório")
    @Pattern(regexp = "(?i)[A-Z0-9-]{4,20}", message = "O número deve ter de 4 a 20 caracteres, só com letras, números e hífen")
    @Schema(description = "Número da carteirinha (gravado em maiúsculas)", example = "CB-000123")
    private String numero;

    @NotNull(message = "A data de emissão é obrigatória")
    @Schema(description = "Data de emissão", example = "2026-02-01")
    private LocalDate dataEmissao;

    @NotNull(message = "A data de validade é obrigatória")
    @Schema(description = "Data de validade (não pode ser anterior à emissão)", example = "2027-02-01")
    private LocalDate dataValidade;

    @NotNull(message = "O status é obrigatório")
    @Schema(description = "Situação da carteirinha", example = "ATIVA")
    private StatusCarteirinha status;

    @NotNull(message = "O limite de empréstimos é obrigatório")
    @Min(value = 1, message = "O limite de empréstimos deve ser de pelo menos 1")
    @Max(value = 10, message = "O limite de empréstimos deve ser de no máximo 10")
    @Schema(description = "Quantidade de livros que o usuário pode ter emprestados ao mesmo tempo (informativo)", example = "3")
    private Integer limiteEmprestimos;

    @NotNull(message = "O ID do usuário é obrigatório")
    @Schema(description = "ID de um usuário já cadastrado e que ainda não tem carteirinha (cada usuário tem uma)", example = "6")
    private Long usuarioId;

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }
    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }
    public StatusCarteirinha getStatus() { return status; }
    public void setStatus(StatusCarteirinha status) { this.status = status; }
    public Integer getLimiteEmprestimos() { return limiteEmprestimos; }
    public void setLimiteEmprestimos(Integer limiteEmprestimos) { this.limiteEmprestimos = limiteEmprestimos; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}

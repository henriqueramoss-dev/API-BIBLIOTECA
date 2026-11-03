package com.senac.biblioteca.dto;

import com.senac.biblioteca.enums.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados recebidos para criar ou atualizar um empréstimo.
 * Usuário e livro são referenciados pelos IDs já cadastrados.
 */
public class EmprestimoRequest {

    @NotNull(message = "A data do empréstimo é obrigatória")
    @Schema(description = "Data em que o empréstimo foi realizado", example = "2026-10-09")
    private LocalDate dataEmprestimo;

    @NotNull(message = "A data prevista de devolução é obrigatória")
    @Schema(description = "Data prevista para devolução", example = "2026-10-25")
    private LocalDate dataPrevistaDevolucao;

    // Preenchida quando o livro é devolvido.
    @Schema(description = "Data em que o livro foi devolvido (opcional)", example = "2026-10-18")
    private LocalDate dataDevolucao;

    @NotNull(message = "O status é obrigatório")
    @Schema(description = "Status do empréstimo", example = "ATIVO")
    private StatusEmprestimo status;

    @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
    @Schema(description = "Observação sobre o empréstimo (opcional)", example = "Livro com capa danificada.")
    private String observacao;

    @NotNull(message = "O ID do usuário é obrigatório")
    @Schema(description = "ID de um usuário já cadastrado", example = "1")
    private Long usuarioId;

    @NotNull(message = "O ID do livro é obrigatório")
    @Schema(description = "ID de um livro já cadastrado", example = "1")
    private Long livroId;

    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDate dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }
    public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate dataDevolucao) { this.dataDevolucao = dataDevolucao; }
    public StatusEmprestimo getStatus() { return status; }
    public void setStatus(StatusEmprestimo status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }
}

package com.senac.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados para reservar um livro (POST /reservas). Quem reserva é o dono da chave de API.
 */
public class ReservaRequest {

    @NotNull(message = "O ID do livro é obrigatório")
    @Schema(description = "ID do livro que será reservado", example = "1")
    private Long livroId;

    @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
    @Schema(description = "Recado para a biblioteca (opcional)", example = "Retiro no sábado de manhã.")
    private String observacao;

    public Long getLivroId() {
        return livroId;
    }

    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}

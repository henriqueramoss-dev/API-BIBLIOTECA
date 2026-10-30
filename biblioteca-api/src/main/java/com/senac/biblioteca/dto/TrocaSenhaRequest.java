package com.senac.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para trocar a senha (PUT /usuarios/{id}/senha).
 */
public class TrocaSenhaRequest {

    @Schema(
        description = "Senha atual. Obrigatória quando o usuário já tem uma senha; "
            + "pode ficar em branco só no primeiro cadastro de senha.",
        example = "minhaSenha123"
    )
    private String senhaAtual;

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 72, message = "A nova senha deve ter de 8 a 72 caracteres")
    @Schema(description = "Nova senha (de 8 a 72 caracteres)", example = "novaSenha456")
    private String novaSenha;

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}

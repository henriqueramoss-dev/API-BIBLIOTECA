package com.senac.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados recebidos no login (POST /auth/login).
 */
public class LoginRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Schema(description = "E-mail cadastrado", example = "henrique@email.com")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "minhaSenha123")
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

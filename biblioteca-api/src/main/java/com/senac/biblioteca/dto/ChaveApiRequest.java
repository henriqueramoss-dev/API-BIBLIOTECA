package com.senac.biblioteca.dto;

import com.senac.biblioteca.enums.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para gerar uma chave de API (POST /chaves-api).
 * O e-mail e a senha confirmam quem está pedindo a chave.
 */
public class ChaveApiRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Schema(description = "E-mail do usuário dono da chave", example = "beatriz@exemplo.com")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "Cliente12345")
    private String senha;

    @NotBlank(message = "O nome da chave é obrigatório")
    @Size(max = 60, message = "O nome da chave deve ter no máximo 60 caracteres")
    @Schema(description = "Nome para reconhecer a chave depois", example = "Postman do laboratório")
    private String nome;

    @Schema(description = "Nível de acesso (padrão LEITURA). Só funcionários podem pedir ESCRITA ou ADMIN.", example = "LEITURA")
    private NivelAcesso nivel;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public NivelAcesso getNivel() {
        return nivel;
    }

    public void setNivel(NivelAcesso nivel) {
        this.nivel = nivel;
    }
}

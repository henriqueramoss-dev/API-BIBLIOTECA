package com.senac.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

/**
 * Resposta do login: os dados do usuário (sem a senha) mais uma chave de API pronta para o botão Authorize.
 */
@JsonPropertyOrder({"chave", "nivel", "aviso"})
@Schema(name = "LoginResposta", description = "Dados do usuário e a chave de API gerada neste login")
public class LoginResposta extends RepresentationModel<LoginResposta> {

    @JsonUnwrapped
    private final Usuario usuario;

    @Schema(description = "Chave a enviar no cabeçalho X-API-Key (ou colar no botão Authorize). Mostrada só agora.",
        example = "bib_3kF9xQ2mVb7Lp0sT1uWcYd4eHg5iJk6lMn8oPq9rSt0")
    private final String chave;

    @Schema(description = "Nível da chave: ADMIN para funcionário (dono), LEITURA para os demais", example = "ADMIN")
    private final NivelAcesso nivel;

    @Schema(description = "Lembrete de que a chave não aparece de novo")
    private final String aviso = "Cole a chave no botão Authorize. Ela não será mostrada de novo; um novo login gera outra.";

    public LoginResposta(Usuario usuario, String chave, NivelAcesso nivel) {
        this.usuario = usuario;
        this.chave = chave;
        this.nivel = nivel;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getChave() {
        return chave;
    }

    public NivelAcesso getNivel() {
        return nivel;
    }

    public String getAviso() {
        return aviso;
    }
}

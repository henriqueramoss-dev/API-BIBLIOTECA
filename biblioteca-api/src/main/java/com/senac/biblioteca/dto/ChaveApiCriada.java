package com.senac.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.enums.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resposta da geração de uma chave de API: é a ÚNICA vez em que a chave completa aparece.
 */
@JsonPropertyOrder({"id", "chave", "aviso", "nome", "prefixo", "nivel", "usuarioId", "criadaEm"})
@Schema(name = "ChaveApiCriada", description = "Chave de API recém-gerada, mostrada uma única vez")
public class ChaveApiCriada {

    @Schema(description = "Identificador da chave", example = "1")
    private final Long id;

    @Schema(description = "Chave a enviar no cabeçalho X-API-Key. Guarde-a: ela não será mostrada de novo.",
        example = "bib_3kF9xQ2mVb7Lp0sT1uWcYd4eHg5iJk6lMn8oPq9rSt0")
    private final String chave;

    @Schema(description = "Lembrete de que a chave não aparece de novo",
        example = "Guarde esta chave agora: por segurança, ela não será mostrada novamente.")
    private final String aviso = "Guarde esta chave agora: por segurança, ela não será mostrada novamente.";

    @Schema(description = "Nome da chave", example = "Postman do laboratório")
    private final String nome;

    @Schema(description = "Início da chave, para reconhecê-la na listagem", example = "bib_3kF9xQ")
    private final String prefixo;

    @Schema(description = "Nível de acesso", example = "LEITURA")
    private final NivelAcesso nivel;

    @Schema(description = "ID do usuário dono da chave", example = "1")
    private final Long usuarioId;

    @Schema(description = "Quando a chave foi gerada", example = "2026-11-06T10:15:30")
    private final LocalDateTime criadaEm;

    public ChaveApiCriada(ChaveApi registro, String chave) {
        this.id = registro.getId();
        this.chave = chave;
        this.nome = registro.getNome();
        this.prefixo = registro.getPrefixo();
        this.nivel = registro.getNivel();
        this.usuarioId = registro.getUsuarioId();
        this.criadaEm = registro.getCriadaEm();
    }

    public Long getId() { return id; }
    public String getChave() { return chave; }
    public String getAviso() { return aviso; }
    public String getNome() { return nome; }
    public String getPrefixo() { return prefixo; }
    public NivelAcesso getNivel() { return nivel; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}

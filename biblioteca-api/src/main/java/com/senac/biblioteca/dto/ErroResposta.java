package com.senac.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padrão de todas as respostas de erro da API.
 * Montado pelo ApiExceptionHandler e documentado no Swagger para os códigos 4xx e 5xx.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "status", "erro", "mensagem", "campos"})
@Schema(name = "ErroResposta", description = "Formato padrão das respostas de erro da API")
public class ErroResposta {

    @Schema(description = "Momento em que o erro aconteceu", example = "2026-10-27T16:53:52")
    private final LocalDateTime timestamp;

    @Schema(description = "Código HTTP do erro", example = "404")
    private final int status;

    @Schema(description = "Tipo do erro", example = "Não encontrado")
    private final String erro;

    @Schema(description = "Explicação do erro", example = "Livro com ID 999 não encontrado.")
    private final String mensagem;

    @Schema(description = "Erros por campo (somente quando os dados enviados são inválidos)", example = "{\"titulo\": \"O título é obrigatório\"}")
    private final Map<String, String> campos;

    public ErroResposta(int status, String erro, String mensagem, Map<String, String> campos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public Map<String, String> getCampos() { return campos; }
}

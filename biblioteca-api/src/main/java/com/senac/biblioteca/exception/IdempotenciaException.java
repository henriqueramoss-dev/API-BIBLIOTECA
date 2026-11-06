package com.senac.biblioteca.exception;

import org.springframework.http.HttpStatus;

/**
 * Problemas com o cabeçalho X-Idempotency-Key.
 *
 * É tratada no ApiExceptionHandler, que responde com o código HTTP guardado aqui:
 *  - 400: chave em formato inválido;
 *  - 409: a primeira requisição com essa chave ainda está sendo processada;
 *  - 422: a chave já foi usada com outra requisição (outro corpo ou outro endereço).
 */
public class IdempotenciaException extends RuntimeException {

    private final HttpStatus status;
    private final String titulo;

    private IdempotenciaException(HttpStatus status, String titulo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.titulo = titulo;
    }

    public static IdempotenciaException chaveInvalida(int tamanhoMaximo) {
        return new IdempotenciaException(HttpStatus.BAD_REQUEST, "Chave de idempotência inválida",
            "O cabeçalho X-Idempotency-Key deve ter de 1 a " + tamanhoMaximo
                + " caracteres, usando letras, números, hífen, sublinhado, ponto ou dois-pontos. Sugestão: um UUID.");
    }

    public static IdempotenciaException emAndamento() {
        return new IdempotenciaException(HttpStatus.CONFLICT, "Requisição em andamento",
            "Uma requisição com esta X-Idempotency-Key ainda está sendo processada. Aguarde e tente de novo.");
    }

    public static IdempotenciaException reutilizada() {
        return new IdempotenciaException(HttpStatus.UNPROCESSABLE_CONTENT, "Chave de idempotência reutilizada",
            "Esta X-Idempotency-Key já foi usada com outra requisição (corpo ou endereço diferentes). "
                + "Use uma chave nova para cada operação.");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitulo() {
        return titulo;
    }
}

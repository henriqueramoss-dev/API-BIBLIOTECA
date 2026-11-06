package com.senac.biblioteca.exception;

/**
 * Lançada quando um cliente passa do limite de requisições por minuto.
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 429 - Too Many Requests,
 * com o cabeçalho Retry-After dizendo quantos segundos esperar.
 */
public class LimiteDeRequisicoesException extends RuntimeException {

    private final long segundosParaTentarDeNovo;

    public LimiteDeRequisicoesException(int limite, long segundosParaTentarDeNovo) {
        super("Limite de " + limite + " requisições por minuto atingido. Tente novamente em "
            + segundosParaTentarDeNovo + (segundosParaTentarDeNovo == 1 ? " segundo." : " segundos."));
        this.segundosParaTentarDeNovo = segundosParaTentarDeNovo;
    }

    public long getSegundosParaTentarDeNovo() {
        return segundosParaTentarDeNovo;
    }
}

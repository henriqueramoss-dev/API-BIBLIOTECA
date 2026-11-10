package com.senac.biblioteca.exception;

/**
 * Reserva que não pode ser feita ou cancelada pelas regras da biblioteca
 * (livro indisponível, limite de reservas, reserva que já foi retirada...).
 * Vira HTTP 409 no ApiExceptionHandler.
 */
public class ReservaNaoPermitidaException extends RuntimeException {

    public ReservaNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}

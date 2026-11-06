package com.senac.biblioteca.exception;

/**
 * Lançada quando uma rota protegida é chamada sem chave de API, ou com uma chave
 * inexistente ou revogada.
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 401 - Unauthorized.
 */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }

    public static NaoAutenticadoException semChave() {
        return new NaoAutenticadoException(
            "Esta rota exige uma chave de API no cabeçalho X-API-Key. Gere uma em POST /chaves-api.");
    }

    public static NaoAutenticadoException chaveInvalida() {
        return new NaoAutenticadoException("A chave de API informada não existe ou foi revogada.");
    }
}

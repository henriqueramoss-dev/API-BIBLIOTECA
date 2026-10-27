package com.senac.biblioteca.exception;

import java.util.Map;

/**
 * Lançada quando os dados enviados são válidos campo a campo, mas contradizem
 * uma regra de negócio (por exemplo, devolução antes do empréstimo).
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 400 - Bad Request, com o
 * campo problemático no objeto "campos", no mesmo formato dos erros do Bean Validation.
 */
public class ValidacaoNegocioException extends RuntimeException {

    private final Map<String, String> campos;

    public ValidacaoNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campos = Map.of(campo, mensagem);
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}

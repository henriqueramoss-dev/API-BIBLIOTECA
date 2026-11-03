package com.senac.biblioteca.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Regra oficial do CPF: cada dígito verificador é calculado com os dígitos anteriores,
 * multiplicados por pesos decrescentes, e comparado com o dígito informado.
 */
public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }

        if (!valor.matches("\\d{11}") || valor.chars().distinct().count() == 1) {
            return false;
        }

        return digitoVerificador(valor, 9) == valor.charAt(9) - '0'
            && digitoVerificador(valor, 10) == valor.charAt(10) - '0';
    }

    // Calcula o dígito verificador a partir dos "quantos" primeiros dígitos.
    private int digitoVerificador(String cpf, int quantos) {
        int soma = 0;
        for (int i = 0; i < quantos; i++) {
            soma += (cpf.charAt(i) - '0') * (quantos + 1 - i);
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}

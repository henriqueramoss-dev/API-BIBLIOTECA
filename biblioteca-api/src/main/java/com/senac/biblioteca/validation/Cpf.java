package com.senac.biblioteca.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida um CPF: 11 dígitos (sem pontos ou traço), não todos iguais e com os
 * dois dígitos verificadores corretos. Campos nulos ou em branco são aceitos
 * aqui; a obrigatoriedade fica por conta do @NotBlank.
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cpf {

    String message() default "O CPF deve ter 11 dígitos, sem pontos ou traço, e ser válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

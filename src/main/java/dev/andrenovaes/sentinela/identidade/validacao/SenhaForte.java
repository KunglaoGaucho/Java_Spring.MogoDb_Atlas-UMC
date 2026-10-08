package dev.andrenovaes.sentinela.identidade.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Política de senha: mínimo de {@link #tamanhoMinimo()} caracteres, com
 * letra maiúscula, minúscula, número e símbolo, e sem espaços.
 */
@Documented
@Constraint(validatedBy = SenhaForteValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "{validacao.senha.fraca}";

    int tamanhoMinimo() default 10;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

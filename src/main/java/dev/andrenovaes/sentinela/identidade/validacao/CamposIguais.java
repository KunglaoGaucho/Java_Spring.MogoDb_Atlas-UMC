package dev.andrenovaes.sentinela.identidade.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validação de classe que exige que dois campos tenham o mesmo valor
 * (ex.: senha e confirmação). Genérica: serve para qualquer par de campos.
 * O erro é associado ao campo indicado em {@link #confirmacao()}.
 */
@Documented
@Constraint(validatedBy = CamposIguaisValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CamposIguais {

    String message() default "{validacao.campos.diferentes}";

    String campo();

    String confirmacao();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

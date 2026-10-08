package dev.andrenovaes.sentinela.identidade.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Payload;

class SenhaForteValidatorTest {

    private SenhaForteValidator validador;

    @BeforeEach
    void preparar() {
        validador = new SenhaForteValidator();
        validador.initialize(new SenhaForte() {
            @Override public Class<? extends Annotation> annotationType() { return SenhaForte.class; }
            @Override public String message() { return ""; }
            @Override public int tamanhoMinimo() { return 10; }
            @Override public Class<?>[] groups() { return new Class<?>[0]; }
            @SuppressWarnings("unchecked")
            @Override public Class<? extends Payload>[] payload() { return new Class[0]; }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "Sentinela#2026", "Ab1!Ab1!Ab", "Ção-Forte9x" })
    void aceitaSenhasQueCumpremAPolitica(String senha) {
        assertThat(validador.isValid(senha, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Curta#1",           // menos de 10
            "semmaiuscula#12",   // sem maiúscula
            "SEMMINUSCULA#12",   // sem minúscula
            "SemNumero#####",    // sem número
            "SemSimbolo1234",    // sem símbolo
            "Com Espaco#123"     // com espaço
    })
    void rejeitaSenhasFracas(String senha) {
        assertThat(validador.isValid(senha, null)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void rejeitaSenhaMaiorQueOLimiteDoBcrypt() {
        assertThat(validador.isValid("Aa1!" + "x".repeat(61), null)).isFalse();
    }
}

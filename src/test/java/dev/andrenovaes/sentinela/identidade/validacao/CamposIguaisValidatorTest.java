package dev.andrenovaes.sentinela.identidade.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.andrenovaes.sentinela.identidade.cadastro.CadastroForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CamposIguaisValidatorTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void encerrar() {
        fabrica.close();
    }

    @Test
    void apontaErroNoCampoDeConfirmacaoQuandoAsSenhasDiferem() {
        var form = formValido();
        form.setConfirmacaoSenha("Outra#Senha99");

        var violacoes = validador.validate(form);

        assertThat(violacoes).hasSize(1);
        assertThat(violacoes.iterator().next().getPropertyPath().toString()).isEqualTo("confirmacaoSenha");
    }

    @Test
    void formularioCompletoNaoTemViolacoes() {
        assertThat(validador.validate(formValido())).isEmpty();
    }

    private static CadastroForm formValido() {
        var form = new CadastroForm();
        form.setNome("Maria da Silva");
        form.setEmail("maria@exemplo.com");
        form.setSenha("Sentinela#2026");
        form.setConfirmacaoSenha("Sentinela#2026");
        form.setAceiteTermos(true);
        return form;
    }
}

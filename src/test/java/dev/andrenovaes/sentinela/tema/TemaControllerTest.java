package dev.andrenovaes.sentinela.tema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TemaControllerTest {

    @ParameterizedTest
    @CsvSource({
            "/conta, /conta",
            "/admin/contas?papel=ADMIN, /admin/contas?papel=ADMIN",
            "https://site-malicioso.com, /",
            "//site-malicioso.com, /",
            "/\\site-malicioso.com, /",
            "javascript:alert(1), /"
    })
    void soPermiteRedirecionarParaCaminhosInternos(String entrada, String esperado) {
        assertThat(TemaController.destinoInterno(entrada)).isEqualTo(esperado);
    }
}

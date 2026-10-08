package dev.andrenovaes.sentinela.identidade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PapelTest {

    @Test
    void niveisRefletemAHierarquia() {
        assertThat(Papel.ADMIN.acimaDe(Papel.GESTOR)).isTrue();
        assertThat(Papel.GESTOR.acimaDe(Papel.MEMBRO)).isTrue();
        assertThat(Papel.MEMBRO.acimaDe(Papel.ADMIN)).isFalse();
    }

    @Test
    void autoridadeUsaPrefixoPadraoDoSpringSecurity() {
        assertThat(Papel.GESTOR.autoridade().getAuthority()).isEqualTo("ROLE_GESTOR");
    }
}

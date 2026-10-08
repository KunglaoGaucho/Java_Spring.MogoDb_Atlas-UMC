package dev.andrenovaes.sentinela.identidade.cadastro;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.identidade.Papel;
import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;

class CadastroServiceTest {

    private final ContaService contaService = mock(ContaService.class);

    @Test
    void cadastroPublicoSempreCriaMembroComEmailNormalizado() {
        var servico = new CadastroService(contaService, propriedades(""));

        servico.cadastrar(form("  Joao@Exemplo.COM ", "Sentinela#2026"));

        verify(contaService).criar(eq("João"), eq("joao@exemplo.com"), eq("Sentinela#2026"), eq(Papel.MEMBRO));
    }

    @Test
    void recusaEmailForaDoDominioConfigurado() {
        var servico = new CadastroService(contaService, propriedades("@umc.br"));

        assertThatThrownBy(() -> servico.cadastrar(form("joao@gmail.com", "Sentinela#2026")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("umc.br");
        verify(contaService, never()).criar(anyString(), anyString(), anyString(), eq(Papel.MEMBRO));
    }

    @Test
    void recusaSenhaQueContemOProprioEmail() {
        var servico = new CadastroService(contaService, propriedades(""));

        assertThatThrownBy(() -> servico.cadastrar(form("joaosilva@exemplo.com", "Joaosilva#2026")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    private static CadastroForm form(String email, String senha) {
        var f = new CadastroForm();
        f.setNome("João");
        f.setEmail(email);
        f.setSenha(senha);
        f.setConfirmacaoSenha(senha);
        f.setAceiteTermos(true);
        return f;
    }

    private static SentinelaProperties propriedades(String dominio) {
        return new SentinelaProperties(
                new SentinelaProperties.Branding("Teste", ""),
                new SentinelaProperties.Tema("aurora", List.of()),
                new SentinelaProperties.Seguranca(5, 15, dominio),
                new SentinelaProperties.Sessao("http_sessions", 30, false),
                new SentinelaProperties.AdminInicial("Admin", "", ""));
    }
}

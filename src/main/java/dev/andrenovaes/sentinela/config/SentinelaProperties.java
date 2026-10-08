package dev.andrenovaes.sentinela.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Todas as configurações próprias da aplicação em um único lugar, tipadas.
 * Os valores vêm de {@code application.yml}, que por sua vez lê variáveis de ambiente.
 */
@ConfigurationProperties(prefix = "sentinela")
public record SentinelaProperties(
        @DefaultValue Branding branding,
        @DefaultValue Tema tema,
        @DefaultValue Seguranca seguranca,
        @DefaultValue Sessao sessao,
        @DefaultValue AdminInicial adminInicial) {

    public record Branding(
            @DefaultValue("Sentinela") String nome,
            @DefaultValue("") String slogan) {
    }

    public record Tema(
            @DefaultValue("aurora") String padrao,
            List<OpcaoTema> disponiveis) {

        public Tema {
            disponiveis = disponiveis == null ? List.of() : List.copyOf(disponiveis);
        }

        public boolean existe(String id) {
            return id != null && disponiveis.stream().anyMatch(t -> t.id().equals(id));
        }
    }

    public record OpcaoTema(String id, String rotulo) {
    }

    public record Seguranca(
            @DefaultValue("5") int maxTentativas,
            @DefaultValue("15") long bloqueioMinutos,
            @DefaultValue("") String dominioCadastro) {
    }

    public record Sessao(
            @DefaultValue("http_sessions") String colecao,
            @DefaultValue("30") long expiraMinutos,
            @DefaultValue("false") boolean cookieSeguro) {
    }

    public record AdminInicial(
            @DefaultValue("Administrador") String nome,
            @DefaultValue("") String email,
            @DefaultValue("") String senha) {

        public boolean configurado() {
            return email != null && !email.isBlank() && senha != null && !senha.isBlank();
        }
    }
}

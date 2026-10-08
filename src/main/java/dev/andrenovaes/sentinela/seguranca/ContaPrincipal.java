package dev.andrenovaes.sentinela.seguranca;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.andrenovaes.sentinela.identidade.Conta;
import dev.andrenovaes.sentinela.identidade.Papel;

/**
 * Usuário autenticado guardado na sessão. É serializado no MongoDB junto
 * com a sessão, por isso carrega só dados mínimos (nunca a entidade inteira)
 * e descarta o hash da senha após o login ({@link #eraseCredentials()}).
 */
public final class ContaPrincipal implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nome;
    private final String email;
    private final Papel papel;
    private final boolean ativa;
    private final boolean bloqueada;
    private String senhaHash;

    private ContaPrincipal(Conta conta, boolean bloqueada) {
        this.id = conta.getId();
        this.nome = conta.getNome();
        this.email = conta.getEmail();
        this.papel = conta.getPapel();
        this.ativa = conta.isAtiva();
        this.bloqueada = bloqueada;
        this.senhaHash = conta.getSenhaHash();
    }

    public static ContaPrincipal de(Conta conta, boolean bloqueada) {
        return new ContaPrincipal(conta, bloqueada);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Papel getPapel() { return papel; }

    public String getPrimeiroNome() {
        int espaco = nome.indexOf(' ');
        return espaco > 0 ? nome.substring(0, espaco) : nome;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(papel.autoridade());
    }

    @Override public String getPassword() { return senhaHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonLocked() { return !bloqueada; }
    @Override public boolean isEnabled() { return ativa; }

    @Override
    public void eraseCredentials() {
        this.senhaHash = null;
    }
}

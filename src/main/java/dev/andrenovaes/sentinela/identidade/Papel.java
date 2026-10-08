package dev.andrenovaes.sentinela.identidade;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Perfis de acesso. A hierarquia (ADMIN &gt; GESTOR &gt; MEMBRO) é declarada em
 * {@code SegurancaConfig#hierarquiaDePapeis}: um papel superior herda
 * automaticamente as permissões dos inferiores.
 *
 * <p>Os nomes exibidos na tela ficam em {@code i18n/messages.properties}
 * (chaves {@code papel.*}). Para adaptar ao PFC basta trocar os rótulos
 * — por exemplo GESTOR = "Professor" e MEMBRO = "Aluno" — sem mexer no código.</p>
 */
public enum Papel {

    ADMIN(3),
    GESTOR(2),
    MEMBRO(1);

    private final int nivel;

    Papel(int nivel) {
        this.nivel = nivel;
    }

    public int nivel() {
        return nivel;
    }

    public boolean acimaDe(Papel outro) {
        return this.nivel > outro.nivel;
    }

    public GrantedAuthority autoridade() {
        return new SimpleGrantedAuthority("ROLE_" + name());
    }

    public String chaveMensagem() {
        return "papel." + name();
    }
}

package dev.andrenovaes.sentinela.seguranca;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.andrenovaes.sentinela.identidade.Conta;
import dev.andrenovaes.sentinela.identidade.ContaRepository;

/** Ponte entre o Spring Security e a coleção {@code contas} do MongoDB. */
@Service
public class ContaUserDetailsService implements UserDetailsService {

    private final ContaRepository contas;
    private final Clock relogio;

    public ContaUserDetailsService(ContaRepository contas, Clock relogio) {
        this.contas = contas;
        this.relogio = relogio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Conta conta = contas.findByEmail(Conta.normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Conta inexistente"));
        return ContaPrincipal.de(conta, conta.estaBloqueada(Instant.now(relogio)));
    }
}

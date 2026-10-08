package dev.andrenovaes.sentinela.sessao;

import java.time.Instant;

/** Visão de leitura de uma sessão, usada na tela "Minhas sessões". */
public record SessaoAtiva(String id, String idCurto, Instant criadaEm, Instant ultimoAcesso,
        Instant expiraEm, boolean atual) {
}

package dev.andrenovaes.sentinela.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Formatação de datas para as views ({@code ${@datas.formatar(valor)}}).
 * O banco guarda tudo em UTC; a conversão para o fuso local acontece só na exibição.
 */
@Component("datas")
public class Datas {

    private final DateTimeFormatter formato;

    public Datas(@Value("${sentinela.fuso:America/Sao_Paulo}") String fuso) {
        this.formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of(fuso));
    }

    public String formatar(Instant instante) {
        return instante == null ? "—" : formato.format(instante);
    }
}

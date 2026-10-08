package dev.andrenovaes.sentinela.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio injetável: permite testar regras de tempo (ex.: bloqueio) sem esperar. */
@Configuration
public class RelogioConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}

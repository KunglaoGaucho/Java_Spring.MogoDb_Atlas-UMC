package dev.andrenovaes.sentinela.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

/**
 * Registra o Layout Dialect, que permite às páginas "decorarem" um layout
 * mestre ({@code layout:decorate}). É isso que mantém a estrutura visual
 * em um único arquivo e facilita trocar o visual sem tocar nas páginas.
 */
@Configuration
public class VisaoConfig {

    @Bean
    LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}

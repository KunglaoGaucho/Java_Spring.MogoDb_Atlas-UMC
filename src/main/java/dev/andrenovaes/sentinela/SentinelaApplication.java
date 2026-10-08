package dev.andrenovaes.sentinela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Ponto de entrada do Sentinela Auth.
 *
 * <p>O sistema foi organizado por "capacidade" (identidade, segurança, sessão,
 * tema...) e não por camada técnica, para que cada pacote possa ser levado
 * para outro projeto — como o PFC — com o mínimo de acoplamento.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SentinelaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelaApplication.class, args);
    }
}

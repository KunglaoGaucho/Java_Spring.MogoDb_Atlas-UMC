package dev.andrenovaes.sentinela.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Habilita o preenchimento automático de {@code @CreatedDate} e
 * {@code @LastModifiedDate} nos documentos do MongoDB.
 */
@Configuration
@EnableMongoAuditing
public class PersistenciaConfig {
}

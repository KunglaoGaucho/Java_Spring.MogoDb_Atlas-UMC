package dev.andrenovaes.sentinela.tema;

import org.springframework.stereotype.Component;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Descobre qual tema visual aplicar na requisição: o escolhido pelo
 * visitante (cookie) se ele ainda existir na configuração, senão o padrão.
 */
@Component
public class TemaAtual {

    static final String COOKIE = "sentinela_tema";

    private final SentinelaProperties.Tema config;

    public TemaAtual(SentinelaProperties propriedades) {
        this.config = propriedades.tema();
    }

    public String resolver(HttpServletRequest requisicao) {
        Cookie[] cookies = requisicao.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE.equals(c.getName()) && config.existe(c.getValue())) {
                    return c.getValue();
                }
            }
        }
        return config.padrao();
    }

    public boolean valido(String id) {
        return config.existe(id);
    }
}

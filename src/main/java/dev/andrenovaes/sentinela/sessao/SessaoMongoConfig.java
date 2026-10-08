package dev.andrenovaes.sentinela.sessao;

import java.time.Duration;

import org.mongodb.spring.session.MongoIndexedSessionRepository;
import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import dev.andrenovaes.sentinela.config.SentinelaProperties;

/**
 * Sessões HTTP persistidas no MongoDB Atlas (coleção configurável).
 *
 * <p>Com a sessão fora da memória do servidor, a aplicação pode ser
 * reiniciada ou escalada horizontalmente sem deslogar ninguém, e passamos a
 * conseguir listar e revogar sessões de um usuário (ver {@link SessoesService}).
 * A expiração é feita por um índice TTL que a própria biblioteca cria.</p>
 */
@Configuration
@EnableMongoHttpSession(collectionName = "${sentinela.sessao.colecao:http_sessions}")
public class SessaoMongoConfig {

    public static final String NOME_COOKIE = "SENTINELA_SID";

    @Bean
    SessionRepositoryCustomizer<MongoIndexedSessionRepository> expiracaoDaSessao(SentinelaProperties propriedades) {
        Duration expiracao = Duration.ofMinutes(propriedades.sessao().expiraMinutos());
        return repositorio -> repositorio.setDefaultMaxInactiveInterval(expiracao);
    }

    /**
     * O Spring Session usa o próprio cookie (ignora server.servlet.session.cookie.*),
     * então as flags de segurança são definidas aqui.
     */
    @Bean
    CookieSerializer cookieDaSessao(SentinelaProperties propriedades) {
        var cookie = new DefaultCookieSerializer();
        cookie.setCookieName(NOME_COOKIE);
        cookie.setUseHttpOnlyCookie(true);
        cookie.setSameSite("Strict");
        cookie.setUseSecureCookie(propriedades.sessao().cookieSeguro());
        cookie.setCookiePath("/");
        return cookie;
    }
}

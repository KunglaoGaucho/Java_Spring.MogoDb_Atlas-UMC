package dev.andrenovaes.sentinela.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import dev.andrenovaes.sentinela.identidade.Conta;

/**
 * Bloqueio temporário de conta após várias senhas erradas seguidas.
 *
 * <p>Reage aos eventos de autenticação publicados pelo Spring Security, em vez
 * de interceptar o fluxo de login. O contador é incrementado com um
 * {@code $inc} atômico no MongoDB, então requisições simultâneas não perdem
 * tentativas. O bloqueio em si é aplicado em {@link ContaPrincipal#isAccountNonLocked()}.</p>
 */
@Component
public class ProtecaoForcaBruta {

    private static final Logger log = LoggerFactory.getLogger(ProtecaoForcaBruta.class);

    private final MongoTemplate mongo;
    private final Clock relogio;
    private final int maxTentativas;
    private final Duration duracaoBloqueio;

    public ProtecaoForcaBruta(MongoTemplate mongo, Clock relogio, SentinelaProperties propriedades) {
        this.mongo = mongo;
        this.relogio = relogio;
        this.maxTentativas = Math.max(1, propriedades.seguranca().maxTentativas());
        this.duracaoBloqueio = Duration.ofMinutes(Math.max(1, propriedades.seguranca().bloqueioMinutos()));
    }

    @EventListener
    public void aoFalhar(AuthenticationFailureBadCredentialsEvent evento) {
        String email = Conta.normalizarEmail(evento.getAuthentication().getName());
        Conta conta = mongo.findAndModify(
                porEmail(email),
                new Update().inc("tentativas_falhas", 1),
                FindAndModifyOptions.options().returnNew(true),
                Conta.class);
        if (conta == null) {
            return; // e-mail inexistente: nada a contar
        }
        if (conta.getTentativasFalhas() >= maxTentativas) {
            Instant ate = Instant.now(relogio).plus(duracaoBloqueio);
            mongo.updateFirst(porEmail(email),
                    new Update().set("bloqueada_ate", ate).set("tentativas_falhas", 0),
                    Conta.class);
            log.warn("Conta bloqueada por excesso de tentativas: id={} ate={}", conta.getId(), ate);
        }
    }

    @EventListener
    public void aoAutenticar(AuthenticationSuccessEvent evento) {
        if (evento.getAuthentication().getPrincipal() instanceof ContaPrincipal principal) {
            mongo.updateFirst(porEmail(principal.getEmail()),
                    new Update()
                            .set("tentativas_falhas", 0)
                            .unset("bloqueada_ate")
                            .set("ultimo_acesso", Instant.now(relogio)),
                    Conta.class);
        }
    }

    private static Query porEmail(String email) {
        return Query.query(Criteria.where("email").is(email));
    }
}

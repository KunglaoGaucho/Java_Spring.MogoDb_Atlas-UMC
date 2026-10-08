package dev.andrenovaes.sentinela.seguranca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import dev.andrenovaes.sentinela.identidade.ContaRepository;
import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.identidade.Papel;

/**
 * Cria o primeiro administrador quando o banco ainda não tem nenhum.
 * As credenciais vêm de variáveis de ambiente — nunca do código.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final ContaRepository contas;
    private final ContaService contaService;
    private final SentinelaProperties.AdminInicial config;

    public AdminInicial(ContaRepository contas, ContaService contaService, SentinelaProperties propriedades) {
        this.contas = contas;
        this.contaService = contaService;
        this.config = propriedades.adminInicial();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (contas.existsByPapel(Papel.ADMIN)) {
            return;
        }
        if (config == null || !config.configurado()) {
            log.warn("Nenhum ADMIN no banco e SENTINELA_ADMIN_EMAIL/SENTINELA_ADMIN_SENHA não definidos. "
                    + "Defina-os no .env para criar o administrador inicial.");
            return;
        }
        contaService.criar(config.nome(), config.email(), config.senha(), Papel.ADMIN);
        log.info("Administrador inicial criado para {}", config.email());
    }
}

package dev.andrenovaes.sentinela.identidade;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Regras de negócio sobre contas: criação, troca de senha, papel e status.
 * Controladores nunca falam direto com o repositório para alterar contas.
 */
@Service
public class ContaService {

    private static final Logger log = LoggerFactory.getLogger(ContaService.class);
    private static final Sort POR_NOME = Sort.by("nome").ascending();

    private final ContaRepository contas;
    private final PasswordEncoder codificador;
    private final EncerradorDeSessoes sessoes;
    private final Clock relogio;

    public ContaService(ContaRepository contas, PasswordEncoder codificador,
            EncerradorDeSessoes sessoes, Clock relogio) {
        this.contas = contas;
        this.codificador = codificador;
        this.sessoes = sessoes;
        this.relogio = relogio;
    }

    public Conta criar(String nome, String email, String senhaPura, Papel papel) {
        String emailNormalizado = Conta.normalizarEmail(email);
        if (contas.existsByEmail(emailNormalizado)) {
            throw new RegraDeNegocioException("email", "Este e-mail já está cadastrado.");
        }
        var conta = new Conta(nome.trim(), emailNormalizado, codificador.encode(senhaPura), papel);
        Conta salva = contas.save(conta);
        log.info("Conta criada: id={} papel={}", salva.getId(), papel);
        return salva;
    }

    public Conta buscar(String id) {
        return contas.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Conta não encontrada."));
    }

    public Conta buscarPorEmail(String email) {
        return contas.findByEmail(Conta.normalizarEmail(email))
                .orElseThrow(() -> new RegraDeNegocioException("Conta não encontrada."));
    }

    public List<Conta> listarTodas() {
        return contas.findAll(POR_NOME);
    }

    public List<Conta> listarPorPapel(Papel papel) {
        return contas.findByPapel(papel, POR_NOME);
    }

    public void atualizarNome(String id, String novoNome) {
        Conta conta = buscar(id);
        conta.renomear(novoNome);
        contas.save(conta);
    }

    /**
     * Troca a senha conferindo a atual e encerra as demais sessões da conta,
     * para que um eventual invasor perca o acesso imediatamente.
     */
    public void alterarSenha(String id, String senhaAtual, String novaSenha, String sessaoAtual) {
        Conta conta = buscar(id);
        if (!codificador.matches(senhaAtual, conta.getSenhaHash())) {
            throw new RegraDeNegocioException("senhaAtual", "A senha atual não confere.");
        }
        if (codificador.matches(novaSenha, conta.getSenhaHash())) {
            throw new RegraDeNegocioException("novaSenha", "A nova senha deve ser diferente da atual.");
        }
        conta.trocarSenha(codificador.encode(novaSenha));
        contas.save(conta);
        int encerradas = sessoes.encerrarTodas(conta.getEmail(), sessaoAtual);
        log.info("Senha alterada: id={} sessoesEncerradas={}", id, encerradas);
    }

    /** Altera o papel de uma conta. Impede que o sistema fique sem administradores. */
    public void alterarPapel(String id, Papel novoPapel, String idDoAutor) {
        Conta conta = buscar(id);
        if (conta.getPapel() == novoPapel) {
            return;
        }
        if (conta.getId().equals(idDoAutor)) {
            throw new RegraDeNegocioException("Você não pode alterar o seu próprio papel.");
        }
        garantirOutroAdminAtivo(conta);
        conta.definirPapel(novoPapel);
        contas.save(conta);
        // As autoridades ficam gravadas na sessão: força novo login para refletir o papel.
        sessoes.encerrarTodas(conta.getEmail(), null);
        log.info("Papel alterado: id={} novoPapel={} autor={}", id, novoPapel, idDoAutor);
    }

    public void definirAtiva(String id, boolean ativa, String idDoAutor) {
        Conta conta = buscar(id);
        if (!ativa && conta.getId().equals(idDoAutor)) {
            throw new RegraDeNegocioException("Você não pode desativar a sua própria conta.");
        }
        if (ativa) {
            conta.ativar();
        } else {
            garantirOutroAdminAtivo(conta);
            conta.desativar();
        }
        contas.save(conta);
        if (!ativa) {
            sessoes.encerrarTodas(conta.getEmail(), null);
        }
        log.info("Conta {}: id={} autor={}", ativa ? "ativada" : "desativada", id, idDoAutor);
    }

    public void desbloquear(String id) {
        Conta conta = buscar(id);
        conta.desbloquear();
        contas.save(conta);
    }

    public boolean estaBloqueada(Conta conta) {
        return conta.estaBloqueada(Instant.now(relogio));
    }

    private void garantirOutroAdminAtivo(Conta alvo) {
        if (alvo.getPapel() != Papel.ADMIN) {
            return;
        }
        long adminsAtivos = contas.findByPapel(Papel.ADMIN, POR_NOME).stream()
                .filter(Conta::isAtiva)
                .count();
        if (adminsAtivos <= 1) {
            throw new RegraDeNegocioException("O sistema precisa manter pelo menos um administrador ativo.");
        }
    }
}

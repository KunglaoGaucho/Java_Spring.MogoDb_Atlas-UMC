package dev.andrenovaes.sentinela.sessao;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import dev.andrenovaes.sentinela.identidade.EncerradorDeSessoes;

/**
 * Consulta e revogação das sessões de um usuário, apoiada no índice por
 * "principal" que o repositório de sessões do MongoDB mantém.
 */
@Service
public class SessoesService implements EncerradorDeSessoes {

    private final FindByIndexNameSessionRepository<? extends Session> repositorio;

    public SessoesService(FindByIndexNameSessionRepository<? extends Session> repositorio) {
        this.repositorio = repositorio;
    }

    public List<SessaoAtiva> listar(String email, String sessaoAtual) {
        return sessoesDe(email).values().stream()
                .map(s -> new SessaoAtiva(
                        s.getId(),
                        s.getId().substring(0, Math.min(8, s.getId().length())),
                        s.getCreationTime(),
                        s.getLastAccessedTime(),
                        s.getLastAccessedTime().plus(s.getMaxInactiveInterval()),
                        s.getId().equals(sessaoAtual)))
                .sorted(Comparator.comparing(SessaoAtiva::ultimoAcesso).reversed())
                .toList();
    }

    /** Revoga uma sessão específica, desde que ela pertença ao usuário informado. */
    public boolean revogar(String email, String idSessao) {
        if (!sessoesDe(email).containsKey(idSessao)) {
            return false;
        }
        repositorio.deleteById(idSessao);
        return true;
    }

    @Override
    public int encerrarTodas(String email, String manter) {
        int total = 0;
        for (String id : sessoesDe(email).keySet()) {
            if (!Objects.equals(id, manter)) {
                repositorio.deleteById(id);
                total++;
            }
        }
        return total;
    }

    private Map<String, ? extends Session> sessoesDe(String email) {
        return repositorio.findByPrincipalName(email);
    }
}

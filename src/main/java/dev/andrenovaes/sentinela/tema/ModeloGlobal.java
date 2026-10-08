package dev.andrenovaes.sentinela.tema;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import dev.andrenovaes.sentinela.identidade.Papel;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Atributos disponíveis em todas as páginas: identidade visual e tema.
 * As views só leem estes valores; não sabem de onde eles vêm.
 */
@ControllerAdvice
public class ModeloGlobal {

    private final SentinelaProperties propriedades;
    private final TemaAtual temaAtual;

    public ModeloGlobal(SentinelaProperties propriedades, TemaAtual temaAtual) {
        this.propriedades = propriedades;
        this.temaAtual = temaAtual;
    }

    @ModelAttribute("marca")
    public SentinelaProperties.Branding marca() {
        return propriedades.branding();
    }

    @ModelAttribute("tema")
    public String tema(HttpServletRequest requisicao) {
        return temaAtual.resolver(requisicao);
    }

    @ModelAttribute("temas")
    public List<SentinelaProperties.OpcaoTema> temas() {
        return propriedades.tema().disponiveis();
    }

    @ModelAttribute("papeis")
    public Papel[] papeis() {
        return Papel.values();
    }

    @ModelAttribute("rotaAtual")
    public String rotaAtual(HttpServletRequest requisicao) {
        return requisicao.getRequestURI();
    }
}

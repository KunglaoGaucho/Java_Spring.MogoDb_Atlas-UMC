package dev.andrenovaes.sentinela.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Converte regras de negócio violadas em mensagem flash e volta para a página
 * anterior (padrão PRG), em vez de exibir uma página de erro genérica.
 */
@ControllerAdvice
public class TratamentoDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public String regraViolada(RegraDeNegocioException e, HttpServletRequest requisicao,
            RedirectAttributes redirecionamento) {
        redirecionamento.addFlashAttribute("erro", e.getMessage());
        return "redirect:" + paginaDeOrigem(requisicao);
    }

    /** Usa só o caminho do Referer, e apenas se for interno — evita open redirect. */
    static String paginaDeOrigem(HttpServletRequest requisicao) {
        String referer = requisicao.getHeader("Referer");
        if (referer == null) {
            return "/painel";
        }
        try {
            var uri = java.net.URI.create(referer);
            String caminho = uri.getRawPath();
            boolean mesmoHost = uri.getHost() == null || uri.getHost().equalsIgnoreCase(requisicao.getServerName());
            if (mesmoHost && caminho != null && caminho.startsWith("/") && !caminho.startsWith("//")) {
                return caminho;
            }
        } catch (IllegalArgumentException ignorado) {
            // Referer malformado: cai no padrão
        }
        return "/painel";
    }
}

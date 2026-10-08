package dev.andrenovaes.sentinela.tema;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Grava a preferência de tema do visitante. Puramente visual. */
@Controller
public class TemaController {

    private final TemaAtual temaAtual;

    public TemaController(TemaAtual temaAtual) {
        this.temaAtual = temaAtual;
    }

    @PostMapping("/tema")
    public String escolher(@RequestParam String id, @RequestParam(defaultValue = "/") String voltar,
            HttpServletRequest requisicao, HttpServletResponse resposta) {
        if (temaAtual.valido(id)) {
            ResponseCookie cookie = ResponseCookie.from(TemaAtual.COOKIE, id)
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(requisicao.isSecure())
                    .maxAge(Duration.ofDays(180))
                    .build();
            resposta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return "redirect:" + destinoInterno(voltar);
    }

    /** Aceita apenas caminhos relativos do próprio site. */
    static String destinoInterno(String voltar) {
        boolean interno = voltar.startsWith("/") && !voltar.startsWith("//") && !voltar.contains("\\")
                && !voltar.contains("://");
        return interno ? voltar : "/";
    }
}

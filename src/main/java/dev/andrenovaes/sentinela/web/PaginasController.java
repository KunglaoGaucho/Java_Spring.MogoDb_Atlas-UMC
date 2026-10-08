package dev.andrenovaes.sentinela.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.andrenovaes.sentinela.seguranca.ContaPrincipal;
import dev.andrenovaes.sentinela.seguranca.UsuarioLogado;

/** Páginas de navegação geral: início, login, painel e acesso negado. */
@Controller
public class PaginasController {

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/entrar")
    public String entrar(Authentication autenticacao) {
        boolean logado = autenticacao != null && !(autenticacao instanceof AnonymousAuthenticationToken);
        return logado ? "redirect:/painel" : "auth/entrar";
    }

    /** Após o login, cada papel cai na sua página principal. */
    @GetMapping("/painel")
    public String painel(@UsuarioLogado ContaPrincipal usuario) {
        return switch (usuario.getPapel()) {
            case ADMIN -> "redirect:/admin/contas";
            case GESTOR -> "redirect:/gestao";
            case MEMBRO -> "redirect:/area";
        };
    }

    @RequestMapping("/acesso-negado")
    public String acessoNegado() {
        return "error/403";
    }
}

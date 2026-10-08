package dev.andrenovaes.sentinela.sessao;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.andrenovaes.sentinela.seguranca.ContaPrincipal;
import dev.andrenovaes.sentinela.seguranca.UsuarioLogado;
import jakarta.servlet.http.HttpSession;

/** Tela em que o próprio usuário vê onde está conectado e encerra sessões. */
@Controller
@RequestMapping("/conta/sessoes")
public class SessoesController {

    private final SessoesService sessoesService;

    public SessoesController(SessoesService sessoesService) {
        this.sessoesService = sessoesService;
    }

    @GetMapping
    public String listar(@UsuarioLogado ContaPrincipal usuario, HttpSession sessao, Model model) {
        model.addAttribute("sessoes", sessoesService.listar(usuario.getEmail(), sessao.getId()));
        return "conta/sessoes";
    }

    @PostMapping("/{id}/revogar")
    public String revogar(@PathVariable String id, @UsuarioLogado ContaPrincipal usuario,
            HttpSession sessao, RedirectAttributes redirecionamento) {
        if (id.equals(sessao.getId())) {
            redirecionamento.addFlashAttribute("erro", "Para encerrar a sessão atual, use o botão Sair.");
        } else if (sessoesService.revogar(usuario.getEmail(), id)) {
            redirecionamento.addFlashAttribute("sucesso", "Sessão encerrada.");
        } else {
            redirecionamento.addFlashAttribute("erro", "Sessão não encontrada.");
        }
        return "redirect:/conta/sessoes";
    }

    @PostMapping("/encerrar-outras")
    public String encerrarOutras(@UsuarioLogado ContaPrincipal usuario, HttpSession sessao,
            RedirectAttributes redirecionamento) {
        int total = sessoesService.encerrarTodas(usuario.getEmail(), sessao.getId());
        redirecionamento.addFlashAttribute("sucesso", total + " sessão(ões) encerrada(s).");
        return "redirect:/conta/sessoes";
    }
}

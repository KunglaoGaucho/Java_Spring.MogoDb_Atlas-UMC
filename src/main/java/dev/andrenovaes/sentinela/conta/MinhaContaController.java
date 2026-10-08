package dev.andrenovaes.sentinela.conta;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;
import dev.andrenovaes.sentinela.seguranca.ContaPrincipal;
import dev.andrenovaes.sentinela.seguranca.UsuarioLogado;
import jakarta.servlet.http.HttpSession;

/** "Minha conta": disponível para qualquer usuário autenticado, de qualquer papel. */
@Controller
@RequestMapping("/conta")
public class MinhaContaController {

    private static final String VIEW = "conta/perfil";

    private final ContaService contaService;

    public MinhaContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public String perfil(@UsuarioLogado ContaPrincipal usuario, Model model) {
        prepararTela(usuario, model, new PerfilForm(contaService.buscar(usuario.getId()).getNome()),
                new AlterarSenhaForm());
        return VIEW;
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(@UsuarioLogado ContaPrincipal usuario,
            @Validated @ModelAttribute("perfilForm") PerfilForm form, BindingResult erros,
            Model model, RedirectAttributes redirecionamento) {
        if (erros.hasErrors()) {
            prepararTela(usuario, model, form, new AlterarSenhaForm());
            return VIEW;
        }
        contaService.atualizarNome(usuario.getId(), form.getNome());
        redirecionamento.addFlashAttribute("sucesso",
                "Nome atualizado. Ele aparecerá no cabeçalho a partir do próximo login.");
        return "redirect:/conta";
    }

    @PostMapping("/senha")
    public String alterarSenha(@UsuarioLogado ContaPrincipal usuario,
            @Validated @ModelAttribute("senhaForm") AlterarSenhaForm form, BindingResult erros,
            HttpSession sessao, Model model, RedirectAttributes redirecionamento) {
        if (!erros.hasErrors()) {
            try {
                contaService.alterarSenha(usuario.getId(), form.getSenhaAtual(), form.getNovaSenha(), sessao.getId());
                redirecionamento.addFlashAttribute("sucesso",
                        "Senha alterada. As outras sessões abertas foram encerradas.");
                return "redirect:/conta";
            } catch (RegraDeNegocioException e) {
                erros.rejectValue(e.campo(), "negocio", e.getMessage());
            }
        }
        form.limpar();
        prepararTela(usuario, model, new PerfilForm(usuario.getNome()), form);
        return VIEW;
    }

    private void prepararTela(ContaPrincipal usuario, Model model, PerfilForm perfil, AlterarSenhaForm senha) {
        model.addAttribute("conta", contaService.buscar(usuario.getId()));
        model.addAttribute("perfilForm", perfil);
        model.addAttribute("senhaForm", senha);
    }
}

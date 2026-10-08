package dev.andrenovaes.sentinela.identidade.cadastro;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private static final String VIEW = "auth/cadastro";

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @ModelAttribute("dominioPermitido")
    String dominioPermitido() {
        return cadastroService.dominioPermitido();
    }

    @GetMapping
    public String formulario(Model model, Authentication autenticacao) {
        if (estaLogado(autenticacao)) {
            return "redirect:/painel";
        }
        model.addAttribute("form", new CadastroForm());
        return VIEW;
    }

    @PostMapping
    public String cadastrar(@Validated @ModelAttribute("form") CadastroForm form, BindingResult erros,
            RedirectAttributes redirecionamento) {
        if (erros.hasErrors()) {
            limparSenhas(form);
            return VIEW;
        }
        try {
            cadastroService.cadastrar(form);
        } catch (RegraDeNegocioException e) {
            erros.rejectValue(e.campo() != null ? e.campo() : "email", "negocio", e.getMessage());
            limparSenhas(form);
            return VIEW;
        }
        redirecionamento.addFlashAttribute("sucesso", "Conta criada! Faça login para continuar.");
        return "redirect:/entrar";
    }

    /** Senhas nunca voltam preenchidas para o navegador. */
    private static void limparSenhas(CadastroForm form) {
        form.setSenha(null);
        form.setConfirmacaoSenha(null);
    }

    private static boolean estaLogado(Authentication autenticacao) {
        return autenticacao != null && autenticacao.isAuthenticated()
                && !(autenticacao instanceof AnonymousAuthenticationToken);
    }
}

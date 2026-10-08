package dev.andrenovaes.sentinela.admin;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.andrenovaes.sentinela.identidade.Conta;
import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.identidade.Papel;
import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;
import dev.andrenovaes.sentinela.seguranca.ContaPrincipal;
import dev.andrenovaes.sentinela.seguranca.UsuarioLogado;

/** Administração completa de contas — exclusivo do ADMIN. */
@Controller
@RequestMapping("/admin/contas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final String VIEW_LISTA = "admin/contas";
    private static final String VIEW_NOVA = "admin/nova-conta";

    private final ContaService contaService;
    private final Clock relogio;

    public AdminController(ContaService contaService, Clock relogio) {
        this.contaService = contaService;
        this.relogio = relogio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Papel papel, Model model) {
        var todas = contaService.listarTodas();
        Map<Papel, Long> porPapel = Arrays.stream(Papel.values())
                .collect(Collectors.toMap(Function.identity(),
                        p -> todas.stream().filter(c -> c.getPapel() == p).count()));
        model.addAttribute("contas", papel == null ? todas
                : todas.stream().filter(c -> c.getPapel() == papel).toList());
        model.addAttribute("filtro", papel);
        model.addAttribute("total", todas.size());
        model.addAttribute("porPapel", porPapel);
        model.addAttribute("agora", Instant.now(relogio));
        return VIEW_LISTA;
    }

    @GetMapping("/nova")
    public String novaForm(Model model) {
        model.addAttribute("form", new NovaContaForm());
        return VIEW_NOVA;
    }

    @PostMapping("/nova")
    public String criar(@Validated @ModelAttribute("form") NovaContaForm form, BindingResult erros,
            RedirectAttributes redirecionamento) {
        if (!erros.hasErrors()) {
            try {
                Conta criada = contaService.criar(form.getNome(), form.getEmail(), form.getSenha(), form.getPapel());
                redirecionamento.addFlashAttribute("sucesso", "Conta de " + criada.getNome() + " criada.");
                return "redirect:/admin/contas";
            } catch (RegraDeNegocioException e) {
                erros.rejectValue(e.campo() != null ? e.campo() : "email", "negocio", e.getMessage());
            }
        }
        form.setSenha(null);
        return VIEW_NOVA;
    }

    @PostMapping("/{id}/papel")
    public String alterarPapel(@PathVariable String id, @RequestParam Papel papel,
            @UsuarioLogado ContaPrincipal autor, RedirectAttributes redirecionamento) {
        contaService.alterarPapel(id, papel, autor.getId());
        redirecionamento.addFlashAttribute("sucesso", "Papel atualizado. O usuário precisará entrar novamente.");
        return "redirect:/admin/contas";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable String id, @RequestParam boolean ativa,
            @UsuarioLogado ContaPrincipal autor, RedirectAttributes redirecionamento) {
        contaService.definirAtiva(id, ativa, autor.getId());
        redirecionamento.addFlashAttribute("sucesso", ativa ? "Conta reativada." : "Conta desativada e sessões encerradas.");
        return "redirect:/admin/contas";
    }

    @PostMapping("/{id}/desbloquear")
    public String desbloquear(@PathVariable String id, RedirectAttributes redirecionamento) {
        contaService.desbloquear(id);
        redirecionamento.addFlashAttribute("sucesso", "Conta desbloqueada.");
        return "redirect:/admin/contas";
    }
}

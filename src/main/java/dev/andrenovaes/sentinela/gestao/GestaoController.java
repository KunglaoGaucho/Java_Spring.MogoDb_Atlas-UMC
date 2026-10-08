package dev.andrenovaes.sentinela.gestao;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

/**
 * Painel do GESTOR: acompanha e ativa/desativa contas de MEMBROS.
 * Não pode mexer em gestores nem administradores — isso é exclusivo do ADMIN.
 */
@Controller
@RequestMapping("/gestao")
@PreAuthorize("hasRole('GESTOR')")
public class GestaoController {

    private final ContaService contaService;

    public GestaoController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public String membros(Model model) {
        var membros = contaService.listarPorPapel(Papel.MEMBRO);
        model.addAttribute("membros", membros);
        model.addAttribute("totalAtivos", membros.stream().filter(Conta::isAtiva).count());
        return "gestao/membros";
    }

    @PostMapping("/membros/{id}/status")
    public String alterarStatus(@PathVariable String id, @RequestParam boolean ativa,
            @UsuarioLogado ContaPrincipal autor, RedirectAttributes redirecionamento) {
        Conta alvo = contaService.buscar(id);
        if (alvo.getPapel() != Papel.MEMBRO) {
            throw new RegraDeNegocioException("Gestores só podem alterar contas de membros.");
        }
        contaService.definirAtiva(id, ativa, autor.getId());
        redirecionamento.addFlashAttribute("sucesso",
                "Conta de " + alvo.getNome() + (ativa ? " reativada." : " desativada."));
        return "redirect:/gestao";
    }
}

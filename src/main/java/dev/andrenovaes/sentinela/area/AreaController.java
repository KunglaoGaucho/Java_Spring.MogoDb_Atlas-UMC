package dev.andrenovaes.sentinela.area;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.seguranca.ContaPrincipal;
import dev.andrenovaes.sentinela.seguranca.UsuarioLogado;

/**
 * Área do MEMBRO (papel base). É aqui que, no PFC, entrariam as
 * funcionalidades do usuário comum (ex.: "minhas atividades" do aluno).
 */
@Controller
@RequestMapping("/area")
public class AreaController {

    private final ContaService contaService;

    public AreaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public String inicio(@UsuarioLogado ContaPrincipal usuario, Model model) {
        model.addAttribute("conta", contaService.buscar(usuario.getId()));
        return "area/inicio";
    }
}

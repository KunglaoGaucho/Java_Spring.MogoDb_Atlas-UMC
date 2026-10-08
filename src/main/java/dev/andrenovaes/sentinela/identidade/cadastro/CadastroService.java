package dev.andrenovaes.sentinela.identidade.cadastro;

import org.springframework.stereotype.Service;

import dev.andrenovaes.sentinela.config.SentinelaProperties;
import dev.andrenovaes.sentinela.identidade.Conta;
import dev.andrenovaes.sentinela.identidade.ContaService;
import dev.andrenovaes.sentinela.identidade.Papel;
import dev.andrenovaes.sentinela.identidade.RegraDeNegocioException;

/**
 * Cadastro público (auto-registro). Toda conta criada por aqui recebe o
 * papel mais baixo ({@link Papel#MEMBRO}); promoções só por um ADMIN.
 */
@Service
public class CadastroService {

    private final ContaService contaService;
    private final String dominioPermitido;

    public CadastroService(ContaService contaService, SentinelaProperties propriedades) {
        this.contaService = contaService;
        String dominio = propriedades.seguranca().dominioCadastro();
        this.dominioPermitido = dominio == null ? "" : dominio.trim().toLowerCase().replaceFirst("^@", "");
    }

    public Conta cadastrar(CadastroForm form) {
        String email = Conta.normalizarEmail(form.getEmail());
        if (!dominioPermitido.isEmpty() && !email.endsWith("@" + dominioPermitido)) {
            throw new RegraDeNegocioException("email",
                    "Use um e-mail do domínio @" + dominioPermitido + ".");
        }
        if (senhaContemEmail(form.getSenha(), email)) {
            throw new RegraDeNegocioException("senha", "A senha não pode conter o seu e-mail.");
        }
        return contaService.criar(form.getNome(), email, form.getSenha(), Papel.MEMBRO);
    }

    public String dominioPermitido() {
        return dominioPermitido;
    }

    private static boolean senhaContemEmail(String senha, String email) {
        String usuario = email.substring(0, email.indexOf('@'));
        return usuario.length() >= 4 && senha.toLowerCase().contains(usuario);
    }
}

package dev.andrenovaes.sentinela.conta;

import dev.andrenovaes.sentinela.identidade.validacao.CamposIguais;
import dev.andrenovaes.sentinela.identidade.validacao.SenhaForte;
import jakarta.validation.constraints.NotBlank;

@CamposIguais(campo = "novaSenha", confirmacao = "confirmacaoNovaSenha")
public class AlterarSenhaForm {

    @NotBlank(message = "{validacao.obrigatorio}")
    private String senhaAtual;

    @NotBlank(message = "{validacao.obrigatorio}")
    @SenhaForte
    private String novaSenha;

    @NotBlank(message = "{validacao.obrigatorio}")
    private String confirmacaoNovaSenha;

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmacaoNovaSenha() { return confirmacaoNovaSenha; }
    public void setConfirmacaoNovaSenha(String c) { this.confirmacaoNovaSenha = c; }

    public void limpar() {
        senhaAtual = null;
        novaSenha = null;
        confirmacaoNovaSenha = null;
    }
}

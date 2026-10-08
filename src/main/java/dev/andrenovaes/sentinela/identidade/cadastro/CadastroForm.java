package dev.andrenovaes.sentinela.identidade.cadastro;

import dev.andrenovaes.sentinela.identidade.validacao.CamposIguais;
import dev.andrenovaes.sentinela.identidade.validacao.SenhaForte;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados enviados pelo formulário público de cadastro. */
@CamposIguais(campo = "senha", confirmacao = "confirmacaoSenha")
public class CadastroForm {

    @NotBlank(message = "{validacao.obrigatorio}")
    @Size(min = 3, max = 80, message = "{validacao.nome.tamanho}")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "{validacao.nome.caracteres}")
    private String nome;

    @NotBlank(message = "{validacao.obrigatorio}")
    @Email(message = "{validacao.email.invalido}")
    @Size(max = 120, message = "{validacao.email.tamanho}")
    private String email;

    @NotBlank(message = "{validacao.obrigatorio}")
    @SenhaForte
    private String senha;

    @NotBlank(message = "{validacao.obrigatorio}")
    private String confirmacaoSenha;

    @AssertTrue(message = "{validacao.termos}")
    private boolean aceiteTermos;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
    public boolean isAceiteTermos() { return aceiteTermos; }
    public void setAceiteTermos(boolean aceiteTermos) { this.aceiteTermos = aceiteTermos; }
}

package dev.andrenovaes.sentinela.admin;

import dev.andrenovaes.sentinela.identidade.Papel;
import dev.andrenovaes.sentinela.identidade.validacao.SenhaForte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Criação de conta pelo ADMIN, já com o papel definido. */
public class NovaContaForm {

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

    @NotNull(message = "{validacao.obrigatorio}")
    private Papel papel = Papel.MEMBRO;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
}

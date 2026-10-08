package dev.andrenovaes.sentinela.conta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilForm {

    @NotBlank(message = "{validacao.obrigatorio}")
    @Size(min = 3, max = 80, message = "{validacao.nome.tamanho}")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "{validacao.nome.caracteres}")
    private String nome;

    public PerfilForm() {
    }

    public PerfilForm(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}

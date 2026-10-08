package dev.andrenovaes.sentinela.identidade.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    /** Senha com mais de 72 bytes é truncada pelo BCrypt, então limitamos aqui. */
    private static final int TAMANHO_MAXIMO = 64;

    private int tamanhoMinimo;

    @Override
    public void initialize(SenhaForte anotacao) {
        this.tamanhoMinimo = anotacao.tamanhoMinimo();
    }

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext contexto) {
        if (senha == null || senha.isEmpty()) {
            return true; // obrigatoriedade é responsabilidade do @NotBlank
        }
        if (senha.length() < tamanhoMinimo || senha.length() > TAMANHO_MAXIMO) {
            return false;
        }
        boolean maiuscula = false, minuscula = false, digito = false, simbolo = false;
        for (char c : senha.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return false;
            }
            if (Character.isUpperCase(c)) maiuscula = true;
            else if (Character.isLowerCase(c)) minuscula = true;
            else if (Character.isDigit(c)) digito = true;
            else simbolo = true;
        }
        return maiuscula && minuscula && digito && simbolo;
    }
}

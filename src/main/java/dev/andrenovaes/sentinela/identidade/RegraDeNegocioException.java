package dev.andrenovaes.sentinela.identidade;

/**
 * Violação de regra de negócio que deve virar mensagem amigável na tela.
 * O {@code campo} (opcional) permite vincular o erro a um input do formulário.
 */
public class RegraDeNegocioException extends RuntimeException {

    private final String campo;

    public RegraDeNegocioException(String mensagem) {
        this(null, mensagem);
    }

    public RegraDeNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String campo() {
        return campo;
    }
}

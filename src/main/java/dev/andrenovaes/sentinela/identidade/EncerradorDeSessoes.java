package dev.andrenovaes.sentinela.identidade;

/**
 * Porta para encerrar sessões ativas de uma conta. O módulo de identidade
 * não conhece o mecanismo de sessão (MongoDB, Redis, memória...): quem
 * implementa é o pacote {@code sessao}. Trocar a tecnologia de sessão não
 * exige mudar as regras de negócio.
 */
public interface EncerradorDeSessoes {

    /** Encerra todas as sessões do e-mail informado, exceto a de id {@code manter} (pode ser null). */
    int encerrarTodas(String email, String manter);
}

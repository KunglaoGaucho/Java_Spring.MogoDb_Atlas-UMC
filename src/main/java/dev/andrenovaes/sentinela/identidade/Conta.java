package dev.andrenovaes.sentinela.identidade;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Conta de acesso armazenada na coleção {@code contas}.
 *
 * <p>Guarda apenas o necessário para identidade e controle de acesso. Dados
 * específicos de um domínio (turma, matrícula, curso...) devem ficar em
 * documentos próprios que referenciam {@link #getId()}, mantendo este núcleo
 * reaproveitável.</p>
 */
@Document(collection = "contas")
public class Conta {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    /** Hash no formato do DelegatingPasswordEncoder, ex.: {bcrypt}$2a$12$... */
    @Field("senha_hash")
    private String senhaHash;

    @Indexed
    private Papel papel = Papel.MEMBRO;

    private boolean ativa = true;

    @Field("tentativas_falhas")
    private int tentativasFalhas;

    @Field("bloqueada_ate")
    private Instant bloqueadaAte;

    @Field("ultimo_acesso")
    private Instant ultimoAcesso;

    @CreatedDate
    @Field("criada_em")
    private Instant criadaEm;

    @LastModifiedDate
    @Field("atualizada_em")
    private Instant atualizadaEm;

    protected Conta() {
        // usado pelo Spring Data
    }

    public Conta(String nome, String email, String senhaHash, Papel papel) {
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
        this.papel = papel;
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public boolean estaBloqueada(Instant agora) {
        return bloqueadaAte != null && bloqueadaAte.isAfter(agora);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Papel getPapel() { return papel; }
    public boolean isAtiva() { return ativa; }
    public int getTentativasFalhas() { return tentativasFalhas; }
    public Instant getBloqueadaAte() { return bloqueadaAte; }
    public Instant getUltimoAcesso() { return ultimoAcesso; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getAtualizadaEm() { return atualizadaEm; }

    public void renomear(String nome) { this.nome = nome.trim(); }
    public void trocarSenha(String novoHash) { this.senhaHash = novoHash; }
    public void definirPapel(Papel papel) { this.papel = papel; }
    public void ativar() { this.ativa = true; }
    public void desativar() { this.ativa = false; }

    public void desbloquear() {
        this.tentativasFalhas = 0;
        this.bloqueadaAte = null;
    }
}

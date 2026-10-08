package dev.andrenovaes.sentinela.seguranca;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import dev.andrenovaes.sentinela.identidade.Papel;
import dev.andrenovaes.sentinela.sessao.SessaoMongoConfig;

/**
 * Regras centrais de autenticação e autorização.
 *
 * <p>Mapa de acesso por URL (o detalhe fino fica em {@code @PreAuthorize}
 * nos controladores):</p>
 * <ul>
 *   <li>Público: {@code /}, {@code /entrar}, {@code /cadastro}, {@code /tema}, recursos estáticos</li>
 *   <li>{@code /area/**}: MEMBRO (e, pela hierarquia, GESTOR e ADMIN)</li>
 *   <li>{@code /gestao/**}: GESTOR e ADMIN</li>
 *   <li>{@code /admin/**}: somente ADMIN</li>
 *   <li>Qualquer outra rota: apenas autenticado</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SegurancaConfig {

    private static final int CUSTO_BCRYPT = 12;

    private static final String[] ROTAS_PUBLICAS = {
            "/", "/entrar", "/cadastro", "/tema", "/error",
            "/assets/**", "/themes/**", "/favicon.ico"
    };

    @Bean
    SecurityFilterChain cadeiaDeSeguranca(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(rotas -> rotas
                .requestMatchers(ROTAS_PUBLICAS).permitAll()
                .requestMatchers("/admin/**").hasRole(Papel.ADMIN.name())
                .requestMatchers("/gestao/**").hasRole(Papel.GESTOR.name())
                .requestMatchers("/area/**").hasRole(Papel.MEMBRO.name())
                .anyRequest().authenticated())
            .formLogin(login -> login
                .loginPage("/entrar")
                .loginProcessingUrl("/entrar")
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/painel")
                .failureUrl("/entrar?erro")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/sair")
                .logoutSuccessUrl("/entrar?saiu")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(SessaoMongoConfig.NOME_COOKIE))
            .sessionManagement(sessao -> sessao
                // Gera um novo id de sessão no login (contra session fixation).
                .sessionFixation(fixacao -> fixacao.changeSessionId()))
            .exceptionHandling(erros -> erros.accessDeniedPage("/acesso-negado"))
            .headers(cabecalhos -> cabecalhos
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; img-src 'self' data:; style-src 'self'; "
                        + "script-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'self'"))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicy.SAME_ORIGIN))
                .frameOptions(frame -> frame.deny()));
        // CSRF permanece habilitado (padrão): o Thymeleaf injeta o token em todo th:action.
        return http.build();
    }

    /**
     * Hash de senha com DelegatingPasswordEncoder: grava em BCrypt custo 12
     * ({@code {bcrypt}$2a$12$...}). O prefixo entre chaves identifica o
     * algoritmo, permitindo migrar para Argon2 no futuro sem invalidar as
     * senhas já existentes (basta trocar o "atual" e manter o antigo no mapa).
     */
    @Bean
    PasswordEncoder codificadorDeSenha() {
        String atual = "bcrypt";
        Map<String, PasswordEncoder> algoritmos = Map.of(
                atual, new BCryptPasswordEncoder(CUSTO_BCRYPT));
        return new DelegatingPasswordEncoder(atual, algoritmos);
    }

    /** ADMIN herda GESTOR, que herda MEMBRO. */
    @Bean
    static RoleHierarchy hierarquiaDePapeis() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Papel.ADMIN.name()).implies(Papel.GESTOR.name())
                .role(Papel.GESTOR.name()).implies(Papel.MEMBRO.name())
                .build();
    }

    /** Publica eventos de sucesso/falha de login (consumidos por {@link ProtecaoForcaBruta}). */
    @Bean
    AuthenticationEventPublisher publicadorDeEventos(ApplicationEventPublisher publicador) {
        return new DefaultAuthenticationEventPublisher(publicador);
    }
}

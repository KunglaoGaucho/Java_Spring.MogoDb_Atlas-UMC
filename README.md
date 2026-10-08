# Sentinela Auth

Núcleo de **autenticação e autorização** construído com **Spring Boot 4, Spring Security, Thymeleaf e MongoDB Atlas**.
Foi pensado para ser reaproveitado: identidade, segurança, sessão e aparência ficam em módulos independentes,
de modo que o sistema possa virar a base de outro projeto (como o PFC) trocando textos, tema e
acrescentando funcionalidades, sem reescrever a lógica central.

---

## Sumário

1. [Funcionalidades](#funcionalidades)
2. [Tecnologias](#tecnologias)
3. [Arquitetura](#arquitetura)
4. [Perfis de acesso](#perfis-de-acesso)
5. [Configurando o MongoDB Atlas](#configurando-o-mongodb-atlas)
6. [Executando localmente](#executando-localmente)
7. [Integração com o MongoDB Atlas](#integração-com-o-mongodb-atlas)
8. [Temas visuais](#temas-visuais)
9. [Adaptando para outro projeto (PFC)](#adaptando-para-outro-projeto-pfc)
10. [Decisões de segurança](#decisões-de-segurança)
11. [Fluxo de trabalho (Gitflow)](#fluxo-de-trabalho-gitflow)

---

## Funcionalidades

- **Cadastro** com validação no servidor (Bean Validation + validadores próprios) e política de senha forte.
- **Login e logout** via Spring Security, com senhas guardadas em **BCrypt (custo 12)**.
- **Três perfis hierárquicos** — `ADMIN` › `GESTOR` › `MEMBRO` — controlando rotas, botões e menus.
- **Bloqueio temporário** da conta após tentativas de login erradas seguidas.
- **Sessões HTTP no MongoDB Atlas**: o usuário vê onde está conectado e pode encerrar sessões.
- **Administração de contas**: criar, promover/rebaixar, ativar/desativar e desbloquear.
- **Painel do gestor**: acompanhamento e ativação/desativação de membros.
- **Temas visuais intercambiáveis** (claro, escuro e alto contraste) sem alterar nenhuma página.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Validation) |
| Segurança | Spring Security 7 |
| Persistência | Spring Data MongoDB + MongoDB Atlas |
| Sessão | `mongodb-spring-session` 4.0 (mantida pela MongoDB) |
| Interface | Thymeleaf + Layout Dialect + Thymeleaf Extras Spring Security |
| Build | Maven (wrapper incluso) |

## Arquitetura

O código é organizado **por capacidade**, não por camada técnica. Cada pacote pode ser entendido
(e reaproveitado) isoladamente:

```
src/main/java/dev/andrenovaes/sentinela
├── config/        Propriedades tipadas (SentinelaProperties), auditoria, relógio, layout
├── identidade/    Núcleo de domínio: Conta, Papel, ContaRepository, ContaService
│   ├── cadastro/  Auto-cadastro público (formulário, regras, controlador)
│   └── validacao/ @SenhaForte e @CamposIguais (validadores reutilizáveis)
├── seguranca/     SegurancaConfig, UserDetailsService, proteção contra força bruta, admin inicial
├── sessao/        Sessão no MongoDB, listagem e revogação de sessões
├── conta/         "Minha conta": perfil e troca de senha (qualquer perfil)
├── area/          Área do MEMBRO
├── gestao/        Painel do GESTOR
├── admin/         Administração de contas (ADMIN)
├── tema/          Resolução e troca de tema + atributos globais das views
└── web/           Navegação geral, formatação de datas, tratamento de erros

src/main/resources
├── application.yml          Configuração (lê variáveis de ambiente / .env)
├── i18n/messages.properties Todos os rótulos (inclusive nomes dos perfis)
├── templates/
│   ├── layout/base.html     Layout mestre (único lugar com <head>, cabeçalho e rodapé)
│   ├── fragmentos/          Cabeçalho, rodapé, alertas, seletor de tema, campos de formulário
│   └── auth, area, gestao, admin, conta, error
└── static/
    ├── assets/css/base.css  Estrutura e componentes (usa só variáveis CSS)
    ├── assets/js/app.js     Melhorias progressivas (o sistema funciona sem JS)
    └── themes/<id>/theme.css Paleta, fontes e raios de cada tema
```

Princípios aplicados:

- **Controladores finos**: recebem, validam e delegam. Regras ficam nos *services*.
- **Inversão de dependência**: `ContaService` precisa encerrar sessões, mas depende só da interface
  `EncerradorDeSessoes`; quem implementa com MongoDB é o pacote `sessao`.
- **Configuração externa**: nada sensível no código; tudo vem de `application.yml` → variáveis de ambiente.
- **Design separado da lógica**: as páginas não têm cor, fonte nem estilo inline.

## Perfis de acesso

A hierarquia é declarada uma única vez (`SegurancaConfig#hierarquiaDePapeis`): um perfil superior herda tudo do inferior.

| Perfil | Rótulo padrão | Pode acessar |
|---|---|---|
| `ADMIN` | Administrador | Tudo, incluindo `/admin/**` |
| `GESTOR` | Gestor | `/gestao/**`, `/area/**`, `/conta/**` |
| `MEMBRO` | Membro | `/area/**`, `/conta/**` |

| Rota | Acesso |
|---|---|
| `/`, `/entrar`, `/cadastro`, `/tema` | Público |
| `/area` | MEMBRO ou superior |
| `/gestao` | GESTOR ou superior |
| `/admin/contas` | Somente ADMIN |
| `/conta`, `/conta/sessoes` | Qualquer usuário autenticado |

A proteção é feita em duas camadas: por URL (`SecurityFilterChain`) e por método (`@PreAuthorize`).
Na interface, `sec:authorize` esconde menus e botões que o perfil não pode usar.

Regras de negócio extras: o cadastro público sempre cria `MEMBRO`; ninguém altera o próprio perfil
nem desativa a própria conta; o sistema nunca fica sem pelo menos um `ADMIN` ativo; o gestor só mexe em membros.

## Configurando o MongoDB Atlas

1. Crie uma conta em <https://www.mongodb.com/cloud/atlas> e um cluster (o gratuito **M0** basta).
2. **Database Access** → *Add New Database User*: crie um usuário com senha e permissão *Read and write to any database*.
3. **Network Access** → *Add IP Address*: adicione o seu IP atual (para testes, `0.0.0.0/0` libera qualquer IP — evite em produção).
4. **Database** → *Connect* → *Drivers* → *Java*: copie a *connection string* `mongodb+srv://...`.
5. Substitua `<password>` pela senha do usuário. Se a senha tiver `@ : / ? # [ ]`, use URL-encoding (ex.: `@` → `%40`).

Não é preciso criar o banco nem as coleções: a aplicação cria tudo na primeira execução.

## Executando localmente

**Pré-requisitos:** JDK 21 e acesso à internet (o Maven Wrapper baixa o Maven e as dependências).

```bash
git clone https://github.com/<seu-usuario>/sentinela-auth.git
cd sentinela-auth

cp .env.example .env        # Windows: copy .env.example .env
# edite o .env: MONGODB_URI, SENTINELA_ADMIN_EMAIL e SENTINELA_ADMIN_SENHA

./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

Acesse <http://localhost:8080> e entre com o e-mail/senha de administrador definidos no `.env`.

Outros comandos úteis:

```bash
./mvnw test                                   # testes unitários
./mvnw clean package && java -jar target/sentinela-auth-1.0.0.jar
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod   # cookie Secure, logs enxutos
```

### Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `MONGODB_URI` | **sim** | — | Connection string do Atlas |
| `MONGODB_DATABASE` | não | `sentinela` | Nome do banco |
| `SENTINELA_ADMIN_EMAIL` / `SENTINELA_ADMIN_SENHA` | na 1ª execução | — | Cria o primeiro ADMIN, se não houver nenhum |
| `SENTINELA_NOME` / `SENTINELA_SLOGAN` | não | `Sentinela` | Identidade exibida nas páginas |
| `SENTINELA_TEMA` | não | `aurora` | Tema padrão |
| `SENTINELA_MAX_TENTATIVAS` | não | `5` | Erros seguidos antes do bloqueio |
| `SENTINELA_BLOQUEIO_MINUTOS` | não | `15` | Duração do bloqueio |
| `SENTINELA_DOMINIO_CADASTRO` | não | vazio | Restringe o cadastro a um domínio (ex.: `umc.br`) |
| `SENTINELA_SESSAO_MINUTOS` | não | `30` | Expiração por inatividade |
| `SENTINELA_COOKIE_SEGURO` | não | `false` | Use `true` quando estiver em HTTPS |

Como alternativa ao `.env`, há um modelo em [`config/application-local.yml.example`](config/application-local.yml.example).

## Integração com o MongoDB Atlas

A conexão é configurada em `application.yml` (`spring.mongodb.uri`), lida da variável `MONGODB_URI`.
A string `mongodb+srv://` descobre os nós do *replica set* por DNS e **sempre usa TLS**; as credenciais
nunca aparecem no repositório (`.env` está no `.gitignore`).

Coleções criadas pela aplicação:

| Coleção | Conteúdo | Índices |
|---|---|---|
| `contas` | Nome, e-mail, hash da senha, perfil, status, contador de falhas, bloqueio, datas | `email` único, `papel` |
| `http_sessions` | Sessões HTTP serializadas (Spring Session) | `principal` (para listar sessões de um usuário) e TTL por expiração |

Exemplo de documento em `contas`:

```json
{
  "_id": "6703f1...",
  "nome": "Maria da Silva",
  "email": "maria@exemplo.com",
  "senha_hash": "{bcrypt}$2a$12$Q9v...",
  "papel": "MEMBRO",
  "ativa": true,
  "tentativas_falhas": 0,
  "ultimo_acesso": "2026-10-07T23:10:00Z",
  "criada_em": "2026-10-07T22:58:00Z",
  "atualizada_em": "2026-10-07T23:10:00Z"
}
```

Por que guardar a sessão no banco? A aplicação pode reiniciar (ou rodar em mais de uma instância)
sem deslogar ninguém, e passa a ser possível **revogar** sessões — usado quando o usuário troca a senha,
quando um ADMIN desativa uma conta ou muda o perfil de alguém.

## Temas visuais

O visual é dividido em duas camadas:

- `static/assets/css/base.css` — layout e componentes, escritos **apenas com variáveis CSS** (`var(--cor-primaria)`...).
- `static/themes/<id>/theme.css` — define os valores dessas variáveis.

**Para criar um tema:**

1. Copie `static/themes/aurora` para `static/themes/meu-tema` e altere as variáveis.
2. Registre-o em `application.yml`:
   ```yaml
   sentinela:
     tema:
       disponiveis:
         - id: meu-tema
           rotulo: Meu tema
   ```
3. (Opcional) torne-o padrão com `SENTINELA_TEMA=meu-tema`.

O visitante escolhe o tema no seletor do cabeçalho; a escolha fica num cookie. Para mudanças estruturais
(ex.: menu lateral), basta editar `templates/layout/base.html` — as páginas só preenchem o bloco `conteudo`.

## Adaptando para outro projeto (PFC)

| Quero... | Onde mexer |
|---|---|
| Renomear os perfis (ex.: Gestor → Professor, Membro → Aluno) | `i18n/messages.properties` (`papel.*`) |
| Mudar nome/slogan do sistema | `SENTINELA_NOME`, `SENTINELA_SLOGAN` |
| Mudar cores e fontes | Novo tema em `static/themes/` |
| Aceitar só e-mails institucionais | `SENTINELA_DOMINIO_CADASTRO=umc.br` |
| Criar telas novas para um perfil | Novo pacote (ex.: `turma/`) + rota sob `/area`, `/gestao` ou `/admin` |
| Guardar dados específicos do usuário | Novo documento que referencia `Conta.id` — sem alterar `Conta` |
| Adicionar um quarto perfil | Novo valor em `Papel`, uma linha na hierarquia e o rótulo em `messages.properties` |

## Decisões de segurança

| Ameaça | Mitigação |
|---|---|
| Vazamento de senhas | BCrypt custo 12 via `DelegatingPasswordEncoder` (prefixo `{bcrypt}` permite migrar de algoritmo); hash descartado da sessão após o login |
| Senhas fracas | `@SenhaForte`: 10–64 caracteres, maiúscula, minúscula, número, símbolo, sem espaço; não pode conter o e-mail |
| Força bruta | Bloqueio temporário após N falhas, com contador atômico (`$inc`) no MongoDB |
| Enumeração de contas | Mensagem de erro de login única para qualquer causa |
| Sequestro de sessão | Cookie `HttpOnly` + `SameSite=Strict` (+ `Secure` em produção); novo id de sessão a cada login |
| Sessões órfãs | Revogação automática ao trocar senha, mudar perfil ou desativar conta |
| CSRF | Token obrigatório em todo POST (injetado pelo Thymeleaf) |
| XSS / clickjacking | Escape automático do Thymeleaf; CSP sem scripts/estilos inline; `frame-ancestors 'none'` |
| Open redirect | Destinos de redirecionamento validados (apenas caminhos internos) |
| Escalada de privilégio | Cadastro público só cria MEMBRO; checagem por URL **e** por método |
| Segredos no Git | Credenciais apenas em variáveis de ambiente / `.env` ignorado |

## Fluxo de trabalho (Gitflow)

| Branch | Uso |
|---|---|
| `main` | Somente versões publicadas (tags `vX.Y.Z`) |
| `develop` | Integração das funcionalidades |
| `feature/*` | Uma funcionalidade por branch, criada a partir de `develop` |
| `release/*` | Preparação de versão (ajustes finais e número de versão) |
| `hotfix/*` | Correções urgentes a partir de `main` |

Todos os merges usam `--no-ff`, preservando no histórico onde cada funcionalidade começou e terminou.
Mensagens de commit seguem o padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/).


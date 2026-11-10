# Recursos avançados da API

Este documento cobre os recursos avançados da API: **chave de API**, **limite de requisições**, **idempotência**, **CORS** e **versionamento**. Para os endpoints, a paginação e os erros em geral, veja o [Guia da API](guia-da-api.md).

| Recurso | Cabeçalhos | Onde está no código |
|---|---|---|
| [Chave de API](#1-chave-de-api-x-api-key) | `X-API-Key` (envio); `WWW-Authenticate` (resposta 401) | `seguranca/ChaveApiFilter`, `seguranca/RegrasDeAcesso`, `service/ChaveApiService` |
| [Limite de requisições](#2-limite-de-requisições-rate-limiting) | `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `Retry-After` | `seguranca/LimiteDeRequisicoesFilter`, `seguranca/LimitadorDeRequisicoes` |
| [Idempotência](#3-idempotência-x-idempotency-key) | `X-Idempotency-Key` (envio); `X-Idempotency-Replayed` (resposta) | `seguranca/IdempotenciaFilter`, `seguranca/RegistrosDeIdempotencia` |
| [CORS](#4-cors) | `Access-Control-*` | `config/CorsConfig` |
| [Versionamento](#5-versionamento-x-api-version) | `X-API-Version` (envio e resposta) | `controller/LivroController`, `dto/LivroV2` |

## Ordem em que tudo acontece

Cada requisição passa por estes filtros, nesta ordem, antes de chegar ao controller (`seguranca/OrdemDosFiltros`):

```
requisição
   │
   ▼
1. CORS ─────────────────── origem não permitida? → 403 (só para navegadores)
   │
   ▼
2. Limite de requisições ── passou do limite?     → 429 + Retry-After
   │
   ▼
3. Chave de API ─────────── sem chave / inválida? → 401   nível insuficiente? → 403
   │
   ▼
4. Idempotência ─────────── POST repetido?        → devolve a resposta guardada
   │
   ▼
controller → service → banco
```

A ordem tem motivo: o **limite** vem antes da autenticação para contar também as tentativas com chave falsa ou senha errada; a **idempotência** vem depois da autenticação para nunca entregar uma resposta guardada a quem não tem permissão. Todos os erros saem no formato padrão (`ErroResposta`), com mensagem em português.

---

## 1. Chave de API (`X-API-Key`)

As rotas que leem dados pessoais ou alteram dados exigem uma chave de API no cabeçalho `X-API-Key`:

```bash
curl http://localhost:8080/usuarios -H "X-API-Key: bib_SUA_CHAVE_AQUI"
```

### Quais rotas exigem chave

As regras ficam num lugar só (`RegrasDeAcesso`), usado tanto pelo filtro quanto pelo Swagger (o cadeado 🔒 do Swagger mostra exatamente as rotas protegidas).

| Acesso | Rotas |
|---|---|
| **Público** (sem chave) | `GET` de `/livros`, `/autores` e `/categorias` (o catálogo), menos `GET /livros/{id}/emprestimos`; `GET /reservas/indisponiveis`; `POST /auth/login`; `POST /chaves-api`; `POST /usuarios` (autocadastro, veja abaixo); `PUT /usuarios/{id}/senha` (exige a senha atual); Swagger |
| **LEITURA** (clientes) | Ver e revogar as próprias chaves; reservar livros, ver e cancelar as próprias reservas (`/reservas`) |
| **ESCRITA** (equipe) | `GET` de `/usuarios`, `/carteirinhas`, `/emprestimos` e `/livros/{id}/emprestimos` (dados pessoais de todos); `POST` e `PUT` em todos os recursos |
| **ADMIN** | `DELETE` em todos os recursos; ver e revogar as chaves de todos |

Cada nível inclui os anteriores (ADMIN pode tudo que ESCRITA pode, e assim por diante).

| Situação | Resposta |
|---|---|
| Rota protegida sem `X-API-Key` | **401** e cabeçalho `WWW-Authenticate: ApiKey header="X-API-Key"` |
| Chave inexistente ou revogada (em qualquer rota, mesmo pública) | **401** |
| Chave válida, mas de nível menor que o exigido | **403** |

### Níveis por tipo de usuário

| Tipo de usuário | Nível máximo das chaves que pode gerar |
|---|---|
| `FUNCIONARIO` | ADMIN |
| `ALUNO`, `PROFESSOR`, `VISITANTE` | LEITURA |

Duas regras evitam que alguém aumente o próprio acesso:

- **Autocadastro:** `POST /usuarios` sem chave (ou com chave de LEITURA) só aceita os tipos `ALUNO` e `VISITANTE`. Funcionários e professores são cadastrados por quem tem chave de ESCRITA. Sem essa regra, qualquer um se cadastraria como `FUNCIONARIO` e geraria uma chave ADMIN.
- **Primeira senha:** definir a senha de um usuário que ainda não tem uma (`PUT /usuarios/{id}/senha` sem `senhaAtual`) exige chave de ESCRITA. Trocar uma senha existente continua público, porque exige a senha atual.

### A primeira chave (administrador)

Ao iniciar, a API cria uma chave ADMIN:

- se a variável de ambiente `BIBLIOTECA_CHAVE_ADMIN` estiver definida (mínimo de 16 caracteres), essa é a chave;
- se não estiver, a API gera uma chave aleatória e **mostra no log** (como o Spring Security faz com a senha padrão):

```text
Chave de API de administrador gerada para esta execução:

    bib_3kF9xQ2mVb7Lp0sT1uWcYd4eHg5iJk6lMn8oPq9rSt0
```

Como o banco é em memória, a chave gerada muda a cada início. Para usar sempre a mesma no seu computador:

```bash
# Linux/macOS
BIBLIOTECA_CHAVE_ADMIN=minha-chave-de-desenvolvimento mvn spring-boot:run
```

```cmd
:: Windows (CMD)
set BIBLIOTECA_CHAVE_ADMIN=minha-chave-de-desenvolvimento
mvn spring-boot:run
```

No IntelliJ: **Run → Edit Configurations → Environment variables**.

### A conta do dono (entrar no painel do site)

O cadastro público só cria `ALUNO` e `VISITANTE`, então a primeira conta de **dono** (tipo `FUNCIONARIO`) não pode ser criada pelo site. Para não depender da chave de administrador a cada reinício, a API cria essa conta sozinha ao iniciar quando estas variáveis de ambiente estão definidas:

| Variável | O que é |
|---|---|
| `BIBLIOTECA_DONO_EMAIL` | E-mail de entrada do dono |
| `BIBLIOTECA_DONO_SENHA` | Senha (8 a 72 caracteres). Só o hash BCrypt é guardado |
| `BIBLIOTECA_DONO_NOME` | Nome exibido (opcional; padrão "Dono da Biblioteca") |

```bash
BIBLIOTECA_DONO_EMAIL=dono@biblioteca.com BIBLIOTECA_DONO_SENHA=minha-senha-forte mvn spring-boot:run
```

Com isso, `/entrar` no site já leva o dono ao painel. CPF, telefone e endereço da conta são provisórios: ajuste em Usuários, Editar. Sem as variáveis, nada é criado, e a primeira conta de funcionário é cadastrada com a chave de administrador (`POST /usuarios`). Como o banco é em memória, a conta é recriada a cada início.

### Gerar e gerenciar chaves

| Método | Rota | O que faz | Acesso |
|---|---|---|---|
| POST | `/chaves-api` | Gera uma chave conferindo e-mail e senha do usuário | Público |
| GET | `/chaves-api` | Lista as chaves do dono da chave usada (ADMIN: todas) | LEITURA |
| GET | `/chaves-api/atual` | Mostra a chave usada na própria requisição | LEITURA |
| GET | `/chaves-api/{id}` | Busca uma chave própria (ADMIN: qualquer uma) | LEITURA |
| DELETE | `/chaves-api/{id}` | Revoga a chave: ela para de funcionar na hora | LEITURA (só as próprias; ADMIN: qualquer uma) |

```bash
# Gerar (o usuário precisa ter senha)
curl -X POST http://localhost:8080/chaves-api -H "Content-Type: application/json" -d '{
  "email": "funcionario@email.com", "senha": "minhaSenha123",
  "nome": "Postman do laboratório", "nivel": "ESCRITA"
}'
```

```json
{
  "id": 2,
  "chave": "bib_8Yc1...",
  "aviso": "Guarde esta chave agora: por segurança, ela não será mostrada novamente.",
  "nome": "Postman do laboratório",
  "prefixo": "bib_8Yc1Zt",
  "nivel": "ESCRITA",
  "usuarioId": 1,
  "criadaEm": "2026-11-06T10:15:30",
  "_links": { "self": {...}, "revogar": {...}, "chaves-api": {...}, "usuario": {...} }
}
```

**A chave completa só aparece nessa resposta.** O banco guarda apenas o **hash SHA-256** dela (como se faz com senhas) e o prefixo, que ajuda a reconhecê-la na listagem. Se perder a chave, revogue e gere outra.

---

## 2. Limite de requisições (rate limiting)

Cada cliente pode fazer um número limitado de requisições por minuto:

| Cliente | Como é identificado | Limite padrão |
|---|---|---|
| Com chave de API válida | Pela chave | 300 por minuto |
| Sem chave (ou com chave inválida) | Pelo endereço IP | 60 por minuto |

Toda resposta da API informa o limite e o uso:

```http
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 54
X-RateLimit-Reset: 38
```

| Cabeçalho | Significado |
|---|---|
| `X-RateLimit-Limit` | Requisições permitidas por minuto |
| `X-RateLimit-Remaining` | Quantas ainda restam na janela atual |
| `X-RateLimit-Reset` | Segundos até a contagem recomeçar |

Passando do limite, a resposta é **429 Too Many Requests**, com o cabeçalho `Retry-After` dizendo quantos segundos esperar:

```http
HTTP/1.1 429
Retry-After: 38
```

```json
{
  "status": 429,
  "erro": "Muitas requisições",
  "mensagem": "Limite de 60 requisições por minuto atingido. Tente novamente em 38 segundos."
}
```

**Como funciona:** janela fixa de 1 minuto por cliente. A primeira requisição abre a janela; quando ela termina, a contagem volta a zero. Uma chave falsa não abre uma contagem nova (continua contando pelo IP), e o login também é limitado, o que dificulta testar senhas sem parar. O Swagger e o `/v3/api-docs` ficam fora do limite.

Os limites ficam no `application.properties`:

```properties
biblioteca.limite.ativo=true
biblioteca.limite.requisicoes-por-minuto.sem-chave=60
biblioteca.limite.requisicoes-por-minuto.com-chave=300
```

---

## 3. Idempotência (`X-Idempotency-Key`)

**O problema:** o cliente envia um `POST`, a conexão cai antes da resposta e ele não sabe se o registro foi criado. Se reenviar, pode cadastrar o mesmo empréstimo duas vezes.

**A solução:** o cliente gera um identificador único para a operação (um UUID) e envia no cabeçalho `X-Idempotency-Key`. Se precisar reenviar, usa a mesma chave:

```bash
curl -X POST http://localhost:8080/emprestimos \
  -H "Content-Type: application/json" \
  -H "X-API-Key: bib_SUA_CHAVE" \
  -H "X-Idempotency-Key: 3f1c9a52-7d4e-4b8a-9c6f-2e5d8b1a0c47" \
  -d '{"dataEmprestimo":"2026-11-06","dataPrevistaDevolucao":"2026-11-20","status":"ATIVO","usuarioId":1,"livroId":1}'
```

| Situação | Resposta |
|---|---|
| Primeira vez com a chave | Processa normalmente. Se der certo (2xx), a resposta fica guardada por 24 horas |
| Mesma requisição de novo (mesma chave, mesmo endereço e mesmo corpo) | **A resposta guardada**, sem cadastrar de novo, com o cabeçalho `X-Idempotency-Replayed: true` |
| Mesma chave com outro corpo ou outro endereço | **422**: "Esta X-Idempotency-Key já foi usada com outra requisição" |
| Mesma chave enquanto a primeira ainda está sendo processada (ex.: clique duplo) | **409** |
| Chave em formato inválido (mais de 100 caracteres ou caracteres especiais) | **400** |

Detalhes:

- O cabeçalho é **opcional**: sem ele, o `POST` funciona como sempre.
- Respostas de **erro não são guardadas**: se o primeiro envio teve um erro de validação, o cliente pode corrigir os dados e reenviar com a mesma chave.
- As chaves são **separadas por cliente** (chave de API ou IP): chaves iguais de pessoas diferentes não se misturam.
- `POST /auth/login` e `POST /chaves-api` não usam idempotência (a resposta da segunda traz a chave de API em texto e não deve ficar guardada).
- A validade fica em `biblioteca.idempotencia.validade-horas` (padrão 24).

---

## 4. CORS

O CORS diz ao **navegador** quais sites podem chamar a API. Ele não protege a API de outros programas (curl, Postman, o app): para isso existe a chave de API.

| Item | Configuração |
|---|---|
| Origens permitidas | `http://localhost:*`, `http://127.0.0.1:*` e `http://192.168.*:*`, em `biblioteca.cors.origens-permitidas` (application.properties) |
| Métodos | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` |
| Cabeçalhos aceitos | `Content-Type`, `Accept`, `X-API-Key`, `X-Idempotency-Key`, `X-API-Version` |
| Cabeçalhos expostos ao JavaScript | `Location`, `Allow`, `WWW-Authenticate`, `X-RateLimit-*`, `Retry-After`, `X-Idempotency-Replayed`, `X-API-Version` |
| Cookies | Não (a API não usa cookies) |
| Cache da verificação prévia | 1 hora (`Access-Control-Max-Age: 3600`) |

Uma origem fora da lista, ou um método fora da lista (ex.: `PATCH`), é recusado com **403** na verificação prévia (`OPTIONS`).

O CORS é um **filtro com a maior prioridade**, e não a configuração do Spring MVC, porque precisa rodar antes dos outros filtros: assim até as respostas 401 e 429 levam os cabeçalhos de CORS, e o site consegue ler a mensagem de erro.

Em produção, troque as origens pelos endereços reais do site:

```properties
biblioteca.cors.origens-permitidas=https://biblioteca.exemplo.com.br
```

---

## 5. Versionamento (`X-API-Version`)

A versão é escolhida pelo cabeçalho `X-API-Version`, usando o versionamento nativo do Spring Framework 7 (`spring.mvc.apiversion.*` no `application.properties`).

| Cabeçalho enviado | Versão usada |
|---|---|
| (nenhum) | **1** (padrão: o site e o app continuam funcionando sem mudar nada) |
| `X-API-Version: 1` | 1 |
| `X-API-Version: 2` | 2 |
| Outro valor (`3`, `abc`...) | **400**: "A versão '3' não existe. Envie o cabeçalho X-API-Version com uma das versões: 1, 2" |

`GET /livros` e `GET /livros/{id}` têm duas versões. As demais rotas não têm versão própria e atendem às duas.

| | Versão 1 | Versão 2 (`LivroV2`) |
|---|---|---|
| `categoria` | Objeto completo (`{"id": 1, "nome": "Romance", ...}`) | Só o nome (`"Romance"`) |
| `autores` | Lista de objetos completos | Lista de nomes, em ordem alfabética |
| `quantidadeAutores` | — | Número de autores |
| Lista na listagem | `_embedded.livroList` | `_embedded.livros` |
| Links HATEOAS | `self`, `update`, `delete`, `categoria`, `autores`... | Os mesmos (os IDs continuam nos links) |

```bash
curl http://localhost:8080/livros/1 -H "X-API-Version: 2"
```

```json
{
  "id": 1,
  "titulo": "Dom Casmurro",
  "isbn": "9788535910663",
  "anoPublicacao": 1899,
  "editora": "Garnier",
  "numeroPaginas": 256,
  "categoria": "Romance",
  "autores": ["Machado de Assis"],
  "quantidadeAutores": 1,
  "_links": { "self": {...}, "categoria": {...}, "autores": {...} }
}
```

A resposta desses dois endpoints traz o cabeçalho `X-API-Version` com a versão usada.

No **Swagger**, há uma documentação para cada versão: escolha no seletor **Versão da API**, no topo da página. Os arquivos ficam em `/v3/api-docs/v1` e `/v3/api-docs/v2`.

---

## Testando

| Onde | Como |
|---|---|
| **Testes automatizados** | `mvn test`: `ChaveApiTest`, `LimiteDeRequisicoesTest`, `IdempotenciaTest`, `RegistrosDeIdempotenciaTest`, `CorsTest` e `VersionamentoTest` |
| **Postman** | Cole a chave de administrador na variável `apiKey` da coleção e rode: as pastas "Chaves de API", "Idempotência" e "Versionamento" testam os recursos |
| **Swagger** | Clique em **Authorize**, cole a chave e use **Try it out** |

## Limitações

- As contagens do limite de requisições e as respostas guardadas pela idempotência ficam **na memória** da API: somem ao reiniciar e, com mais de uma instância da API, cada uma teria as suas. Numa publicação real, elas iriam para um armazenamento compartilhado (como o Redis).
- Quem tem chave de ESCRITA ou ADMIN (só a equipe da biblioteca) vê os dados pessoais de **todos** os usuários, inclusive o CPF completo.
- Atrás de um proxy reverso, o IP visto pela API é o do proxy. Nesse caso, configure `server.forward-headers-strategy=native` para usar o IP real do cliente.

Veja também [Segurança e limitações](seguranca-e-limitacoes.md).

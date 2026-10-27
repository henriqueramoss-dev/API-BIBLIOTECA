# Arquitetura

## Visão geral

```
 Site (React) ─┐                                       ┌─ Swagger UI (/swagger-ui.html)
               ├─ HTTP/JSON ─►  Controller ─► Service ─► Repository ─► H2 (memória)
 App (Expo)  ──┘                    │            │
                                 Assembler    Regras de negócio
                                 (links HATEOAS)
```

Os dois front-ends não têm banco nem regras próprias: tudo vem da API.

## API (`biblioteca-api`)

Pacote base: `com.senac.biblioteca`.

| Pacote | Responsabilidade |
|---|---|
| `controller` | Recebe as requisições HTTP, valida a entrada e devolve a resposta. Uma classe por recurso, com a documentação Swagger de cada rota (`@Operation`, `@ApiResponses`) |
| `service` | Regras de negócio: confere se as categorias, autores, usuários e livros referenciados existem, atualiza os campos e decide quando devolver "não encontrado" |
| `repository` | Acesso ao banco (Spring Data JPA). Tem as consultas personalizadas, como `findByTituloContainingIgnoreCase`, e carrega os relacionamentos junto (`@EntityGraph`) |
| `entity` | Classes mapeadas para tabelas (JPA), com as validações (Bean Validation) |
| `dto` | Objetos de entrada que usam IDs no lugar de objetos (`LivroRequest`, `PerfilRequest`, `EmprestimoRequest`) e o formato dos erros (`ErroResposta`) |
| `enums` | `StatusEmprestimo`, `TipoUsuario` e `StatusLivro` (sem uso) |
| `assembler` | Transforma cada entidade em `EntityModel` com os links HATEOAS |
| `exception` | Exceções do projeto e o `ApiExceptionHandler`, que converte cada erro em uma resposta padronizada |
| `config` | Configurações: CORS, paginação (`PaginacaoConfig`, `Paginas`), Swagger e tema do Swagger |
| `validation` | Validação própria de CPF (`@Cpf`), com os dígitos verificadores |

### Caminho de uma requisição

`POST /livros`, por exemplo:

1. **PaginacaoConfig** (interceptador) confere `page` e `size`, se vierem na URL e a rota usar `Pageable`.
2. **LivroController** recebe o corpo e valida o `LivroRequest` (`@Valid`).
3. **LivroService** busca a categoria e os autores pelos IDs (se algum não existir, lança `RecursoRelacionadoNaoEncontradoException`) e monta o `Livro`.
4. **LivroRepository** salva (se o ISBN já existe, o banco recusa e vira erro 409).
5. **LivroModelAssembler** adiciona os links e o controller responde **201** com o cabeçalho `Location`.

Qualquer exceção no meio do caminho cai no **ApiExceptionHandler**, que responde no formato `ErroResposta` (veja o [Guia da API](guia-da-api.md#erros)).

### Pontos de atenção do código

| Assunto | Como é feito |
|---|---|
| **Paginação** | Todas as listagens recebem `Pageable` (padrão 10, máximo 100, definido em `application.properties`). `PaginacaoConfig` recusa `page` e `size` inválidos (400) só nas rotas que usam `Pageable`, com as mesmas regras de conversão do Spring Data. O controller chama `Paginas.conferir(...)` depois da consulta para responder 404 quando a página pedida passou da última |
| **Erros** | Um único `ApiExceptionHandler` trata validação, regras de negócio, JSON inválido, parâmetros, integridade do banco (409), não encontrado (404), método não aceito (405, com o cabeçalho `Allow`), tipo de conteúdo e `Accept` não aceitos (415 e 406), endereço inexistente (404) e erros inesperados (500). Todos usam o formato `ErroResposta`. As mensagens de "não encontrado" ficam em `RecursoNaoEncontradoException` |
| **HATEOAS** | Um assembler por entidade. Respostas únicas usam `EntityModel`; listagens usam `PagedModel` |
| **Swagger** | Anotações nos controllers, informações gerais em `OpenApiConfig` e o tema da Biblioteca em `resources/static/swagger/` (`index.css`, `index.html`, `favicon.svg`), ligado por `SwaggerUiConfig`. `OpenApiConfig` também acrescenta ao Swagger o formato dos erros e os limites de paginação |
| **CORS** | `CorsConfig` libera `localhost`, `127.0.0.1` e `192.168.*`, nos métodos GET, POST, PUT, DELETE e OPTIONS |
| **Banco** | H2 em memória, criado com `ddl-auto=create-drop`; sem scripts de carga inicial |

### Versões

Java 21, Spring Boot 4.1.2, Springdoc 3.2.0, Maven. As dependências estão em `biblioteca-api/pom.xml`.

### Testes

| Tipo | Onde | O que cobre |
|---|---|---|
| **Integração (Java)** | `biblioteca-api/src/test/java/.../BibliotecaApiTest.java` | 13 testes que sobem a API de verdade (porta aleatória, H2 em memória) e chamam os endpoints por HTTP: os arquivos do Swagger carregam, o 405 traz `Allow`, os erros 415 e 406 usam o formato padrão, os limites de paginação, as regras do empréstimo, o código da categoria em maiúsculas e a validação e a exposição do CPF |
| **Coleção Postman** | `biblioteca-api/postman/` | 60 requisições com testes de status: CRUD das 6 entidades, consultas, rotas de navegação e erros 400, 404, 405 e 409 |

Para rodar os testes em Java: `cd biblioteca-api && mvn test`.

## Site (`biblioteca-web`)

React 18 + Vite + TypeScript, com React Router.

```
src/
  main.tsx               ponto de entrada (endereço da API e roteador)
  App.tsx                menu lateral e rotas
  api/client.ts          cliente HTTP: listar, buscar, criar, editar, excluir; lê a paginação HATEOAS e os erros da API
  api/resources.ts       descrição de cada entidade: campos, busca, colunas, validação
  pages/Dashboard.tsx    tela Início
  pages/ResourceList.tsx listagem genérica (busca, filtro, paginação, exclusão)
  pages/ResourceForm.tsx formulário genérico de cadastro e edição
  components/            ícones e carregamento das opções dos campos de seleção
  styles.css             visual, com tema claro e escuro e versão para celular
```

**Como as telas nascem:** as listagens e os formulários não são escritos entidade por entidade. Eles são montados a partir da descrição em `api/resources.ts`. Para criar um campo novo, basta descrevê-lo ali.

## App (`REACT-NATIVE`)

React Native + Expo (SDK 54) com Expo Router (cada arquivo da pasta `app/` é uma tela).

```
app/                 telas: abertura, login, criar conta, painel, listagens, formulário, detalhes do livro
components/          lista, campos de formulário, seletor de opções, etiqueta de status
services/
  biblioteca-api.ts  cliente HTTP (paginação HATEOAS, erros da API)
  resources.ts       descrição de cada entidade (a mesma ideia do site)
contexts/            tema claro/escuro
```

O site e o app usam o mesmo visual (azul-marinho e fundo creme) e descrições de entidades equivalentes: `resources.ts` é praticamente o mesmo arquivo nos dois.

## Como o mesmo dado aparece em cada parte

Exemplo, o campo **CPF** do usuário:

| Camada | Onde |
|---|---|
| Banco e validação | `entity/Usuario.java` (`@Pattern`, `unique`) |
| Documentação | `@Schema` na entidade, que aparece no Swagger |
| Site | `biblioteca-web/src/api/resources.ts` |
| App | `services/resources.ts` e a tela `cadastro.tsx` |
| Postman | corpos de `Criar usuario` |

Ao mudar um campo, lembre de atualizar todos esses lugares.

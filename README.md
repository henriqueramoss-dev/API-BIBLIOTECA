# API de Biblioteca - Gestão de Acervo e Empréstimos

Projeto acadêmico desenvolvido para a disciplina de Desenvolvimento de APIs com Spring Boot.

## 1. Objetivo do projeto

Desenvolver uma API REST para o gerenciamento de uma biblioteca, aplicando os conceitos estudados em Spring Boot, persistência de dados com JPA, banco de dados H2, validações, paginação, documentação com Swagger e HATEOAS.

O projeto contempla **somente os Requisitos Técnicos - Parte 1** da atividade.

---

## 2. Tecnologias utilizadas

- Java 21
- Spring Boot 4.1.2 (versão estável mais recente)
- Maven
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Bean Validation
- Spring HATEOAS
- Springdoc OpenAPI / Swagger

---

## 3. Estrutura do projeto

O projeto está organizado de forma simples e objetiva.

```text
com.senac.biblioteca
│
├── BibliotecaApplication.java
├── assembler
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
└── service
```

Responsabilidade de cada pacote:

- `entity`: representa as tabelas do banco de dados;
- `repository`: realiza o acesso aos dados com Spring Data JPA;
- `service`: concentra as regras de negócio e valida os relacionamentos;
- `controller`: recebe as requisições HTTP e disponibiliza os endpoints REST;
- `dto`: dados de entrada que referenciam outros registros por ID (livro, perfil e empréstimo);
- `assembler`: monta as respostas HATEOAS (`EntityModel`) com os links de cada recurso;
- `enums`: armazena valores fixos utilizados pelas entidades;
- `exception`: tratamento de erros da API;
- `config`: configurações gerais, como Swagger e CORS.

---

## 4. Entidades do sistema

A API será composta pelas seguintes entidades. Cada uma possui pelo menos 5 dados cadastrais próprios (além do `id` e dos relacionamentos). Campos marcados com * são obrigatórios.

### Usuario

Representa o usuário cadastrado na biblioteca.

Principais dados:

- id
- nome *
- email * (único)
- cpf * (11 dígitos, único)
- matricula
- tipo * (`ALUNO`, `PROFESSOR`, `FUNCIONARIO` ou `VISITANTE`)

### Perfil

Armazena informações complementares do usuário.

Principais dados:

- id
- telefone *
- endereco *
- cidade *
- estado * (sigla com 2 letras, ex.: SP)
- cep * (8 dígitos)
- data de nascimento *
- usuario * (relacionamento)

### Autor

Representa o autor de um ou mais livros.

Principais dados:

- id
- nome *
- nacionalidade *
- data de nascimento
- gênero literário
- biografia

### Categoria

Representa a categoria de um livro.

Principais dados:

- id
- nome *
- codigo * (único, ex.: ROM)
- descricao
- localizacao (ex.: Estante A)
- publico alvo

Exemplos:

- Romance
- Tecnologia
- Ficção
- História

### Livro

Representa um livro cadastrado na biblioteca.

Principais dados:

- id
- titulo *
- isbn * (único)
- ano de publicação *
- editora *
- número de páginas *
- categoria * (relacionamento)
- autores (relacionamento)

### Emprestimo

Representa o empréstimo de um livro para um usuário.

Principais dados:

- id
- data do empréstimo *
- data prevista para devolução *
- data de devolução
- status *
- observacao
- usuario * (relacionamento)
- livro * (relacionamento)

---

## 5. Relacionamentos

O projeto utiliza os três tipos de relacionamento exigidos na atividade.

### One-to-One

```text
Usuario 1 -------- 1 Perfil
```

Cada usuário possui um único perfil e cada perfil pertence a um único usuário.

### One-to-Many

```text
Categoria 1 -------- N Livro
```

Uma categoria pode possuir vários livros, mas cada livro pertence a uma categoria.

Também existem os relacionamentos:

```text
Usuario 1 -------- N Emprestimo
Livro   1 -------- N Emprestimo
```

### Many-to-Many

```text
Livro N -------- N Autor
```

Um livro pode possuir vários autores e um autor pode participar de vários livros.

---

## 6. Enum

O projeto utiliza enum para representar valores fixos.

Exemplo:

```java
public enum StatusEmprestimo {
    ATIVO,
    DEVOLVIDO,
    ATRASADO
}
```

O enum evita o uso de textos livres para representar o status do empréstimo.

---

## 7. Validações

Os dados recebidos pela API serão validados utilizando Bean Validation.

Exemplos de validações utilizadas:

```java
@NotBlank
@Email
@Size
@NotNull
```

Exemplo:

```java
@NotBlank
private String nome;

@Email
@NotBlank
private String email;
```

Caso os dados enviados não sejam válidos, a API deverá retornar um erro HTTP adequado.

---

## 8. Operações CRUD

Cada entidade possuirá operações CRUD completas.

### Criar

```http
POST /usuarios
```

### Listar

```http
GET /usuarios
```

### Buscar por ID

```http
GET /usuarios/{id}
```

### Atualizar

```http
PUT /usuarios/{id}
```

### Excluir

```http
DELETE /usuarios/{id}
```

Além do CRUD, cada entidade terá pelo menos uma consulta personalizada.

Exemplo:

```http
GET /usuarios/buscar?nome=Henrique
```

Endpoints por entidade:

| Entidade | Endpoints | Consultas personalizadas |
|---|---:|---|
| Livros | 7 | `GET /livros/buscar?titulo=`, `GET /livros/{id}/emprestimos` |
| Autores | 7 | `GET /autores/buscar?nome=`, `GET /autores/{id}/livros` |
| Categorias | 7 | `GET /categorias/buscar?nome=`, `GET /categorias/{id}/livros` |
| Usuários | 7 | `GET /usuarios/buscar?nome=`, `GET /usuarios/{id}/emprestimos` |
| Perfis | 6 | `GET /perfis/buscar?telefone=` |
| Empréstimos | 6 | `GET /emprestimos/buscar?status=` |

O `POST` retorna `201 Created` com o endereço do novo registro no cabeçalho `Location`.

---

## 9. Paginação

Todas as rotas de listagem utilizarão `Pageable`.

Exemplo:

```http
GET /livros?page=0&size=10
```

Também poderá ser utilizado ordenamento:

```http
GET /livros?page=0&size=10&sort=titulo,asc
```

Limites da paginação (configurados em `config/PaginacaoConfig.java`):

| Situação | Resposta |
|---|---|
| Sem `page`/`size` | Página 0 com 10 registros |
| `size` entre 1 e 100 | Normal |
| `size` acima de 100, menor que 1 ou que não é número | `400` — "O tamanho da página (size) deve ser entre 1 e 100. Valor informado: 100000." |
| `page` negativa ou que não é número | `400` — "A página (page) deve ser um número a partir de 0." |
| `page` depois da última página | `404` — "A página 100000 não existe. Esta listagem tem 1 página (de 0 a 0)." |

Uma listagem sem nenhum registro continua respondendo `200` na página 0.

---

## 10. Códigos HTTP

A API utilizará códigos HTTP adequados para cada operação.

| Situação | Código |
|---|---:|
| Requisição realizada com sucesso | 200 OK |
| Recurso criado | 201 Created |
| Exclusão realizada | 204 No Content |
| Dados inválidos, ID relacionado inexistente, parâmetro ausente ou ordenação inválida | 400 Bad Request |
| Registro ou endereço não encontrado | 404 Not Found |
| Valor duplicado (e-mail, ISBN, perfil) ou registro em uso ao excluir | 409 Conflict |
| Erro interno | 500 Internal Server Error |

Todos os erros voltam com uma mensagem explicando o problema, no mesmo formato.
Exemplo de `GET /livros/999` quando o livro não existe:

```json
{
  "timestamp": "2026-10-27T16:53:52.564",
  "status": 404,
  "erro": "Não encontrado",
  "mensagem": "Livro com ID 999 não encontrado."
}
```

---

## 11. Swagger / OpenAPI

A API utiliza **Springdoc OpenAPI** para gerar a documentação interativa dos endpoints.

A configuração geral está na classe:

```text
config/OpenApiConfig.java
```

Ela define:

- título da API;
- versão;
- descrição geral do projeto.

Além disso, a documentação será feita diretamente nos controllers usando:

```java
@Operation
@ApiResponses
@ApiResponse
```

Os campos das entidades utilizam `@Schema` para fornecer descrições e exemplos.

A interface do Swagger tem um tema próprio, com a mesma identidade visual do site
(azul-marinho, fundo creme e os mesmos ícones): a apresentação da API imita uma ficha
catalográfica, cada grupo de endpoints tem o ícone da entidade e cada endpoint lembra
a lombada de um livro, com a cor do método HTTP. Os arquivos ficam em
`resources/static/swagger/` (`index.css`, `index.html` e `favicon.svg`) e são ligados ao
Swagger pela classe `config/SwaggerUiConfig.java`.

Exemplo:

```java
@Schema(
    description = "Nome do usuário",
    example = "Henrique Ramos"
)
private String nome;
```

Cada endpoint deverá informar:

- objetivo da operação;
- descrição;
- parâmetros;
- exemplo quando necessário;
- possíveis códigos HTTP de resposta.

Exemplo de documentação de endpoint:

```java
@Operation(
    summary = "Lista todos os usuários",
    description = "Retorna os usuários cadastrados de forma paginada."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso"),
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
})
```

Depois que os controllers forem implementados, todas as rotas poderão ser visualizadas em:

```text
http://localhost:8080/swagger-ui.html
```

---

## 12. HATEOAS

A API utilizará Spring HATEOAS para incluir links relacionados nas respostas.

Exemplo simplificado:

```json
{
  "id": 1,
  "nome": "Henrique",
  "_links": {
    "self": {
      "href": "/usuarios/1"
    },
    "update": {
      "href": "/usuarios/1"
    },
    "delete": {
      "href": "/usuarios/1"
    }
  }
}
```

Além de `self`, `update`, `delete` e do link para a listagem, cada recurso
traz links para os recursos relacionados, permitindo navegar pela API:

| Recurso | Links relacionados |
|---|---|
| Livro | `categoria`, `autores`, `emprestimos` (histórico do livro) |
| Empréstimo | `usuario`, `livro` |
| Perfil | `usuario` |
| Usuário | `perfil` (quando existir), `emprestimos` |
| Autor | `livros` |
| Categoria | `livros` |

Assim é possível navegar nos dois sentidos: do livro para a categoria e da categoria
para os seus livros, do empréstimo para o usuário e do usuário para os seus empréstimos.

Exemplo de um empréstimo:

```json
"_links": {
  "self":        { "href": "http://localhost:8080/emprestimos/1" },
  "update":      { "href": "http://localhost:8080/emprestimos/1" },
  "delete":      { "href": "http://localhost:8080/emprestimos/1" },
  "emprestimos": { "href": "http://localhost:8080/emprestimos" },
  "usuario":     { "href": "http://localhost:8080/usuarios/1" },
  "livro":       { "href": "http://localhost:8080/livros/1" }
}
```

São utilizados os recursos:

- `EntityModel`
- `PagedModel`

---

## 13. Banco de dados H2

O projeto utiliza o banco H2 em memória.

Configuração principal:

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca
spring.datasource.username=sa
spring.datasource.password=
```

Console do H2:

> **O console web do H2 não está disponível neste projeto.** A partir do Spring Boot 4 ele saiu do pacote padrão e fica no módulo separado `spring-boot-h2console`, que não está no `pom.xml` (por isso `http://localhost:8080/h2-console` responde 404, apesar das linhas `spring.h2.console.*` do `application.properties`). Para consultar os dados, use a própria API (Swagger ou Postman).
>
> Para habilitá-lo apenas em testes locais, adicione ao `pom.xml` a dependência `org.springframework.boot:spring-boot-h2console` e reinicie a API. Dados de acesso: JDBC URL `jdbc:h2:mem:biblioteca`, usuário `sa`, senha vazia. Não deixe o console habilitado fora do seu computador: ele dá acesso direto ao banco.

---

## 14. Como executar

Requisitos:

- Java 21
- IntelliJ IDEA ou outra IDE Java
- Maven

Passos:

1. Abrir a pasta `biblioteca-api` no IntelliJ.
2. Configurar o JDK 21.
3. Aguardar o Maven baixar as dependências.
4. Executar a classe:

```text
BibliotecaApplication.java
```

Quando a aplicação iniciar corretamente, será exibida uma mensagem semelhante a:

```text
Started BibliotecaApplication
```

A API estará disponível em:

```text
http://localhost:8080
```

---

## 15. Checklist dos requisitos da Parte 1

| Requisito | Implementação no projeto |
|---|---|
| Spring Boot (versão mais recente) | 4.1.2 |
| Java 17 ou superior | Java 21 |
| Maven ou Gradle | Maven |
| H2 | Sim |
| Spring Data JPA | Sim |
| Mínimo de 5 entidades | 6 entidades |
| One-to-One | Usuario x Perfil |
| One-to-Many | Categoria x Livro / Usuario x Emprestimo / Livro x Emprestimo |
| Many-to-Many | Livro x Autor |
| Bean Validation | Sim |
| Enum | StatusEmprestimo (Emprestimo) e TipoUsuario (Usuario) |
| CRUD completo | Sim, para todas as entidades |
| 5 endpoints por entidade | 6 ou 7 por entidade (40 no total) |
| Pageable | Sim, em todas as listagens e consultas |
| Consulta personalizada | Pelo menos uma por entidade |
| Status HTTP | 200, 201 (com `Location`), 204, 400, 404, 405 (com `Allow`), 406, 409, 415 e 500, todos com mensagem |
| Swagger/OpenAPI | Todos os endpoints com descrição, exemplos e códigos de resposta (inclusive o formato dos erros) |
| HATEOAS | `EntityModel` e `PagedModel`, links `self`, `update`, `delete` e de navegação entre recursos |

---

## 16. Escopo

Este projeto está limitado aos requisitos da **Parte 1** da atividade.

Não fazem parte desta etapa:

- autenticação por API Key;
- idempotência;
- rate limiting;
- versionamento da API;
- recursos avançados previstos para etapas futuras.


---

## 17. Front-end web

A pasta `biblioteca-web` contém as telas web da biblioteca, feitas com **React + Vite + TypeScript**, que consomem esta API.

Telas disponíveis:

- **Início**: totais de cada cadastro, empréstimos por status e empréstimos recentes;
- **Listagem** de empréstimos, livros, autores, categorias, usuários e perfis, com busca (endpoints `/buscar`), paginação e exclusão;
- **Formulário** de cadastro e edição de cada entidade, com validação dos campos e exibição dos erros devolvidos pela API.

Como executar (com a API já rodando em `http://localhost:8080`):

```bash
cd biblioteca-web
npm install
npm run dev
```

O site abre em `http://localhost:5173`. Para usar outro endereço da API, copie `.env.example` para `.env` e altere `VITE_API_URL`.

A classe `config/CorsConfig.java` libera o acesso à API a partir de `localhost` e da rede local (`192.168.*`), necessário para o navegador aceitar as requisições do front-end.

O aplicativo mobile (React Native / Expo) está no repositório [REACT-NATIVE](https://github.com/henriqueramoss-dev/REACT-NATIVE).

---

## Documentação, termos e privacidade

| Documento | Conteúdo |
|---|---|
| [Documentação do projeto](docs/README.md) | Índice com os guias abaixo |
| [Guia de instalação](docs/guia-de-instalacao.md) | Como rodar a API, o site e o app |
| [Guia da API](docs/guia-da-api.md) | Endpoints, paginação, erros e exemplos |
| [Modelo de dados](docs/modelo-de-dados.md) | Entidades, campos e regras |
| [Arquitetura](docs/arquitetura.md) | Como o código está organizado |
| [Segurança e limitações](docs/seguranca-e-limitacoes.md) | O que falta para uso real |
| [Termos de Serviço](biblioteca-api/TERMS_OF_SERVICE.md) | Regras de uso do sistema |
| [Política de Privacidade](biblioteca-api/PRIVACY_POLICY.md) | Quais dados são tratados e os direitos do titular (LGPD) |

## Termos de uso

Esta API foi desenvolvida para fins acadêmicos no curso de Sistemas para Internet. A versão completa está nos [Termos de Serviço](biblioteca-api/TERMS_OF_SERVICE.md).

Condições de uso:

- utilizar o projeto para estudo, demonstração e avaliação acadêmica;
- utilizar somente dados fictícios durante os testes;
- não utilizar a aplicação em produção sem revisão adicional de segurança e persistência;
- manter a identificação do projeto quando o código for utilizado como referência acadêmica.

A documentação interativa da API está disponível, durante a execução, em:

```text
http://localhost:8080/swagger-ui.html
```

# API de Biblioteca - Gestão de Acervo e Empréstimos

Projeto acadêmico desenvolvido para a disciplina de Desenvolvimento de APIs com Spring Boot.

## 1. Objetivo do projeto

Desenvolver uma API REST para o gerenciamento de uma biblioteca, aplicando os conceitos estudados em Spring Boot, persistência de dados com JPA, banco de dados H2, validações, paginação, documentação com Swagger e HATEOAS.

O projeto contempla **somente os Requisitos Técnicos - Parte 1** da atividade.

---

## 2. Tecnologias utilizadas

- Java 21
- Spring Boot
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

A API será composta pelas seguintes entidades:

### Usuario

Representa o usuário cadastrado na biblioteca.

Principais dados:

- id
- nome
- email

### Perfil

Armazena informações complementares do usuário.

Principais dados:

- id
- telefone
- endereco
- data de nascimento

### Autor

Representa o autor de um ou mais livros.

Principais dados:

- id
- nome

### Categoria

Representa a categoria de um livro.

Exemplos:

- Romance
- Tecnologia
- Ficção
- História

### Livro

Representa um livro cadastrado na biblioteca.

Principais dados:

- id
- titulo
- isbn
- ano de publicação
- categoria
- autores

### Emprestimo

Representa o empréstimo de um livro para um usuário.

Principais dados:

- id
- usuario
- livro
- data do empréstimo
- data prevista para devolução
- status

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

---

## 10. Códigos HTTP

A API utilizará códigos HTTP adequados para cada operação.

| Situação | Código |
|---|---:|
| Requisição realizada com sucesso | 200 OK |
| Recurso criado | 201 Created |
| Exclusão realizada | 204 No Content |
| Dados inválidos, ID relacionado inexistente, parâmetro ausente ou ordenação inválida | 400 Bad Request |
| Recurso não encontrado | 404 Not Found |
| Valor duplicado (e-mail, ISBN, perfil) ou registro em uso ao excluir | 409 Conflict |
| Erro interno | 500 Internal Server Error |

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
| Livro | `categoria`, `autores` |
| Empréstimo | `usuario`, `livro` |
| Perfil | `usuario` |
| Usuário | `perfil` (quando existir) |

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

```text
http://localhost:8080/h2-console
```

Dados de acesso:

```text
JDBC URL: jdbc:h2:mem:biblioteca
User Name: sa
Password: vazio
```

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
| Spring Boot | Sim |
| Java 17 ou superior | Java 21 |
| Maven ou Gradle | Maven |
| H2 | Sim |
| Spring Data JPA | Sim |
| Mínimo de 5 entidades | 6 entidades |
| One-to-One | Usuario x Perfil |
| One-to-Many | Categoria x Livro / Usuario x Emprestimo |
| Many-to-Many | Livro x Autor |
| Bean Validation | Sim |
| Enum | StatusEmprestimo |
| CRUD completo | Sim, para todas as entidades |
| 5 endpoints por entidade | Sim |
| Pageable | Sim |
| Consulta personalizada | Uma por entidade |
| Status HTTP | Sim |
| Swagger/OpenAPI | Sim |
| HATEOAS | Sim |

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

## Termos de uso

Esta API foi desenvolvida para fins acadêmicos no curso de Sistemas para Internet.

Condições de uso:

- utilizar o projeto para estudo, demonstração e avaliação acadêmica;
- utilizar somente dados fictícios durante os testes;
- não utilizar a aplicação em produção sem revisão adicional de segurança e persistência;
- manter a identificação do projeto quando o código for utilizado como referência acadêmica.

A documentação interativa da API está disponível, durante a execução, em:

```text
http://localhost:8080/swagger-ui.html
```

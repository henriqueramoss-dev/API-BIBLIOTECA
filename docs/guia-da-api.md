# Guia da API

A API é REST, fala **JSON** e roda por padrão em `http://localhost:8080`. A documentação interativa (Swagger) está em <http://localhost:8080/swagger-ui.html>. Este guia resume o que o Swagger mostra e explica as regras gerais.

## Convenções

| Item | Como é |
|---|---|
| Formato | `Content-Type: application/json` nas requisições com corpo |
| Datas | `AAAA-MM-DD` (ex.: `2026-10-27`) |
| Identificadores | Números inteiros gerados pela API (`id`); o `id` enviado no corpo é ignorado na criação |
| Relacionamentos na entrada | Por **ID**: `categoriaId`, `autoresIds`, `usuarioId`, `livroId` |
| Relacionamentos na saída | Objetos completos dentro do registro, mais links HATEOAS |
| Autenticação | Nenhuma (veja [Segurança e limitações](seguranca-e-limitacoes.md)) |

## Endpoints

São **40 endpoints** em 6 recursos. Todo recurso tem o CRUD completo (listar, buscar por ID, criar, atualizar, excluir) e uma consulta personalizada (`/buscar`). Livros, autores, categorias e usuários também têm uma rota para navegar até os registros relacionados.

| Método | Rota | O que faz | Respostas |
|---|---|---|---|
| **Livros** | | | |
| GET | `/livros` | Listar livros | 200, 400, 404 |
| POST | `/livros` | Cadastrar livro | 201, 400, 409 |
| GET | `/livros/buscar?titulo=` | Buscar por título | 200, 400, 404 |
| GET | `/livros/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/livros/{id}` | Atualizar | 200, 400, 404, 409 |
| DELETE | `/livros/{id}` | Excluir | 204, 404, 409 |
| GET | `/livros/{id}/emprestimos` | Histórico de empréstimos do livro | 200, 400, 404 |
| **Autores** | | | |
| GET | `/autores` | Listar autores | 200, 400, 404 |
| POST | `/autores` | Cadastrar autor | 201, 400 |
| GET | `/autores/buscar?nome=` | Buscar por nome | 200, 400, 404 |
| GET | `/autores/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/autores/{id}` | Atualizar | 200, 400, 404 |
| DELETE | `/autores/{id}` | Excluir | 204, 404, 409 |
| GET | `/autores/{id}/livros` | Livros do autor | 200, 400, 404 |
| **Categorias** | | | |
| GET | `/categorias` | Listar categorias | 200, 400, 404 |
| POST | `/categorias` | Cadastrar categoria | 201, 400 |
| GET | `/categorias/buscar?nome=` | Buscar por nome | 200, 400, 404 |
| GET | `/categorias/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/categorias/{id}` | Atualizar | 200, 400, 404 |
| DELETE | `/categorias/{id}` | Excluir | 204, 404, 409 |
| GET | `/categorias/{id}/livros` | Livros da categoria | 200, 400, 404 |
| **Usuários** | | | |
| GET | `/usuarios` | Listar usuários | 200, 400, 404 |
| POST | `/usuarios` | Cadastrar usuário | 201, 400, 409 |
| GET | `/usuarios/buscar?nome=` | Buscar por nome | 200, 400, 404 |
| GET | `/usuarios/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/usuarios/{id}` | Atualizar | 200, 400, 404, 409 |
| DELETE | `/usuarios/{id}` | Excluir (apaga também o perfil) | 204, 404, 409 |
| GET | `/usuarios/{id}/emprestimos` | Empréstimos do usuário | 200, 400, 404 |
| **Perfis** | | | |
| GET | `/perfis` | Listar perfis | 200, 400, 404 |
| POST | `/perfis` | Cadastrar perfil | 201, 400, 409 |
| GET | `/perfis/buscar?telefone=` | Buscar por telefone | 200, 400, 404 |
| GET | `/perfis/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/perfis/{id}` | Atualizar | 200, 400, 404, 409 |
| DELETE | `/perfis/{id}` | Excluir | 204, 404 |
| **Empréstimos** | | | |
| GET | `/emprestimos` | Listar empréstimos | 200, 400, 404 |
| POST | `/emprestimos` | Cadastrar empréstimo | 201, 400 |
| GET | `/emprestimos/buscar?status=` | Filtrar por status (`ATIVO`, `DEVOLVIDO`, `ATRASADO`) | 200, 400, 404 |
| GET | `/emprestimos/{id}` | Buscar por ID | 200, 400, 404 |
| PUT | `/emprestimos/{id}` | Atualizar | 200, 400, 404 |
| DELETE | `/emprestimos/{id}` | Excluir | 204, 404 |

Nas listagens, o **404** significa "a página pedida não existe" (veja a seção de paginação). Nas rotas com `{id}`, significa "registro não encontrado".

## Paginação e ordenação

Todas as listagens, buscas e rotas de navegação são **paginadas**.

| Parâmetro | Padrão | Regra |
|---|---|---|
| `page` | `0` | Número da página, começando em 0 |
| `size` | `10` | De 1 a **100** registros por página |
| `sort` | `id,asc` | `campo,asc` ou `campo,desc` (ex.: `titulo,desc`) |

```bash
curl "http://localhost:8080/livros?page=0&size=5&sort=titulo,asc"
```

| O que acontece | Resposta |
|---|---|
| `size` fora de 1–100 ou que não é número | **400** |
| `page` negativa ou que não é número | **400** |
| `page` depois da última página | **404**, dizendo quantas páginas existem |
| `sort` por um campo que não existe | **400** |
| Listagem sem nenhum registro | **200**, página 0 vazia |

Formato da resposta (HATEOAS, `PagedModel`):

```json
{
  "_embedded": { "livroList": [ { "...": "um livro por item" } ] },
  "_links": {
    "first": { "href": "http://localhost:8080/livros?page=0&size=2&sort=id,asc" },
    "self":  { "href": "http://localhost:8080/livros?page=0&size=2&sort=id,asc" },
    "next":  { "href": "http://localhost:8080/livros?page=1&size=2&sort=id,asc" },
    "last":  { "href": "http://localhost:8080/livros?page=1&size=2&sort=id,asc" }
  },
  "page": { "number": 0, "size": 2, "totalElements": 4, "totalPages": 2 }
}
```

A chave dentro de `_embedded` é o nome do recurso mais `List`: `livroList`, `autorList`, `categoriaList`, `usuarioList`, `perfilList` e `emprestimoList`.

## Links (HATEOAS)

Cada registro traz `_links` para as operações e para os recursos relacionados:

| Recurso | Links, além de `self`, `update`, `delete` e o link da listagem |
|---|---|
| Livro | `categoria`, `autores`, `emprestimos` (histórico do livro) |
| Autor | `livros` |
| Categoria | `livros` |
| Usuário | `perfil` (quando existe), `emprestimos` |
| Perfil | `usuario` |
| Empréstimo | `usuario`, `livro` |

Exemplo, `GET /categorias/4`:

```json
{
  "id": 4,
  "nome": "Ficção",
  "codigo": "FIC",
  "descricao": "Ficção científica e fantasia",
  "localizacao": "Estante D",
  "publicoAlvo": "Juvenil",
  "_links": {
    "self":       { "href": "http://localhost:8080/categorias/4" },
    "update":     { "href": "http://localhost:8080/categorias/4" },
    "delete":     { "href": "http://localhost:8080/categorias/4" },
    "livros":     { "href": "http://localhost:8080/categorias/4/livros" },
    "categorias": { "href": "http://localhost:8080/categorias" }
  }
}
```

## Erros

Todos os erros têm o mesmo formato, com uma mensagem em português:

```json
{
  "timestamp": "2026-10-27T16:53:52.564",
  "status": 404,
  "erro": "Não encontrado",
  "mensagem": "Livro com ID 999 não encontrado."
}
```

| Código | Quando | Exemplo de `mensagem` |
|---|---|---|
| **400** | Campos inválidos | `Um ou mais campos possuem valores inválidos.` (acompanha o objeto `campos`) |
| **400** | JSON malformado ou valor de tipo errado (ex.: status inexistente) | `O corpo da requisição está malformado ou contém um valor incompatível com o tipo esperado.` |
| **400** | Parâmetro de URL inválido ou ausente | `O parâmetro 'titulo' é obrigatório.` |
| **400** | Ordenação por um campo que não existe | `O campo 'campoInexistente' não pode ser usado para ordenação.` |
| **400** | Paginação fora dos limites | `O tamanho da página (size) deve ser entre 1 e 100. Valor informado: 100000.` |
| **400** | ID de categoria, autor, usuário ou livro inexistente ao cadastrar | `Categoria com ID 99 não encontrado(a).` |
| **404** | Registro não encontrado | `Livro com ID 999 não encontrado.` |
| **404** | Página que não existe | `A página 100000 não existe. Esta listagem tem 1 página (de 0 a 0).` |
| **404** | Endereço que não existe | `O endereço '/livross' não existe nesta API.` |
| **400** | Dados que se contradizem (regras do empréstimo) | `Informe a data de devolução quando o status for DEVOLVIDO.` (acompanha o objeto `campos`) |
| **405** | Método HTTP não aceito (a resposta traz o cabeçalho `Allow` com os métodos aceitos) | `O método PATCH não é aceito neste endereço. Métodos aceitos: DELETE, GET, PUT.` |
| **406** | Cabeçalho `Accept` que a API não atende | `A API responde em application/json (ou HAL+JSON). Ajuste o cabeçalho Accept.` |
| **415** | `Content-Type` que a API não aceita | `Envie o corpo da requisição como application/json.` |
| **409** | Valor repetido ou registro em uso | `A operação não pôde ser concluída porque existe um dado duplicado ou um relacionamento que impede a alteração.` |
| **500** | Erro inesperado | `Ocorreu um erro inesperado ao processar a requisição.` |

Erro de validação (**400**), com os problemas por campo:

```json
{
  "timestamp": "2026-10-27T17:40:07.131",
  "status": 400,
  "erro": "Dados inválidos",
  "mensagem": "Um ou mais campos possuem valores inválidos.",
  "campos": {
    "nacionalidade": "A nacionalidade é obrigatória",
    "nome": "O nome é obrigatório"
  }
}
```

## Exemplos

Os exemplos usam `curl`. A ordem importa: um livro precisa de uma categoria, um empréstimo precisa de um livro e de um usuário, e assim por diante.

### Regras que a API confere além do formato

| Recurso | Regra | Erro |
|---|---|---|
| Usuário | O **CPF** precisa ter 11 dígitos, sem pontos ou traço, **e os dígitos verificadores corretos**. Sequências como `00000000000` e `11111111111` são recusadas | 400, campo `cpf` |
| Categoria | O **código** é gravado em maiúsculas e sem espaços nas pontas (`rom` vira `ROM`), então `ROM`, `rom` e `Rom` são o mesmo código | 409 se já existir |
| Empréstimo | `dataPrevistaDevolucao` e `dataDevolucao` não podem ser anteriores a `dataEmprestimo` | 400, campo da data |
| Empréstimo | Status `DEVOLVIDO` exige `dataDevolucao` | 400, campo `dataDevolucao` |
| Empréstimo | Só o status `DEVOLVIDO` pode ter `dataDevolucao` | 400, campo `status` |

> **CPF nos exemplos.** `12345678909` é um CPF de teste com dígitos corretos. Nunca cadastre o CPF de uma pessoa real.
>
> **Onde o CPF aparece.** Ele é devolvido pelas rotas de `/usuarios`, mas **não** dentro de empréstimos, perfis e demais respostas em que o usuário vem aninhado.

### 1. Cadastrar uma categoria e um autor

```bash
curl -X POST http://localhost:8080/categorias -H "Content-Type: application/json" -d '{
  "nome": "Romance", "codigo": "ROM", "descricao": "Narrativas de ficção em prosa",
  "localizacao": "Estante A", "publicoAlvo": "Adulto"
}'

curl -X POST http://localhost:8080/autores -H "Content-Type: application/json" -d '{
  "nome": "Machado de Assis", "nacionalidade": "Brasileira",
  "dataNascimento": "1839-06-21", "generoLiterario": "Romance"
}'
```

A resposta é **201 Created**, com o endereço do novo registro no cabeçalho `Location`. O código da categoria volta em maiúsculas.

### 2. Cadastrar um livro (usa os IDs acima)

```bash
curl -X POST http://localhost:8080/livros -H "Content-Type: application/json" -d '{
  "titulo": "Dom Casmurro", "isbn": "9788535910663", "anoPublicacao": 1899,
  "editora": "Penguin-Companhia", "numeroPaginas": 256,
  "categoriaId": 1, "autoresIds": [1]
}'
```

### 3. Cadastrar um usuário, o perfil dele e um empréstimo

```bash
curl -X POST http://localhost:8080/usuarios -H "Content-Type: application/json" -d '{
  "nome": "Henrique Ramos", "email": "henrique@email.com",
  "cpf": "12345678909", "matricula": "2026001", "tipo": "ALUNO"
}'

curl -X POST http://localhost:8080/perfis -H "Content-Type: application/json" -d '{
  "telefone": "11999990000", "endereco": "Rua das Flores, 10",
  "cidade": "São Paulo", "estado": "SP", "cep": "01001000",
  "dataNascimento": "2000-05-10", "usuarioId": 1
}'

curl -X POST http://localhost:8080/emprestimos -H "Content-Type: application/json" -d '{
  "dataEmprestimo": "2026-10-20", "dataPrevistaDevolucao": "2026-11-03",
  "status": "ATIVO", "usuarioId": 1, "livroId": 1
}'
```

### 4. Consultar

```bash
curl "http://localhost:8080/livros/buscar?titulo=dom"              # busca por parte do título
curl "http://localhost:8080/emprestimos/buscar?status=ATIVO"       # empréstimos ativos
curl "http://localhost:8080/categorias/1/livros"                   # livros de uma categoria
curl "http://localhost:8080/usuarios/1/emprestimos"                # empréstimos de um usuário
```

### 5. Devolver um livro (atualizar o empréstimo)

O `PUT` substitui os campos do registro, então envie o corpo completo:

```bash
curl -X PUT http://localhost:8080/emprestimos/1 -H "Content-Type: application/json" -d '{
  "dataEmprestimo": "2026-10-20", "dataPrevistaDevolucao": "2026-11-03",
  "dataDevolucao": "2026-11-01", "status": "DEVOLVIDO",
  "usuarioId": 1, "livroId": 1
}'
```

### 6. Excluir

```bash
curl -X DELETE http://localhost:8080/emprestimos/1     # 204 No Content
```

**Ordem de exclusão:** um registro em uso não pode ser excluído (erro 409). Exclua primeiro quem depende dele: empréstimo → livro → autor e categoria → usuário (que leva o perfil junto).

## Campos e regras de cada recurso

A lista completa de campos, tipos e validações está em [Modelo de dados](modelo-de-dados.md).

# Termos de Serviço — Biblioteca

**Última atualização:** 27 de outubro de 2026

Estes termos valem para o uso do sistema **Biblioteca — Gestão de Acervo e Empréstimos**, formado pela **API** (`biblioteca-api`), pelo **site** (`biblioteca-web`) e pelo **aplicativo** mobile. Ao usar qualquer uma dessas partes, você concorda com o que está escrito aqui.

## 1. Finalidade

A Biblioteca é um **projeto acadêmico** (curso de Sistemas para Internet, Senac), criado para demonstrar a construção de uma API REST com Java e Spring Boot e de telas que a consomem.

O sistema permite cadastrar, consultar, atualizar e excluir **usuários, perfis, autores, categorias, livros e empréstimos**.

## 2. Quem pode usar

O projeto é voltado a estudantes, professores e avaliadores. Qualquer pessoa que consiga acessar a API consegue usar o sistema, porque as rotas ainda não exigem token (veja a seção 7).

## 3. Uso permitido

Você pode usar o sistema para:

- estudo e atividades acadêmicas;
- testes de endpoints REST, com o Swagger ou o Postman;
- demonstrações de CRUD, paginação, validação, Swagger/OpenAPI e HATEOAS;
- aprendizado de Spring Boot, JPA, React e React Native;
- avaliação do projeto.

## 4. Uso não permitido

Você **não deve**:

- cadastrar **dados pessoais reais** de ninguém, nem dados confidenciais ou sensíveis;
- usar o sistema em **produção** ou para controlar uma biblioteca de verdade sem revisão de segurança e de persistência;
- tentar acessar dados de outras pessoas, sobrecarregar a API ou burlar as validações;
- usar o sistema para fins ilegais ou que prejudiquem terceiros;
- apresentar o projeto como seu sem manter a identificação do autor.

## 5. Dados cadastrados

- **Use somente dados fictícios.** CPF, e-mail, telefone, endereço e datas de nascimento devem ser inventados. A API só aceita CPFs com dígitos verificadores corretos, e um CPF assim pode pertencer a uma pessoa de verdade: use CPFs de teste (como `12345678909`) e nunca o de alguém real.
- Quem cadastra um dado é **responsável** por ele e por ter o direito de usá-lo.
- O tratamento de dados pessoais está descrito na [Política de Privacidade](PRIVACY_POLICY.md).

## 6. Regras do sistema

Estas regras são aplicadas pela API e valem para o site e para o app:

| Regra | Efeito |
|---|---|
| E-mail, CPF, ISBN e código de categoria **não podem se repetir** | A API recusa o cadastro com erro 409 |
| Cada usuário tem **no máximo um perfil** | Um segundo perfil para o mesmo usuário é recusado (409) |
| O **CPF** deve ter 11 dígitos, sem pontos ou traço, e dígitos verificadores corretos; o **CEP**, 8 dígitos; o **estado**, a sigla com 2 letras | Valores inválidos são recusados (400) |
| O **código da categoria** é gravado em maiúsculas | `rom` e `ROM` são o mesmo código |
| Em um **empréstimo**, as datas de devolução não podem ser anteriores à data do empréstimo; o status `DEVOLVIDO` exige a data de devolução e só ele pode tê-la | Dados que se contradizem são recusados (400) |
| Registros **em uso não podem ser excluídos** | Um livro com empréstimos, uma categoria ou um autor com livros e um usuário com empréstimos não podem ser excluídos (409). Excluir um usuário apaga também o perfil dele |
| As listagens têm **no máximo 100 registros por página** | Valores maiores são recusados (400); páginas que não existem retornam 404 |
| O status de um empréstimo é **ativo, devolvido ou atrasado** | O status não muda sozinho com o passar do tempo: quem registra escolhe o valor |

## 7. Login e acesso

"Criar conta" cadastra um usuário com **senha** (de 8 a 72 caracteres), e a tela "Entrar" confere e-mail e senha. A senha pode ser trocada na tela **Configurações** do app. A API guarda apenas o hash da senha. Guarde bem a sua senha: não há recuperação por e-mail.

Mas a API **ainda não emite token**: depois do login, as demais rotas continuam abertas a qualquer um que alcance a API. Isso significa que **não existe controle de quem pode ler ou alterar os dados**. Não reutilize aqui uma senha que você usa em outros serviços. Por isso o sistema só deve rodar em ambiente local e de testes.

## 8. Ambiente de execução

O sistema foi projetado para rodar **localmente**:

| Parte | Endereço |
|---|---|
| API | `http://localhost:8080` |
| Documentação (Swagger) | `http://localhost:8080/swagger-ui.html` |
| Site | `http://localhost:5173` |

A API usa o banco **H2 em memória**: **todos os dados cadastrados são apagados quando a API é encerrada**.

## 9. Disponibilidade e suporte

Por ser um projeto acadêmico, **não há garantia** de disponibilidade contínua, de suporte, de correção de erros em prazo definido nem de manutenção. O sistema é fornecido **"como está"**.

## 10. Limitação de responsabilidade

O autor não se responsabiliza por perdas de dados, falhas, uso indevido ou prejuízos decorrentes do uso do sistema, em especial se ele for usado em desacordo com estes termos (por exemplo, com dados reais ou em produção). O uso é por conta e risco de quem usa.

## 11. Propriedade intelectual

O código-fonte e a documentação estão publicados para fins acadêmicos, sob a condição **"Uso acadêmico — Projeto Senac"**: podem ser lidos, executados e usados como referência de estudo, mantendo-se a identificação do autor. O projeto não vem acompanhado de uma licença de código aberto formal.

Bibliotecas de terceiros (Spring Boot, React, Expo etc.) seguem as licenças dos seus próprios autores.

## 12. Alterações nos termos

Estes termos podem ser alterados quando o sistema mudar. A data de atualização fica no topo e o histórico fica no controle de versões do repositório (Git). Continuar a usar o sistema depois de uma alteração significa que você aceita a nova versão.

## 13. Lei aplicável

Estes termos seguem a legislação brasileira, em especial a Lei Geral de Proteção de Dados (Lei nº 13.709/2018).

## 14. Contato e código-fonte

Projeto acadêmico desenvolvido por **Henrique Ramos**.

- Código-fonte: <https://github.com/henriqueramoss-dev/API-DE-JAVA->
- Dúvidas ou pedidos sobre dados: <https://github.com/henriqueramoss-dev/API-DE-JAVA-/issues>

## 15. Documentos relacionados

- [Política de Privacidade](PRIVACY_POLICY.md)
- [Documentação do projeto](../docs/README.md)

> Este documento foi elaborado para um projeto acadêmico e não substitui a avaliação de um profissional de direito.

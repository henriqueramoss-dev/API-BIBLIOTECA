# Política de Privacidade — Biblioteca

**Última atualização:** 27 de outubro de 2026

Esta política explica quais dados pessoais o sistema **Biblioteca** trata, para quê, por quanto tempo e quais são os seus direitos, conforme a Lei Geral de Proteção de Dados (LGPD — Lei nº 13.709/2018).

O sistema é formado por três partes, todas cobertas por este documento:

| Parte | O que é |
|---|---|
| **API** (`biblioteca-api`) | API REST em Java/Spring Boot que guarda os dados |
| **Site** (`biblioteca-web`) | Telas web em React |
| **Aplicativo** | App em React Native / Expo |

> **Aviso importante.** A Biblioteca é um **projeto acadêmico** (curso de Sistemas para Internet, Senac). Ela foi feita para estudo e demonstração e **não deve ser usada com dados pessoais reais**. Use sempre dados fictícios. Os motivos estão na seção 8.

---

## 1. Quem é o responsável

| | |
|---|---|
| **Responsável pelo projeto** | Henrique Ramos |
| **Natureza** | Projeto acadêmico, sem fins comerciais |
| **Contato** | Pelo repositório: <https://github.com/henriqueramoss-dev/API-DE-JAVA-/issues> |
| **Encarregado (DPO)** | Não designado, por se tratar de projeto acadêmico |

Se você rodar o sistema no seu próprio computador, **você** é quem controla os dados cadastrados: eles ficam somente na sua máquina.

---

## 2. Quais dados são tratados

Todos os dados abaixo são **informados por você** ao cadastrar um registro, no site, no app ou diretamente na API. O sistema não coleta nenhum dado por conta própria.

### Usuário

| Dado | Obrigatório | Observação |
|---|---|---|
| Nome | Sim | |
| E-mail | Sim | Não pode se repetir |
| CPF | Sim | 11 dígitos com dígitos verificadores corretos; não pode se repetir |
| Matrícula | Não | |
| Tipo (aluno, professor, funcionário ou visitante) | Sim | |

### Perfil (complemento do usuário)

| Dado | Obrigatório |
|---|---|
| Telefone | Sim |
| Endereço, cidade, estado e CEP | Sim |
| Data de nascimento | Sim |

### Empréstimos

Guarda **qual usuário pegou qual livro**, as datas de empréstimo, de devolução prevista e de devolução, o status e uma observação opcional. O conjunto forma o **histórico de leitura** do usuário.

### Autores, categorias e livros

Dados do acervo (nome, nacionalidade, data de nascimento e biografia do autor; título, ISBN, editora etc. do livro). Em geral são informações públicas sobre obras, mas a data de nascimento e a biografia de um autor podem ser dados pessoais se ele for uma pessoa viva.

### Dados sensíveis

O sistema **não pede** dados pessoais sensíveis (origem racial ou étnica, religião, opinião política, saúde, vida sexual, dados genéticos ou biométricos). Não os informe nos campos livres (biografia, observação).

---

## 3. O que o sistema NÃO coleta

- **Cookies:** nenhum.
- **Armazenamento no navegador ou no celular** (`localStorage`, `sessionStorage` etc.): nenhum. Ao fechar o site ou o app, nada fica guardado neles.
- **Ferramentas de análise, publicidade, rastreamento ou relatórios de erro** de terceiros: nenhuma.
- **Localização, contatos, câmera, microfone ou fotos:** o app não pede nenhuma dessas permissões.
- **Login e senha:** a API ainda **não tem autenticação**. A tela "Entrar" é demonstrativa e **não guarda nem confere senha**; "Criar conta" apenas cadastra um usuário e o seu perfil na API.

---

## 4. Para que os dados são usados

| Finalidade | Dados |
|---|---|
| Cadastrar leitores e controlar empréstimos de livros | Usuário, perfil e empréstimos |
| Demonstrar o projeto em atividades e avaliações acadêmicas | Todos (fictícios) |
| Evitar cadastros duplicados | E-mail, CPF, ISBN e código da categoria |

Os dados **não são usados** para publicidade, perfilamento, venda, decisões automatizadas ou qualquer outra finalidade diferente das acima.

**Base legal (LGPD, art. 7º, I):** consentimento de quem preenche o cadastro. Ao informar os dados você concorda com o uso descrito nesta política; você pode retirar o consentimento a qualquer momento excluindo o registro (seção 7).

---

## 5. Com quem os dados são compartilhados

**Com ninguém.** Os dados vão do site ou do app direto para a API e ficam nela. Eles não são enviados a servidores do projeto, nem a terceiros, nem vendidos.

Há **um único contato com terceiro**, e ele **não envolve dados cadastrados**:

| Quem | Quando | O que o terceiro recebe |
|---|---|---|
| Google Fonts (`fonts.googleapis.com`) | Ao abrir a documentação da API (`/swagger-ui.html`), que carrega fontes de lá | O endereço IP e o navegador de quem abriu a página, como em qualquer carregamento de fonte externa |

O site e o app não fazem esse carregamento.

Se quem executa o sistema decidir publicá-lo na internet (hospedar a API e o banco em um serviço de nuvem), esse serviço passará a hospedar os dados, e este documento precisará ser revisto.

---

## 6. Onde os dados ficam e por quanto tempo

- Os dados ficam em um banco **H2 em memória**, dentro da própria API, na máquina em que ela está rodando.
- **Quando a API é encerrada, todos os dados são apagados.** Não há cópia de segurança.
- O app e o site **não guardam cópia** dos dados.
- Se o responsável mudar a configuração para um banco em arquivo ou em outro servidor, os dados passam a ficar guardados até serem excluídos.

Não há transferência internacional de dados enquanto o sistema roda localmente.

---

## 7. Seus direitos (LGPD, art. 18)

Você pode, a qualquer momento:

| Direito | Como exercer no sistema |
|---|---|
| Confirmar que existem dados seus | Procure pelo nome no site/app ou use `GET /usuarios/buscar?nome=` |
| Acessar os dados | Abra o registro (`GET /usuarios/{id}` e `GET /perfis/{id}`) |
| Corrigir dados incompletos ou errados | Edite o registro no site/app ou use `PUT` |
| Excluir os dados (eliminação e retirada do consentimento) | Botão de excluir no site/app ou `DELETE /usuarios/{id}`. Excluir o usuário apaga também o perfil dele. Se houver empréstimos, exclua-os antes (`DELETE /emprestimos/{id}`) |
| Receber os dados em formato estruturado (portabilidade) | As respostas da API já são JSON |
| Saber com quem os dados são compartilhados | Seção 5 deste documento |
| Pedir anonimização ou bloqueio de dados desnecessários | Edite ou exclua o registro |
| Reclamar à Autoridade Nacional de Proteção de Dados (ANPD) | <https://www.gov.br/anpd> |

Se você não tem acesso ao sistema (por exemplo, seus dados foram cadastrados por outra pessoa), peça pelo contato da seção 1. Como o banco fica em memória, basta encerrar a API para que tudo seja apagado.

---

## 8. Segurança

O sistema tem as seguintes proteções:

- validação de todos os campos recebidos (Bean Validation);
- acesso do navegador limitado a `localhost` e à rede local (`192.168.*`);
- mensagens de erro que não revelam detalhes internos.

E tem **limitações que impedem o uso com dados reais**:

| Limitação | Consequência |
|---|---|
| Sem autenticação nem permissões | Quem alcançar a API consegue ler, alterar e excluir tudo |
| Tráfego em HTTP, sem criptografia | Os dados podem ser lidos por quem estiver na mesma rede |
| CPF e demais dados guardados sem criptografia | Quem acessar o banco lê os dados |
| O CPF completo é devolvido pelas rotas de `/usuarios` | Quem alcançar a API lê o CPF de todos os usuários. Ele não é repetido dentro de empréstimos e perfis |
| Se o console do H2 for habilitado (hoje ele **não** está disponível, veja o README) | Quem tiver acesso à API conseguiria consultar o banco diretamente |
| Sem limite de requisições | Não há proteção contra uso abusivo |

Veja `docs/seguranca-e-limitacoes.md` para o que seria necessário antes de uma publicação real.

**Em caso de incidente** (vazamento ou acesso indevido), o responsável avisará as pessoas afetadas e a ANPD quando a lei exigir.

---

## 9. Crianças e adolescentes

O sistema não é direcionado a menores de 18 anos. Como ele pede a data de nascimento, um menor só deve ser cadastrado com o consentimento específico de um responsável (LGPD, art. 14).

---

## 10. Alterações desta política

Esta política pode mudar quando o sistema mudar. A data de atualização fica no topo e o histórico fica no controle de versões do repositório (Git).

---

## 11. Documentos relacionados

- [Termos de Serviço](TERMS_OF_SERVICE.md)
- [Documentação do projeto](../docs/README.md)
- [Limitações de segurança](../docs/seguranca-e-limitacoes.md)

> Este documento foi elaborado para um projeto acadêmico e não substitui a avaliação de um profissional de direito. Antes de usar o sistema com dados reais, ele precisa ser revisado.

# Segurança e limitações

A Biblioteca é um **projeto acadêmico**. Este documento diz com clareza o que ele **não** faz, para ninguém usá-lo por engano com dados reais.

## Resumo

**Não use com dados pessoais reais e não publique na internet sem resolver os itens abaixo.**

## O que existe hoje

| Proteção | Onde |
|---|---|
| Validação de todos os campos recebidos (CPF com dígitos verificadores, CEP, UF, e-mail, tamanhos, obrigatoriedade) e regras entre campos do empréstimo | Bean Validation, `@Cpf` e `EmprestimoService` |
| Valores únicos garantidos pelo banco (e-mail, CPF, ISBN, código da categoria, um perfil por usuário). O código da categoria é gravado em maiúsculas para a regra não ser contornada | Restrições `unique` |
| CPF devolvido só pelas rotas de `/usuarios` (não dentro de empréstimos e perfis) | `@JsonIgnoreProperties` |
| Limite de 100 registros por página | `PaginacaoConfig` |
| Erros padronizados, sem mostrar detalhes internos (o erro 500 só diz que houve um erro inesperado) | `ApiExceptionHandler` |
| Navegador só acessa a API a partir de `localhost`, `127.0.0.1` e `192.168.*` | `CorsConfig` |

## O que NÃO existe

| Limitação | Risco | O que seria preciso |
|---|---|---|
| **Sem autenticação nem permissões** | Quem alcançar a API lê, altera e exclui tudo. A tela "Entrar" do app é demonstrativa e aceita qualquer senha | Login (por exemplo, Spring Security com JWT), senha guardada com hash (BCrypt), perfis de acesso (aluno × bibliotecário) |
| **Tráfego em HTTP puro** | Os dados podem ser lidos por quem estiver na mesma rede | HTTPS (certificado) |
| **Dados sem criptografia no banco** (CPF, telefone, endereço) | Quem acessar o banco lê tudo | Banco com acesso restrito e, para os campos mais sensíveis, criptografia |
| **Banco em memória** | Os dados somem ao reiniciar a API | Um banco persistente (PostgreSQL, MySQL) com cópias de segurança |
| **Sem limite de requisições** | Uso abusivo ou sobrecarga | Rate limiting |
| **CORS não protege a API** | O CORS só controla navegadores; qualquer programa (curl, Postman, o app) acessa a API sem restrição | Autenticação |
| **Sem registro de acessos (auditoria)** | Não se sabe quem alterou o quê | Logs de auditoria |
| **Poucos testes automatizados** | Há 13 testes de integração e a coleção do Postman, mas não há testes de unidade nem medição de cobertura | Mais testes, rodando a cada alteração (CI) |
| **CPF visível para qualquer um** | As rotas de `/usuarios` devolvem o CPF completo de todos os usuários, e a API não exige login | Autenticação e, se possível, não devolver o CPF (ou devolver mascarado) |
| **CPF de teste pode ser de gente real** | A API só aceita CPFs com dígitos verificadores corretos, e um CPF assim pode pertencer a uma pessoa de verdade | Usar sempre CPFs de teste (ex.: `12345678909`) |
| **Status do empréstimo manual** | Um empréstimo vencido não vira `ATRASADO` sozinho | Tarefa agendada que atualiza o status |
| **Fonte externa no Swagger** | Ao abrir `/swagger-ui.html`, o navegador pede fontes ao Google Fonts (o IP de quem abre vai ao Google) | Hospedar as fontes dentro do projeto |

## Console do H2

O `application.properties` tem `spring.h2.console.enabled=true`, mas **o console não responde** (`/h2-console` dá 404) porque, no Spring Boot 4, ele fica em um módulo separado que o projeto não inclui. Isso é bom: o console dá acesso direto a todos os dados do banco.

Se você habilitar o módulo para depurar, faça só no seu computador e remova depois.

## Antes de uma publicação real

Checklist mínimo:

- [ ] Autenticação e controle de permissões na API
- [ ] HTTPS
- [ ] Banco persistente, com acesso restrito e cópias de segurança
- [ ] Revisão do que é guardado: precisa mesmo de CPF, endereço e data de nascimento? Guardar menos dados é mais seguro
- [ ] Criptografia dos dados pessoais mais sensíveis
- [ ] CORS restrito aos endereços reais do site
- [ ] Limite de requisições
- [ ] Console do H2 desligado
- [ ] Fontes e demais arquivos hospedados no próprio projeto
- [ ] Mais testes automatizados, rodando a cada alteração
- [ ] Decidir se a API precisa devolver o CPF completo
- [ ] Política de Privacidade e Termos revisados por um profissional de direito (os atuais foram feitos para um projeto acadêmico)

## Como relatar um problema de segurança

Abra uma issue no repositório, sem colar dados pessoais ou credenciais: <https://github.com/henriqueramoss-dev/API-DE-JAVA-/issues>

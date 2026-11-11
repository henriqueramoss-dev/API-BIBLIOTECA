# Segurança e limitações

A Biblioteca é um **projeto acadêmico**. Este documento diz com clareza o que ele **não** faz, para ninguém usá-lo por engano com dados reais.

## Resumo

**Não use com dados pessoais reais e não publique na internet sem resolver os itens abaixo.**

## O que existe hoje

| Proteção | Onde |
|---|---|
| Validação de todos os campos recebidos (CPF com dígitos verificadores, CEP, UF, e-mail, tamanhos, obrigatoriedade) e regras entre campos do empréstimo | Bean Validation, `@Cpf` e `EmprestimoService` |
| Valores únicos garantidos pelo banco (e-mail, CPF, ISBN, código da categoria, uma carteirinha por usuário). O código da categoria é gravado em maiúsculas para a regra não ser contornada | Restrições `unique` |
| CPF e demais dados pessoais devolvidos só pelas rotas de `/usuarios` (não dentro de empréstimos e carteirinhas) | `@JsonIgnoreProperties` |
| Limite de 100 registros por página | `PaginacaoConfig` |
| Erros padronizados, sem mostrar detalhes internos (o erro 500 só diz que houve um erro inesperado) | `ApiExceptionHandler` |
| Navegador só acessa a API a partir de `localhost`, `127.0.0.1` e `192.168.*` (configurável), com métodos e cabeçalhos listados um a um | `CorsConfig` |
| Chave de API (`X-API-Key`) com níveis LEITURA (cliente: só o que é dele e as reservas), ESCRITA e ADMIN (equipe: dados de todos e alterações). O banco guarda só o hash SHA-256 das chaves, e elas podem ser revogadas | `ChaveApiFilter`, `RegrasDeAcesso`, `ChaveApiService` |
| Autocadastro limitado a `ALUNO` e `VISITANTE`; só funcionários geram chaves de ESCRITA ou ADMIN; definir a primeira senha de alguém exige chave de ESCRITA | `UsuarioService`, `ChaveApiService`, `AutenticacaoService` |
| Limite de requisições por minuto (por chave ou por IP), que também limita as tentativas de login | `LimiteDeRequisicoesFilter` |
| Reenvio seguro de `POST` com `X-Idempotency-Key` (evita cadastros duplicados) | `IdempotenciaFilter` |

## O que NÃO existe

| Limitação | Risco | O que seria preciso |
|---|---|---|
| **A chave do catálogo é pública** | Fica no código do site e do app, e qualquer pessoa pode usá-la para ler livros, autores e categorias. É de propósito: o acervo não é sensível. Ela não lê dados pessoais, não grava e não reserva | Se o acervo fosse restrito, visitantes teriam de ter conta |
| **A equipe vê os dados de todos** | Quem tem chave de ESCRITA ou ADMIN (só funcionários) consulta os dados pessoais de todos os usuários, carteirinhas e empréstimos. Clientes (chave de LEITURA) não veem dados de outras pessoas | Níveis mais finos (ex.: separar quem só atende no balcão de quem administra) |
| **Chave de administrador inicial** | Sem `BIBLIOTECA_CHAVE_ADMIN`, a chave ADMIN é gerada a cada início e aparece no log; quem lê o log tem acesso total | Definir a chave por variável de ambiente, guardada num cofre de segredos, e restringir o acesso aos logs |
| **Chaves sem prazo de validade** | Uma chave vale até ser revogada | Prazo de validade e renovação |
| **Limite de tentativas de login só por minuto** | O limite de requisições por IP dificulta, mas não bloqueia, quem testa senhas devagar | Bloqueio temporário da conta após várias falhas |
| **Sessão guardada no aparelho** | O app guarda no aparelho o `id`, o nome e o e-mail de quem entrou (não a senha). Para acessar as rotas protegidas, o app precisa gerar uma chave de API (`POST /chaves-api`) e guardá-la | Guardar a chave em armazenamento seguro do aparelho (ex.: `expo-secure-store`) |
| **Tráfego em HTTP puro** | Os dados podem ser lidos por quem estiver na mesma rede | HTTPS (certificado) |
| **Dados sem criptografia no banco** (CPF, telefone, endereço) | Quem acessar o banco lê tudo | Banco com acesso restrito e, para os campos mais sensíveis, criptografia |
| **Banco em memória** | Os dados somem ao reiniciar a API | Um banco persistente (PostgreSQL, MySQL) com cópias de segurança |
| **Limite de requisições e idempotência na memória** | As contagens e as respostas guardadas somem ao reiniciar; com mais de uma instância da API, cada uma teria as suas | Guardar num lugar compartilhado (ex.: Redis) |
| **CORS não protege a API** | O CORS só controla navegadores; qualquer programa (curl, Postman, o app) acessa as rotas públicas sem restrição | A chave de API protege as demais rotas |
| **Sem registro de acessos (auditoria)** | Não se sabe quem alterou o quê | Logs de auditoria |
| **Poucos testes de unidade** | Há 68 testes de integração, 5 de unidade e a coleção do Postman, rodando no CI, mas não há medição de cobertura | Mais testes de unidade e medição de cobertura |
| **CPF completo para a equipe** | As rotas de `/usuarios` devolvem o CPF completo para chaves de ESCRITA e ADMIN | Devolver o CPF mascarado quando o nome já basta |
| **CPF de teste pode ser de gente real** | A API só aceita CPFs com dígitos verificadores corretos, e um CPF assim pode pertencer a uma pessoa de verdade | Usar sempre CPFs de teste (ex.: `12345678909`) |
| **Status do empréstimo manual** | Um empréstimo vencido não vira `ATRASADO` sozinho | Tarefa agendada que atualiza o status |
| **Capas pelo Open Library** | Ao mostrar a capa de um livro, o navegador ou o app pede a imagem a `covers.openlibrary.org`, que recebe o ISBN e o IP de quem vê a lista | Desligar as capas reais (app: Configurações; site: `VITE_CAPAS_REAIS=false`) ou guardar as imagens na própria API |
| **Fonte externa no Swagger** | Ao abrir `/swagger-ui.html`, o navegador pede fontes ao Google Fonts (o IP de quem abre vai ao Google) | Hospedar as fontes dentro do projeto |

## Console do H2

O `application.properties` tem `spring.h2.console.enabled=true`, mas **o console não responde** (`/h2-console` dá 404) porque, no Spring Boot 4, ele fica em um módulo separado que o projeto não inclui. Isso é bom: o console dá acesso direto a todos os dados do banco.

Se você habilitar o módulo para depurar, faça só no seu computador e remova depois.

## Antes de uma publicação real

Checklist mínimo:

- [x] Chave de acesso e níveis de permissão na API (`X-API-Key`)
- [ ] Filtrar os dados pessoais pelo dono da chave
- [ ] Definir a chave de administrador por variável de ambiente (`BIBLIOTECA_CHAVE_ADMIN`) guardada num cofre de segredos
- [ ] HTTPS
- [ ] Banco persistente, com acesso restrito e cópias de segurança
- [ ] Revisão do que é guardado: precisa mesmo de CPF, endereço e data de nascimento? Guardar menos dados é mais seguro
- [ ] Criptografia dos dados pessoais mais sensíveis
- [ ] CORS restrito aos endereços reais do site
- [x] Limite de requisições (em memória; com várias instâncias, usar um armazenamento compartilhado)
- [ ] Console do H2 desligado
- [ ] Fontes e demais arquivos hospedados no próprio projeto
- [ ] Mais testes automatizados, rodando a cada alteração
- [ ] Decidir se a API precisa devolver o CPF completo
- [ ] Política de Privacidade e Termos revisados por um profissional de direito (os atuais foram feitos para um projeto acadêmico)

## Como relatar um problema de segurança

Abra uma issue no repositório, sem colar dados pessoais ou credenciais: <https://github.com/henriqueramoss-dev/API-DE-JAVA-/issues>

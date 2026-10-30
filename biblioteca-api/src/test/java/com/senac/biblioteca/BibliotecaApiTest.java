package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração: sobem a API de verdade (porta aleatória, banco H2 em memória) e chamam
 * os endpoints por HTTP, como o site, o app e o Postman fazem.
 *
 * Cada teste cria os próprios dados, com valores únicos, porque o banco é compartilhado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BibliotecaApiTest {

    @Value("${local.server.port}")
    private int porta;

    private final HttpClient http = HttpClient.newHttpClient();

    // ---------------------------------------------------------------- utilitários

    private record Resposta(int status, String corpo, HttpResponse<String> http) {
        String cabecalho(String nome) {
            return http.headers().firstValue(nome).orElse("");
        }
    }

    private Resposta chamar(String metodo, String caminho, String corpo, String... cabecalhos) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        req.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo));
        if (corpo != null) {
            req.header("Content-Type", "application/json");
        }
        for (int i = 0; i < cabecalhos.length; i += 2) {
            req.setHeader(cabecalhos[i], cabecalhos[i + 1]);
        }
        HttpResponse<String> r = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return new Resposta(r.statusCode(), r.body(), r);
    }

    private Resposta get(String caminho) throws Exception {
        return chamar("GET", caminho, null);
    }

    private Resposta post(String caminho, String json) throws Exception {
        return chamar("POST", caminho, json);
    }

    // O id do registro está no link "self" (o primeiro "id" do JSON pode ser o de um objeto aninhado).
    private long id(Resposta r) {
        Matcher m = Pattern.compile("\"self\":\\{\"href\":\"[^\"]*/(\\d+)\"").matcher(r.corpo());
        assertThat(m.find()).as("link self na resposta: " + r.corpo()).isTrue();
        return Long.parseLong(m.group(1));
    }

    private String unico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** Gera um CPF válido (com os dígitos verificadores certos) a partir de 9 dígitos. */
    private String cpfValido() {
        String base = String.format("%09d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 900_000_000L + 100_000_000L);
        String parcial = base + digito(base);
        return parcial + digito(parcial);
    }

    private int digito(String numeros) {
        int soma = 0;
        for (int i = 0; i < numeros.length(); i++) {
            soma += (numeros.charAt(i) - '0') * (numeros.length() + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private long criarCategoria() throws Exception {
        String codigo = "C" + unico().substring(0, 6);
        return id(post("/categorias", "{\"nome\":\"Categoria " + codigo + "\",\"codigo\":\"" + codigo + "\"}"));
    }

    private long criarLivro() throws Exception {
        long categoria = criarCategoria();
        String isbn = "97" + Math.abs(UUID.randomUUID().getMostSignificantBits() % 10_000_000_000L);
        return id(post("/livros", "{\"titulo\":\"Livro " + unico() + "\",\"isbn\":\"" + isbn + "\",\"anoPublicacao\":2000,"
            + "\"editora\":\"Editora\",\"numeroPaginas\":100,\"categoriaId\":" + categoria + ",\"autoresIds\":[]}"));
    }

    private long criarUsuario() throws Exception {
        return id(post("/usuarios", "{\"nome\":\"Leitor " + unico() + "\",\"email\":\"" + unico() + "@email.com\","
            + "\"cpf\":\"" + cpfValido() + "\",\"tipo\":\"ALUNO\"}"));
    }

    private String emprestimo(long usuario, long livro, String status, String devolucao, String emprestimoEm, String prevista) {
        return "{\"dataEmprestimo\":\"" + emprestimoEm + "\",\"dataPrevistaDevolucao\":\"" + prevista + "\","
            + (devolucao == null ? "" : "\"dataDevolucao\":\"" + devolucao + "\",")
            + "\"status\":\"" + status + "\",\"usuarioId\":" + usuario + ",\"livroId\":" + livro + "}";
    }

    // ---------------------------------------------------------------- Swagger (item 11)

    @Test
    void swaggerCarregaTodosOsSeusArquivos() throws Exception {
        Resposta pagina = get("/swagger-ui/index.html");

        assertThat(pagina.status()).isEqualTo(200);
        assertThat(pagina.corpo()).contains("<title>Biblioteca");

        // A página própria cita os arquivos do Swagger pelo nome: todos precisam existir nesta versão.
        Matcher m = Pattern.compile("(?:src|href)=\"\\./?([^\"]+)\"").matcher(pagina.corpo());
        int conferidos = 0;
        while (m.find()) {
            String arquivo = m.group(1);
            assertThat(get("/swagger-ui/" + arquivo).status()).as("/swagger-ui/" + arquivo).isEqualTo(200);
            conferidos++;
        }
        assertThat(conferidos).isGreaterThanOrEqualTo(5);
        assertThat(get("/swagger-ui/index.css").status()).isEqualTo(200);
    }

    @Test
    void especificacaoOpenApiTemOFormatoDeErroEOsLimitesDePaginacao() throws Exception {
        Resposta docs = get("/v3/api-docs");

        assertThat(docs.status()).isEqualTo(200);
        assertThat(docs.corpo()).contains("\"ErroResposta\"").contains("de 1 a 100").contains("Página inexistente");
    }

    // ---------------------------------------------------------------- erros (itens 1 e 2)

    @Test
    void respostaDe405TemOCabecalhoAllow() throws Exception {
        Resposta r = chamar("PATCH", "/livros/1", "{}");

        assertThat(r.status()).isEqualTo(405);
        assertThat(r.corpo()).contains("\"erro\":\"Método não permitido\"");
        assertThat(r.cabecalho("Allow")).contains("GET").contains("PUT").contains("DELETE");
    }

    @Test
    void outrosErrosHttpDoSpringUsamOFormatoPadrao() throws Exception {
        Resposta tipo = chamar("POST", "/livros", "texto", "Content-Type", "text/plain");
        assertThat(tipo.status()).isEqualTo(415);
        assertThat(tipo.corpo()).contains("\"status\":415").contains("\"erro\":\"Tipo de conteúdo não suportado\"").contains("\"mensagem\"");

        Resposta aceite = chamar("GET", "/livros", null, "Accept", "text/xml");
        assertThat(aceite.status()).isEqualTo(406);
        assertThat(aceite.corpo()).contains("\"status\":406").contains("\"mensagem\"");
    }

    @Test
    void registroInexistenteRespondeComMensagem() throws Exception {
        Resposta r = get("/livros/999999");

        assertThat(r.status()).isEqualTo(404);
        assertThat(r.corpo()).contains("Livro com ID 999999 não encontrado.");
    }

    // ---------------------------------------------------------------- paginação (itens 3 e 4)

    @Test
    void tamanhoDePaginaForaDoLimiteResponde400() throws Exception {
        for (String consulta : new String[] {"size=101", "size=100000", "size=0", "size=abc", "size=%205", "size=5%20"}) {
            Resposta r = get("/livros?" + consulta);
            assertThat(r.status()).as(consulta).isEqualTo(400);
            assertThat(r.corpo()).as(consulta).contains("deve ser entre 1 e 100");
        }

        assertThat(get("/livros?size=100").status()).isEqualTo(200);
        assertThat(get("/livros?size=1").status()).isEqualTo(200);
    }

    @Test
    void paginaInvalidaResponde400ePaginaInexistenteResponde404() throws Exception {
        assertThat(get("/livros?page=-1").status()).isEqualTo(400);
        assertThat(get("/livros?page=%201").status()).isEqualTo(400);
        assertThat(get("/livros?page=+1").status()).isEqualTo(400);

        criarLivro();
        Resposta alem = get("/livros?page=100000");
        assertThat(alem.status()).isEqualTo(404);
        assertThat(alem.corpo()).contains("A página 100000 não existe");
        assertThat(get("/livros/buscar?titulo=Livro&page=100000").status()).isEqualTo(404);
    }

    @Test
    void validacaoDePaginacaoSoValeParaRotasPaginadas() throws Exception {
        long livro = criarLivro();

        // /livros/{id} não usa Pageable: um "size" qualquer na URL não deve gerar o erro de paginação.
        Resposta r = get("/livros/" + livro + "?size=99999&page=-5");
        assertThat(r.status()).isEqualTo(200);
    }

    // ---------------------------------------------------------------- regras do empréstimo (item 5)

    @Test
    void emprestimoRecusaDatasEStatusContraditorios() throws Exception {
        long usuario = criarUsuario();
        long livro = criarLivro();

        Resposta devolucaoAntes = post("/emprestimos", emprestimo(usuario, livro, "DEVOLVIDO", "1990-01-01", "2026-10-01", "2026-10-15"));
        assertThat(devolucaoAntes.status()).isEqualTo(400);
        assertThat(devolucaoAntes.corpo()).contains("\"dataDevolucao\"");

        Resposta ativoComDevolucao = post("/emprestimos", emprestimo(usuario, livro, "ATIVO", "2026-10-05", "2026-10-01", "2026-10-15"));
        assertThat(ativoComDevolucao.status()).isEqualTo(400);
        assertThat(ativoComDevolucao.corpo()).contains("\"status\"");

        Resposta devolvidoSemData = post("/emprestimos", emprestimo(usuario, livro, "DEVOLVIDO", null, "2026-10-01", "2026-10-15"));
        assertThat(devolvidoSemData.status()).isEqualTo(400);
        assertThat(devolvidoSemData.corpo()).contains("\"dataDevolucao\"");

        Resposta previstaAntes = post("/emprestimos", emprestimo(usuario, livro, "ATIVO", null, "2026-10-20", "2026-10-01"));
        assertThat(previstaAntes.status()).isEqualTo(400);
        assertThat(previstaAntes.corpo()).contains("\"dataPrevistaDevolucao\"");
    }

    @Test
    void emprestimoValidoPodeSerCriadoEDevolvido() throws Exception {
        long usuario = criarUsuario();
        long livro = criarLivro();

        Resposta criado = post("/emprestimos", emprestimo(usuario, livro, "ATIVO", null, "2026-10-01", "2026-10-15"));
        assertThat(criado.status()).isEqualTo(201);

        long id = id(criado);
        Resposta devolvido = chamar("PUT", "/emprestimos/" + id, emprestimo(usuario, livro, "DEVOLVIDO", "2026-10-10", "2026-10-01", "2026-10-15"));
        assertThat(devolvido.status()).isEqualTo(200);
        assertThat(devolvido.corpo()).contains("\"dataDevolucao\":\"2026-10-10\"");

        Resposta contraditorio = chamar("PUT", "/emprestimos/" + id, emprestimo(usuario, livro, "ATIVO", "2026-10-10", "2026-10-01", "2026-10-15"));
        assertThat(contraditorio.status()).isEqualTo(400);
    }

    // ---------------------------------------------------------------- código da categoria (item 7)

    @Test
    void codigoDaCategoriaEGravadoEmMaiusculasEContinuaUnico() throws Exception {
        String codigo = "T" + unico().substring(0, 5).toLowerCase();

        Resposta primeira = post("/categorias", "{\"nome\":\"Primeira\",\"codigo\":\"" + codigo + "\"}");
        assertThat(primeira.status()).isEqualTo(201);
        assertThat(primeira.corpo()).contains("\"codigo\":\"" + codigo.toUpperCase() + "\"");

        Resposta repetida = post("/categorias", "{\"nome\":\"Segunda\",\"codigo\":\"" + codigo.toUpperCase().charAt(0) + codigo.substring(1) + "\"}");
        assertThat(repetida.status()).isEqualTo(409);
    }

    // ---------------------------------------------------------------- CPF (item 8)

    @Test
    void cpfInvalidoEhRecusadoECpfValidoEhAceito() throws Exception {
        String corpo = "{\"nome\":\"Leitor\",\"email\":\"%s@email.com\",\"cpf\":\"%s\",\"tipo\":\"ALUNO\"}";

        for (String cpf : new String[] {"11122233344", "00000000000", "11111111111", "123", "123.456.789-09"}) {
            Resposta r = post("/usuarios", corpo.formatted(unico(), cpf));
            assertThat(r.status()).as(cpf).isEqualTo(400);
            assertThat(r.corpo()).as(cpf).contains("\"cpf\"");
        }

        assertThat(post("/usuarios", corpo.formatted(unico(), "12345678909")).status()).isIn(201, 409);
        assertThat(post("/usuarios", corpo.formatted(unico(), cpfValido())).status()).isEqualTo(201);
    }

    @Test
    void cpfSoApareceNasRotasDeUsuarios() throws Exception {
        long usuario = criarUsuario();
        long livro = criarLivro();
        long emprestimo = id(post("/emprestimos", emprestimo(usuario, livro, "ATIVO", null, "2026-10-01", "2026-10-15")));
        post("/perfis", "{\"telefone\":\"11999990000\",\"endereco\":\"Rua A\",\"cidade\":\"São Paulo\",\"estado\":\"SP\","
            + "\"cep\":\"01001000\",\"dataNascimento\":\"2000-01-01\",\"usuarioId\":" + usuario + "}");

        assertThat(get("/usuarios/" + usuario).corpo()).contains("\"cpf\"");
        assertThat(get("/emprestimos/" + emprestimo).corpo()).contains("\"usuario\"").doesNotContain("\"cpf\"");
        assertThat(get("/usuarios/" + usuario + "/emprestimos").corpo()).doesNotContain("\"cpf\"");
        assertThat(get("/perfis/buscar?telefone=11999990000&size=100").corpo()).contains("\"usuario\"").doesNotContain("\"cpf\"");
    }

    // ---------------------------------------------------------------- senha e login

    private record Conta(long id, String email) {
    }

    /** Cria um usuário; a senha é opcional (null cria um usuário sem senha). */
    private Conta criarUsuarioComSenha(String senha) throws Exception {
        String email = unico() + "@email.com";
        String campoSenha = senha == null ? "" : ",\"senha\":\"" + senha + "\"";
        Resposta r = post("/usuarios", "{\"nome\":\"Leitor Senha\",\"email\":\"" + email + "\",\"cpf\":\"" + cpfValido()
            + "\",\"tipo\":\"ALUNO\"" + campoSenha + "}");
        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        return new Conta(id(r), email);
    }

    private Resposta login(String email, String senha) throws Exception {
        return post("/auth/login", "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
    }

    private Resposta trocarSenha(long id, String atual, String nova) throws Exception {
        String atualJson = atual == null ? "" : "\"senhaAtual\":\"" + atual + "\",";
        return chamar("PUT", "/usuarios/" + id + "/senha", "{" + atualJson + "\"novaSenha\":\"" + nova + "\"}");
    }

    @Test
    void senhaNuncaEhDevolvidaPelaApi() throws Exception {
        Conta conta = criarUsuarioComSenha("senhaSegura1");

        for (String caminho : new String[] {"/usuarios/" + conta.id(), "/usuarios?size=100", "/usuarios/buscar?nome=Leitor&size=100"}) {
            assertThat(get(caminho).corpo()).as(caminho).doesNotContain("senha").doesNotContain("$2a$").doesNotContain("$2b$");
        }

        assertThat(login(conta.email(), "senhaSegura1").corpo()).doesNotContain("senha").doesNotContain("$2");
    }

    @Test
    void loginComSenhaCorretaDevolveOUsuarioEIgnoraMaiusculasNoEmail() throws Exception {
        Conta conta = criarUsuarioComSenha("senhaSegura1");

        Resposta r = login(conta.email().toUpperCase(), "senhaSegura1");

        assertThat(r.status()).as(r.corpo()).isEqualTo(200);
        assertThat(id(r)).isEqualTo(conta.id());
        assertThat(r.corpo()).contains("\"nome\":\"Leitor Senha\"");
    }

    @Test
    void loginRecusaSenhaErradaEmailInexistenteEUsuarioSemSenhaComAMesmaMensagem() throws Exception {
        Conta comSenha = criarUsuarioComSenha("senhaSegura1");
        Conta semSenha = criarUsuarioComSenha(null);

        for (Resposta r : new Resposta[] {
            login(comSenha.email(), "senhaErrada1"),
            login(unico() + "@nao-existe.com", "senhaSegura1"),
            login(semSenha.email(), "qualquerSenha1")
        }) {
            assertThat(r.status()).as(r.corpo()).isEqualTo(401);
            assertThat(r.corpo()).contains("E-mail ou senha incorretos.").contains("\"status\":401");
        }

        Resposta vazio = post("/auth/login", "{\"email\":\"\",\"senha\":\"\"}");
        assertThat(vazio.status()).isEqualTo(400);
        assertThat(vazio.corpo()).contains("\"email\"").contains("\"senha\"");
    }

    @Test
    void senhaCurtaNoCadastroEhRecusada() throws Exception {
        Resposta r = post("/usuarios", "{\"nome\":\"Leitor\",\"email\":\"" + unico() + "@email.com\",\"cpf\":\"" + cpfValido()
            + "\",\"tipo\":\"ALUNO\",\"senha\":\"curta\"}");

        assertThat(r.status()).isEqualTo(400);
        assertThat(r.corpo()).contains("\"senha\":\"A senha deve ter de 8 a 72 caracteres\"");
    }

    @Test
    void trocarSenhaExigeASenhaAtualEValidaANova() throws Exception {
        Conta conta = criarUsuarioComSenha("senhaSegura1");

        Resposta errada = trocarSenha(conta.id(), "outraSenha99", "novaSenha456");
        assertThat(errada.status()).isEqualTo(400);
        assertThat(errada.corpo()).contains("\"senhaAtual\":\"A senha atual está incorreta.\"");

        Resposta semAtual = trocarSenha(conta.id(), null, "novaSenha456");
        assertThat(semAtual.status()).isEqualTo(400);
        assertThat(semAtual.corpo()).contains("\"senhaAtual\":\"Informe a senha atual.\"");

        Resposta curta = trocarSenha(conta.id(), "senhaSegura1", "curta");
        assertThat(curta.status()).isEqualTo(400);
        assertThat(curta.corpo()).contains("\"novaSenha\"");

        Resposta igual = trocarSenha(conta.id(), "senhaSegura1", "senhaSegura1");
        assertThat(igual.status()).isEqualTo(400);
        assertThat(igual.corpo()).contains("\"novaSenha\":\"A nova senha deve ser diferente da atual.\"");

        // Nenhuma tentativa acima mudou a senha.
        assertThat(login(conta.email(), "senhaSegura1").status()).isEqualTo(200);
    }

    @Test
    void trocarSenhaFazAAntigaDeixarDeValerEANovaPassarAValer() throws Exception {
        Conta conta = criarUsuarioComSenha("senhaSegura1");

        Resposta r = trocarSenha(conta.id(), "senhaSegura1", "novaSenha456");

        assertThat(r.status()).isEqualTo(204);
        assertThat(login(conta.email(), "senhaSegura1").status()).isEqualTo(401);
        assertThat(login(conta.email(), "novaSenha456").status()).isEqualTo(200);
        assertThat(trocarSenha(999999, "x", "novaSenha456").status()).isEqualTo(404);
    }

    @Test
    void usuarioSemSenhaPodeDefinirAPrimeiraEAtualizarDadosNaoMudaASenha() throws Exception {
        Conta conta = criarUsuarioComSenha(null);

        assertThat(trocarSenha(conta.id(), null, "primeiraSenha1").status()).isEqualTo(204);
        assertThat(login(conta.email(), "primeiraSenha1").status()).isEqualTo(200);

        // PUT /usuarios/{id} atualiza os dados, inclusive se o corpo trouxer uma "senha": ela é ignorada.
        Resposta atualizado = chamar("PUT", "/usuarios/" + conta.id(), "{\"nome\":\"Nome Novo\",\"email\":\"" + conta.email()
            + "\",\"cpf\":\"" + cpfDe(conta.id()) + "\",\"tipo\":\"PROFESSOR\",\"senha\":\"outraSenha789\"}");
        assertThat(atualizado.status()).as(atualizado.corpo()).isEqualTo(200);
        assertThat(login(conta.email(), "primeiraSenha1").status()).isEqualTo(200);
        assertThat(login(conta.email(), "outraSenha789").status()).isEqualTo(401);
    }

    private String cpfDe(long usuario) throws Exception {
        Matcher m = Pattern.compile("\"cpf\":\"(\\d{11})\"").matcher(get("/usuarios/" + usuario).corpo());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    @Test
    void swaggerDocumentaLoginETrocaDeSenhaSemExporOHash() throws Exception {
        String api = get("/v3/api-docs").corpo();

        assertThat(api).contains("/auth/login").contains("/usuarios/{id}/senha").contains("TrocaSenhaRequest").contains("LoginRequest");
        assertThat(api).doesNotContain("senhaHash");
    }
}

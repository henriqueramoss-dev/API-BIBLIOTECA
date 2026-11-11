package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reservas do cliente: escolher um livro e reservá-lo para retirar na biblioteca.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class ReservaTest extends ApiHttpTeste {

    private static final String SENHA = "senhaDeTeste123";

    /** Um cliente (ou funcionário) com senha e uma chave do nível pedido. */
    private record Pessoa(long id, String email, String chave) {
    }

    private Pessoa novaPessoa(String tipo, String nivel) throws Exception {
        String email = "reserva-" + unico() + "@email.com";
        Resposta usuario = comAdmin("POST", "/usuarios", usuarioJson(email, tipo, SENHA));
        assertThat(usuario.status()).as(usuario.corpo()).isEqualTo(201);
        Resposta chave = chamar("POST", "/chaves-api", "{\"email\":\"" + email + "\",\"senha\":\"" + SENHA
            + "\",\"nome\":\"Teste\",\"nivel\":\"" + nivel + "\"}");
        assertThat(chave.status()).as(chave.corpo()).isEqualTo(201);
        return new Pessoa(id(usuario), email, campo(chave, "chave"));
    }

    private long novoLivro() throws Exception {
        Resposta categoria = comAdmin("POST", "/categorias", "{\"nome\":\"Cat " + unico() + "\",\"codigo\":\"C" + unico() + "\"}");
        assertThat(categoria.status()).as(categoria.corpo()).isEqualTo(201);
        Resposta livro = comAdmin("POST", "/livros", "{\"titulo\":\"Livro " + unico() + "\",\"isbn\":\"" + unico()
            + "\",\"anoPublicacao\":2001,\"editora\":\"Ed\",\"numeroPaginas\":100,\"categoriaId\":" + id(categoria) + "}");
        assertThat(livro.status()).as(livro.corpo()).isEqualTo(201);
        return id(livro);
    }

    /** A situação do livro no catálogo público (GET /livros/{id}): DISPONIVEL, EMPRESTADO ou INDISPONIVEL. */
    private String situacao(long livroId) throws Exception {
        Resposta r = chamar("GET", "/livros/" + livroId, null, "X-API-Key", CHAVE_CATALOGO);
        assertThat(r.status()).isEqualTo(200);
        return campo(r, "situacao");
    }

    private Resposta reservar(Pessoa pessoa, long livroId) throws Exception {
        return chamar("POST", "/reservas", "{\"livroId\":" + livroId + "}", "X-API-Key", pessoa.chave());
    }

    @Test
    void clienteReservaUmLivroComChaveDeLeitura() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        long livro = novoLivro();

        Resposta r = reservar(ana, livro);

        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        assertThat(campo(r, "status")).isEqualTo("RESERVADO");
        assertThat(campo(r, "retirarAte")).isNotBlank();
        // A reserva é da dona da chave, sem o cliente escolher o usuário.
        assertThat(r.corpo()).contains("\"email\":\"" + ana.email() + "\"");
    }

    @Test
    void livroReservadoFicaIndisponivelParaOutraPessoa() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        Pessoa beto = novaPessoa("VISITANTE", "LEITURA");
        long livro = novoLivro();

        assertThat(situacao(livro)).isEqualTo("DISPONIVEL");
        assertThat(reservar(ana, livro).status()).isEqualTo(201);

        Resposta segunda = reservar(beto, livro);
        assertThat(segunda.status()).isEqualTo(409);
        assertThat(segunda.corpo()).contains("não está disponível");
        // O catálogo (público) mostra o livro como indisponível, sem dados de pessoas.
        assertThat(situacao(livro)).isEqualTo("INDISPONIVEL");
        assertThat(chamar("GET", "/livros/" + livro, null, "X-API-Key", CHAVE_CATALOGO).corpo()).doesNotContain("@");
    }

    @Test
    void situacaoDoLivroAcompanhaAReservaOEmprestimoEADevolucao() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        long livro = novoLivro();
        assertThat(situacao(livro)).isEqualTo("DISPONIVEL");

        Resposta reserva = reservar(ana, livro);
        assertThat(situacao(livro)).isEqualTo("INDISPONIVEL");

        String corpo = "{\"dataEmprestimo\":\"2026-11-10\",\"dataPrevistaDevolucao\":\"2026-11-24\",\"status\":\"STATUS\","
            + "\"usuarioId\":" + ana.id() + ",\"livroId\":" + livro + "}";
        assertThat(comAdmin("PUT", "/emprestimos/" + id(reserva), corpo.replace("STATUS", "ATIVO")).status()).isEqualTo(200);
        assertThat(situacao(livro)).isEqualTo("EMPRESTADO");

        String devolvido = corpo.replace("STATUS", "DEVOLVIDO").replace("}", ",\"dataDevolucao\":\"2026-11-12\"}");
        assertThat(comAdmin("PUT", "/emprestimos/" + id(reserva), devolvido).status()).isEqualTo(200);
        assertThat(situacao(livro)).isEqualTo("DISPONIVEL");
    }

    @Test
    void listagensDeLivrosJaTrazAsituacaoDeCadaLivro() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        long livro = novoLivro();
        reservar(ana, livro);

        assertThat(chamar("GET", "/livros?size=100", null, "X-API-Key", CHAVE_CATALOGO).corpo()).contains("\"situacao\":\"INDISPONIVEL\"");
        assertThat(chamar("GET", "/livros?size=100", null, "X-API-Version", "2", "X-API-Key", CHAVE_CATALOGO).corpo()).contains("\"situacao\":\"INDISPONIVEL\"");
        assertThat(chamar("GET", "/livros/buscar?titulo=Livro", null, "X-API-Key", CHAVE_CATALOGO).corpo()).contains("\"situacao\"");
    }

    @Test
    void cadaPessoaTemNoMaximoTresReservasAguardandoRetirada() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        for (int i = 0; i < 3; i++) {
            assertThat(reservar(ana, novoLivro()).status()).isEqualTo(201);
        }

        Resposta quarta = reservar(ana, novoLivro());

        assertThat(quarta.status()).isEqualTo(409);
        assertThat(campo(quarta, "mensagem")).contains("máximo é 3");
    }

    @Test
    void minhasReservasMostramSoAsDaPropriaPessoa() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        Pessoa beto = novaPessoa("ALUNO", "LEITURA");
        long livroDaAna = novoLivro();
        long livroDoBeto = novoLivro();
        reservar(ana, livroDaAna);
        reservar(beto, livroDoBeto);

        Resposta minhas = chamar("GET", "/reservas/minhas", null, "X-API-Key", ana.chave());

        assertThat(minhas.status()).isEqualTo(200);
        assertThat(minhas.corpo()).contains(ana.email()).doesNotContain(beto.email());
    }

    @Test
    void clienteCancelaAPropriaReservaMasNaoADeOutraPessoa() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        Pessoa beto = novaPessoa("ALUNO", "LEITURA");
        long livro = novoLivro();
        long reserva = id(reservar(ana, livro));

        // Beto não vê nem cancela a reserva da Ana (404, sem revelar que ela existe).
        assertThat(chamar("DELETE", "/reservas/" + reserva, null, "X-API-Key", beto.chave()).status()).isEqualTo(404);
        assertThat(chamar("DELETE", "/reservas/" + reserva, null, "X-API-Key", ana.chave()).status()).isEqualTo(204);
        // O livro volta a ficar disponível.
        assertThat(reservar(beto, livro).status()).isEqualTo(201);
    }

    @Test
    void reservaJaRetiradaNaoPodeSerCancelada() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");
        Resposta reserva = reservar(ana, novoLivro());
        long id = id(reserva);
        // A equipe confirma a retirada: o empréstimo passa a ATIVO.
        Resposta atualizada = comAdmin("PUT", "/emprestimos/" + id, "{\"dataEmprestimo\":\"2026-11-10\",\"dataPrevistaDevolucao\":\"2026-11-24\","
            + "\"status\":\"ATIVO\",\"usuarioId\":" + ana.id() + ",\"livroId\":" + livroDe(reserva) + "}");
        assertThat(atualizada.status()).as(atualizada.corpo()).isEqualTo(200);

        Resposta cancelar = chamar("DELETE", "/reservas/" + id, null, "X-API-Key", ana.chave());

        assertThat(cancelar.status()).isEqualTo(409);
        assertThat(campo(cancelar, "mensagem")).contains("ainda não foram retiradas");
    }

    @Test
    void semChaveNaoReservaEChaveDeAdminInicialNaoTemReservas() throws Exception {
        assertThat(chamar("POST", "/reservas", "{\"livroId\":1}").status()).isEqualTo(401);
        assertThat(chamar("GET", "/reservas/minhas", null).status()).isEqualTo(401);
        // A chave ADMIN inicial não pertence a ninguém.
        assertThat(comAdmin("GET", "/reservas/minhas", null).status()).isEqualTo(403);
    }

    @Test
    void reservaDeLivroInexistenteOuSemLivroResponde400() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");

        assertThat(reservar(ana, 999_999).status()).isEqualTo(400);
        assertThat(chamar("POST", "/reservas", "{}", "X-API-Key", ana.chave()).status()).isEqualTo(400);
    }

    @Test
    void clienteNaoVeDadosPessoaisDeOutrasPessoas() throws Exception {
        Pessoa ana = novaPessoa("ALUNO", "LEITURA");

        assertThat(chamar("GET", "/usuarios", null, "X-API-Key", ana.chave()).status()).isEqualTo(403);
        assertThat(chamar("GET", "/emprestimos", null, "X-API-Key", ana.chave()).status()).isEqualTo(403);
        assertThat(chamar("GET", "/carteirinhas", null, "X-API-Key", ana.chave()).status()).isEqualTo(403);
        // O catálogo continua aberto.
        assertThat(chamar("GET", "/livros", null, "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(200);
    }

    @Test
    void respostaDoLoginTrazNomeETipoParaOSiteEscolherATela() throws Exception {
        Pessoa funcionario = novaPessoa("FUNCIONARIO", "ADMIN");
        Resposta chave = chamar("POST", "/chaves-api", "{\"email\":\"" + funcionario.email() + "\",\"senha\":\"" + SENHA
            + "\",\"nome\":\"Site\",\"nivel\":\"LEITURA\"}");

        assertThat(campo(chave, "usuarioTipo")).isEqualTo("FUNCIONARIO");
        assertThat(campo(chave, "usuarioNome")).startsWith("Usuário");
    }

    /** O id do livro da reserva, lido do link "livro" da resposta. */
    private long livroDe(Resposta reserva) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"livro\":\\{\"href\":\"[^\"]*/(\\d+)\"").matcher(reserva.corpo());
        assertThat(m.find()).as(reserva.corpo()).isTrue();
        return Long.parseLong(m.group(1));
    }
}

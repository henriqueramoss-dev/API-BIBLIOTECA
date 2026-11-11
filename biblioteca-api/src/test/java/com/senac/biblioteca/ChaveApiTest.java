package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Autenticação com chave de API (cabeçalho X-API-Key) e níveis de acesso.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class ChaveApiTest extends ApiHttpTeste {

    private static final String SENHA = "senhaDeTeste123";

    /** Cadastra (com a chave ADMIN) um usuário com senha e devolve o e-mail dele. */
    private String criarUsuario(String tipo) throws Exception {
        String email = "chave-" + unico() + "@email.com";
        assertThat(comAdmin("POST", "/usuarios", usuarioJson(email, tipo, SENHA)).status()).isEqualTo(201);
        return email;
    }

    private Resposta gerarChave(String email, String senha, String nivel) throws Exception {
        String nivelJson = nivel == null ? "" : ",\"nivel\":\"" + nivel + "\"";
        return chamar("POST", "/chaves-api", "{\"email\":\"" + email + "\",\"senha\":\"" + senha
            + "\",\"nome\":\"Chave de teste\"" + nivelJson + "}");
    }

    private String novaChave(String tipo, String nivel) throws Exception {
        Resposta r = gerarChave(criarUsuario(tipo), SENHA, nivel);
        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        return campo(r, "chave");
    }

    // ---------------------------------------------------------------- sem chave ou chave inválida

    @Test
    void rotaProtegidaSemChaveResponde401ComOCabecalhoWwwAuthenticate() throws Exception {
        Resposta r = chamar("GET", "/usuarios", null);

        assertThat(r.status()).isEqualTo(401);
        assertThat(r.cabecalho("WWW-Authenticate")).contains("X-API-Key");
        assertThat(campo(r, "mensagem")).contains("X-API-Key");
    }

    @Test
    void chaveInexistenteResponde401AteEmRotaPublica() throws Exception {
        Resposta r = chamar("GET", "/livros", null, "X-API-Key", "bib_chave_que_nao_existe");

        assertThat(r.status()).isEqualTo(401);
        assertThat(campo(r, "mensagem")).contains("não existe ou foi revogada");
    }

    @Test
    void catalogoExigeChaveDeLeituraMasLoginCadastroESwaggerContinuamAbertos() throws Exception {
        // Sem chave, o catálogo dá 401; com a chave de leitura (a do catálogo ou a de um cliente), responde.
        for (String rota : new String[]{"/livros", "/autores", "/categorias", "/livros/1", "/livros/buscar?titulo=a", "/categorias/1/livros"}) {
            assertThat(chamar("GET", rota, null).status()).as("sem chave: GET " + rota).isEqualTo(401);
        }
        assertThat(chamar("GET", "/livros", null, "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(200);
        assertThat(chamar("GET", "/autores", null, "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(200);
        assertThat(chamar("GET", "/categorias", null, "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(200);
        // Seguem abertos: o Swagger, o login, a geração de chave e o autocadastro.
        assertThat(chamar("GET", "/v3/api-docs", null).status()).isEqualTo(200);
        assertThat(chamar("POST", "/auth/login", "{\"email\":\"ninguem@email.com\",\"senha\":\"x\"}").status())
            .isEqualTo(401); // credenciais erradas, mas a rota foi atendida
    }

    @Test
    void chaveDoCatalogoSoLeOCatalogo() throws Exception {
        for (String rota : new String[]{"/usuarios", "/carteirinhas", "/emprestimos", "/livros/1/emprestimos"}) {
            assertThat(chamar("GET", rota, null, "X-API-Key", CHAVE_CATALOGO).status()).as("GET " + rota).isEqualTo(403);
        }
        // Não escreve, não exclui, não reserva (a reserva é do dono da chave) e não lista chaves.
        assertThat(chamar("POST", "/categorias", "{\"nome\":\"X\"}", "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(403);
        assertThat(chamar("DELETE", "/autores/1", null, "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(403);
        assertThat(chamar("POST", "/reservas", "{\"livroId\":1}", "X-API-Key", CHAVE_CATALOGO).status()).isEqualTo(403);
        Resposta chaves = chamar("GET", "/chaves-api", null, "X-API-Key", CHAVE_CATALOGO);
        assertThat(chaves.status()).isEqualTo(200);
        assertThat(chaves.corpo()).doesNotContain("\"prefixo\"");
    }

    @Test
    void historicoDeEmprestimosDeUmLivroNaoEhPublico() throws Exception {
        // Fica dentro de /livros (catálogo público), mas mostra quem pegou o livro.
        assertThat(chamar("GET", "/livros/1/emprestimos", null).status()).isEqualTo(401);
        assertThat(chamar("GET", "/livros/1", null, "X-API-Key", CHAVE_CATALOGO).status()).isIn(200, 404);
    }

    @Test
    void escritaEExclusaoExigemChave() throws Exception {
        assertThat(chamar("POST", "/categorias", "{\"nome\":\"Sem chave\"}").status()).isEqualTo(401);
        assertThat(chamar("PUT", "/livros/1", "{}").status()).isEqualTo(401);
        assertThat(chamar("DELETE", "/autores/1", null).status()).isEqualTo(401);
    }

    // ---------------------------------------------------------------- geração da chave

    @Test
    void funcionarioGeraChaveQueSoApareceUmaVezEPassaAFuncionar() throws Exception {
        Resposta gerada = gerarChave(criarUsuario("FUNCIONARIO"), SENHA, "ADMIN");

        assertThat(gerada.status()).isEqualTo(201);
        String chave = campo(gerada, "chave");
        assertThat(chave).startsWith("bib_").hasSize(47);
        assertThat(gerada.cabecalho("Location")).matches(".*/chaves-api/\\d+");
        assertThat(campo(gerada, "prefixo")).isEqualTo(chave.substring(0, 10));

        assertThat(chamar("GET", "/usuarios", null, "X-API-Key", chave).status()).isEqualTo(200);

        // Depois de gerada, a chave completa (e o hash) nunca mais aparecem.
        Resposta atual = chamar("GET", "/chaves-api/atual", null, "X-API-Key", chave);
        assertThat(atual.status()).isEqualTo(200);
        assertThat(atual.corpo()).doesNotContain(chave).doesNotContain("\"hash\"").contains("\"nivel\":\"ADMIN\"");
        assertThat(chamar("GET", "/chaves-api", null, "X-API-Key", chave).corpo()).doesNotContain(chave);
    }

    @Test
    void senhaErradaNaoGeraChave() throws Exception {
        Resposta r = gerarChave(criarUsuario("FUNCIONARIO"), "senhaErrada999", null);

        assertThat(r.status()).isEqualTo(401);
        assertThat(r.corpo()).doesNotContain("bib_");
    }

    @Test
    void alunoSoPodeGerarChaveDeLeitura() throws Exception {
        String email = criarUsuario("ALUNO");

        Resposta escrita = gerarChave(email, SENHA, "ESCRITA");
        assertThat(escrita.status()).isEqualTo(403);
        assertThat(campo(escrita, "mensagem")).contains("no máximo nível LEITURA");

        Resposta leitura = gerarChave(email, SENHA, null);
        assertThat(leitura.status()).isEqualTo(201);
        assertThat(campo(leitura, "nivel")).isEqualTo("LEITURA");
    }

    // ---------------------------------------------------------------- níveis de acesso

    @Test
    void chaveDeLeituraEhDeClienteEnaoVeDadosDeOutrasPessoasNemAltera() throws Exception {
        String chave = novaChave("ALUNO", "LEITURA");

        // Cliente vê o que é dele (a própria chave), mas não os dados pessoais de outras pessoas.
        assertThat(chamar("GET", "/chaves-api/atual", null, "X-API-Key", chave).status()).isEqualTo(200);
        assertThat(chamar("GET", "/usuarios", null, "X-API-Key", chave).status()).isEqualTo(403);
        assertThat(chamar("GET", "/carteirinhas", null, "X-API-Key", chave).status()).isEqualTo(403);
        assertThat(chamar("GET", "/emprestimos", null, "X-API-Key", chave).status()).isEqualTo(403);

        Resposta criar = chamar("POST", "/categorias", "{\"nome\":\"Leitura\"}", "X-API-Key", chave);
        assertThat(criar.status()).isEqualTo(403);
        assertThat(campo(criar, "mensagem")).contains("ESCRITA").contains("LEITURA");

        assertThat(chamar("DELETE", "/autores/1", null, "X-API-Key", chave).status()).isEqualTo(403);
    }

    @Test
    void clienteComChaveDeLeituraListaSoAsProprias() throws Exception {
        String chaveDaAna = novaChave("ALUNO", "LEITURA");
        String chaveDoBeto = novaChave("ALUNO", "LEITURA");

        Resposta lista = chamar("GET", "/chaves-api", null, "X-API-Key", chaveDaAna);

        // Antes da correção, quem não era ADMIN recebia 500 aqui.
        assertThat(lista.status()).as(lista.corpo()).isEqualTo(200);
        assertThat(lista.corpo()).contains(chaveDaAna.substring(0, 10)).doesNotContain(chaveDoBeto.substring(0, 10));
    }

    @Test
    void chaveDeLeituraListaCatalogoNoSwaggerSemPrecisarDeOutroNivel() throws Exception {
        String chave = novaChave("ALUNO", "LEITURA");

        // Com a chave de leitura, as listas do catálogo respondem (e são as mesmas que um visitante vê).
        for (String rota : new String[]{"/livros", "/autores", "/categorias", "/livros/buscar?titulo=a", "/categorias/1/livros"}) {
            int status = chamar("GET", rota, null, "X-API-Key", chave).status();
            assertThat(status).as("GET " + rota + " com chave de LEITURA").isIn(200, 404); // 404: categoria 1 pode não existir neste teste
        }
    }

    @Test
    void chaveDeEscritaCadastraMasSoAdminExclui() throws Exception {
        String chave = novaChave("FUNCIONARIO", "ESCRITA");

        // A equipe da biblioteca (ESCRITA ou ADMIN) vê os dados de todos.
        assertThat(chamar("GET", "/usuarios", null, "X-API-Key", chave).status()).isEqualTo(200);

        Resposta autor = chamar("POST", "/autores", "{\"nome\":\"Autor " + unico() + "\",\"nacionalidade\":\"Brasileira\"}", "X-API-Key", chave);
        assertThat(autor.status()).isEqualTo(201);

        assertThat(chamar("DELETE", "/autores/" + id(autor), null, "X-API-Key", chave).status()).isEqualTo(403);
        assertThat(comAdmin("DELETE", "/autores/" + id(autor), null).status()).isEqualTo(204);
    }

    // ---------------------------------------------------------------- gerenciamento

    @Test
    void chaveRevogadaDeixaDeFuncionarNaHora() throws Exception {
        Resposta gerada = gerarChave(criarUsuario("ALUNO"), SENHA, null);
        String chave = campo(gerada, "chave");
        long id = id(gerada);

        assertThat(chamar("DELETE", "/chaves-api/" + id, null, "X-API-Key", chave).status()).isEqualTo(204);
        assertThat(chamar("GET", "/usuarios", null, "X-API-Key", chave).status()).isEqualTo(401);

        // Continua listada para o administrador, marcada como inativa.
        Resposta registro = comAdmin("GET", "/chaves-api/" + id, null);
        assertThat(registro.corpo()).contains("\"ativa\":false").contains("revogadaEm").doesNotContain("\"revogar\"");
    }

    @Test
    void ninguemVeOuRevogaAChaveDeOutraPessoa() throws Exception {
        long idDaOutra = id(gerarChave(criarUsuario("ALUNO"), SENHA, null));
        String minha = novaChave("ALUNO", null);

        assertThat(chamar("GET", "/chaves-api/" + idDaOutra, null, "X-API-Key", minha).status()).isEqualTo(404);
        assertThat(chamar("DELETE", "/chaves-api/" + idDaOutra, null, "X-API-Key", minha).status()).isEqualTo(404);
        assertThat(chamar("GET", "/chaves-api", null, "X-API-Key", minha).corpo()).doesNotContain("/chaves-api/" + idDaOutra + "\"");
    }

    // ---------------------------------------------------------------- autocadastro e primeira senha

    @Test
    void autocadastroSemChaveSoAceitaAlunoEVisitante() throws Exception {
        assertThat(chamar("POST", "/usuarios", usuarioJson(unico() + "@email.com", "ALUNO", SENHA)).status()).isEqualTo(201);
        assertThat(chamar("POST", "/usuarios", usuarioJson(unico() + "@email.com", "VISITANTE", null)).status()).isEqualTo(201);

        Resposta funcionario = chamar("POST", "/usuarios", usuarioJson(unico() + "@email.com", "FUNCIONARIO", SENHA));
        assertThat(funcionario.status()).isEqualTo(403);
        assertThat(campo(funcionario, "mensagem")).contains("ALUNO e VISITANTE");
    }

    @Test
    void primeiraSenhaExigeChaveDeEscrita() throws Exception {
        long usuario = id(comAdmin("POST", "/usuarios", usuarioJson(unico() + "@email.com", "ALUNO", null)));
        String corpo = "{\"novaSenha\":\"primeiraSenha123\"}";

        assertThat(chamar("PUT", "/usuarios/" + usuario + "/senha", corpo).status()).isEqualTo(403);
        assertThat(comAdmin("PUT", "/usuarios/" + usuario + "/senha", corpo).status()).isEqualTo(204);

        // Com a senha definida, a troca volta a ser pública: quem sabe a senha atual pode trocá-la.
        assertThat(chamar("PUT", "/usuarios/" + usuario + "/senha",
            "{\"senhaAtual\":\"primeiraSenha123\",\"novaSenha\":\"segundaSenha123\"}").status()).isEqualTo(204);
    }

    // ---------------------------------------------------------------- Swagger

    @Test
    void swaggerMostraQuaisRotasExigemChave() throws Exception {
        String api = chamar("GET", "/v3/api-docs/v2", null).corpo();

        assertThat(api).contains("\"chaveApi\"").contains("\"X-API-Key\"");
        assertThat(api).contains("Exige chave de API (X-API-Key) de nível ADMIN");
        assertThat(api).contains("/chaves-api/atual");
    }
}

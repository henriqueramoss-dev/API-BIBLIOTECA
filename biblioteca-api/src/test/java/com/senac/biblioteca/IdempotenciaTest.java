package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Idempotência dos POSTs com o cabeçalho X-Idempotency-Key.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class IdempotenciaTest extends ApiHttpTeste {

    private String autorJson(String nome) {
        return "{\"nome\":\"" + nome + "\",\"nacionalidade\":\"Brasileira\"}";
    }

    private Resposta criarAutor(String json, String chaveIdempotencia, String chaveApi) throws Exception {
        return chamar("POST", "/autores", json, "X-API-Key", chaveApi, "X-Idempotency-Key", chaveIdempotencia);
    }

    private long quantosAutoresComONome(String nome) throws Exception {
        String corpo = comAdmin("GET", "/autores/buscar?nome=" + nome, null).corpo();
        return Long.parseLong(corpo.replaceAll("(?s).*\"totalElements\":(\\d+).*", "$1"));
    }

    @Test
    void reenvioComAMesmaChaveDevolveARespostaOriginalSemCadastrarDeNovo() throws Exception {
        String nome = "Idempotente" + unico();
        String chave = UUID.randomUUID().toString();

        Resposta primeira = criarAutor(autorJson(nome), chave, CHAVE_ADMIN);
        Resposta segunda = criarAutor(autorJson(nome), chave, CHAVE_ADMIN);

        assertThat(primeira.status()).isEqualTo(201);
        assertThat(primeira.cabecalho("X-Idempotency-Replayed")).isEmpty();
        assertThat(segunda.status()).isEqualTo(201);
        assertThat(segunda.cabecalho("X-Idempotency-Replayed")).isEqualTo("true");
        assertThat(segunda.corpo()).isEqualTo(primeira.corpo());
        assertThat(segunda.cabecalho("Location")).isEqualTo(primeira.cabecalho("Location")).isNotEmpty();
        assertThat(quantosAutoresComONome(nome)).isEqualTo(1);
    }

    @Test
    void semOCabecalhoCadaPostCadastraUmRegistro() throws Exception {
        String nome = "SemChave" + unico();

        assertThat(comAdmin("POST", "/autores", autorJson(nome)).status()).isEqualTo(201);
        assertThat(comAdmin("POST", "/autores", autorJson(nome)).status()).isEqualTo(201);
        assertThat(quantosAutoresComONome(nome)).isEqualTo(2);
    }

    @Test
    void mesmaChaveComOutroCorpoResponde422() throws Exception {
        String chave = UUID.randomUUID().toString();
        assertThat(criarAutor(autorJson("Primeiro" + unico()), chave, CHAVE_ADMIN).status()).isEqualTo(201);

        Resposta outra = criarAutor(autorJson("Segundo" + unico()), chave, CHAVE_ADMIN);
        assertThat(outra.status()).isEqualTo(422);
        assertThat(campo(outra, "mensagem")).contains("já foi usada com outra requisição");
    }

    @Test
    void mesmaChaveEmOutroEnderecoResponde422() throws Exception {
        String chave = UUID.randomUUID().toString();
        String nome = "Endereco" + unico();
        assertThat(criarAutor(autorJson(nome), chave, CHAVE_ADMIN).status()).isEqualTo(201);

        Resposta categoria = chamar("POST", "/categorias", "{\"nome\":\"" + nome + "\",\"codigo\":\"E" + unico().substring(0, 5) + "\"}",
            "X-API-Key", CHAVE_ADMIN, "X-Idempotency-Key", chave);
        assertThat(categoria.status()).isEqualTo(422);
    }

    @Test
    void respostaDeErroNaoEhGuardadaEPermiteCorrigirComAMesmaChave() throws Exception {
        String chave = UUID.randomUUID().toString();

        Resposta invalida = criarAutor("{\"nome\":\"\"}", chave, CHAVE_ADMIN);
        assertThat(invalida.status()).isEqualTo(400);

        Resposta corrigida = criarAutor(autorJson("Corrigido" + unico()), chave, CHAVE_ADMIN);
        assertThat(corrigida.status()).isEqualTo(201);
        assertThat(corrigida.cabecalho("X-Idempotency-Replayed")).isEmpty();
    }

    @Test
    void chavesIguaisDeClientesDiferentesNaoSeMisturam() throws Exception {
        String chave = UUID.randomUUID().toString();

        // Autocadastro sem chave de API (cliente = IP) e cadastro pelo administrador (cliente = chave ADMIN).
        Resposta peloIp = chamar("POST", "/usuarios", usuarioJson(unico() + "@email.com", "ALUNO", null),
            "X-Idempotency-Key", chave);
        Resposta peloAdmin = chamar("POST", "/usuarios", usuarioJson(unico() + "@email.com", "ALUNO", null),
            "X-API-Key", CHAVE_ADMIN, "X-Idempotency-Key", chave);

        assertThat(peloIp.status()).isEqualTo(201);
        assertThat(peloAdmin.status()).isEqualTo(201);
        assertThat(id(peloAdmin)).isNotEqualTo(id(peloIp));
    }

    @Test
    void respostaGuardadaNaoEhEntregueAQuemNaoTemChaveDeApi() throws Exception {
        String chave = UUID.randomUUID().toString();
        String json = autorJson("Protegido" + unico());
        assertThat(criarAutor(json, chave, CHAVE_ADMIN).status()).isEqualTo(201);

        Resposta semChave = chamar("POST", "/autores", json, "X-Idempotency-Key", chave);
        assertThat(semChave.status()).isEqualTo(401);
    }

    @Test
    void chaveEmFormatoInvalidoResponde400() throws Exception {
        Resposta comEspaco = criarAutor(autorJson("Formato"), "chave com espaços", CHAVE_ADMIN);
        Resposta longaDemais = criarAutor(autorJson("Formato"), "x".repeat(101), CHAVE_ADMIN);

        assertThat(comEspaco.status()).isEqualTo(400);
        assertThat(campo(comEspaco, "erro")).isEqualTo("Chave de idempotência inválida");
        assertThat(longaDemais.status()).isEqualTo(400);
    }

    @Test
    void loginEGeracaoDeChaveNaoUsamIdempotencia() throws Exception {
        String chave = UUID.randomUUID().toString();
        String login = "{\"email\":\"ninguem@email.com\",\"senha\":\"senhaQualquer1\"}";

        Resposta primeira = chamar("POST", "/auth/login", login, "X-Idempotency-Key", chave);
        Resposta segunda = chamar("POST", "/auth/login", "{\"email\":\"outro@email.com\",\"senha\":\"x\"}",
            "X-Idempotency-Key", chave);

        assertThat(primeira.status()).isEqualTo(401);
        assertThat(segunda.status()).isEqualTo(401); // não é 422: a chave é ignorada no login
    }

    @Test
    void swaggerDocumentaOCabecalhoNosPosts() throws Exception {
        String api = chamar("GET", "/v3/api-docs", null).corpo();

        assertThat(api).contains("X-Idempotency-Key").contains("\"422\"");
    }
}

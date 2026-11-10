package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O seletor de versão do Swagger: a versão 1 mostra só a Parte 1; a versão 2 mostra a Parte 1 mais a Parte 2.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class SwaggerVersoesTest extends ApiHttpTeste {

    private String v1() throws Exception {
        Resposta r = chamar("GET", "/v3/api-docs/v1", null);
        assertThat(r.status()).isEqualTo(200);
        return r.corpo();
    }

    private String v2() throws Exception {
        Resposta r = chamar("GET", "/v3/api-docs/v2", null);
        assertThat(r.status()).isEqualTo(200);
        return r.corpo();
    }

    @Test
    void versao1MostraOCrudDasEntidades() throws Exception {
        String doc = v1();

        for (String rota : new String[]{"\"/livros\"", "\"/livros/{id}\"", "\"/autores\"", "\"/categorias\"", "\"/usuarios\"",
            "\"/carteirinhas\"", "\"/emprestimos\"", "\"/livros/buscar\"", "\"/categorias/{id}/livros\""}) {
            assertThat(doc).as("rota da Parte 1 " + rota).contains(rota);
        }
        assertThat(doc).contains("\"size\"").contains("Registros por página");
    }

    @Test
    void versao1NaoMostraNadaDaParte2() throws Exception {
        String doc = v1();

        // Rotas dos recursos avançados e extras
        // (o texto de ajuda pode citar POST /chaves-api; o que não pode é a rota aparecer na lista de rotas)
        for (String rota : new String[]{"\"/chaves-api", "\"/reservas", "\"/auth/", "\"/usuarios/{id}/senha\""}) {
            assertThat(doc).as("rota fora da Parte 1: " + rota).doesNotContain(rota);
        }
        // Idempotência, versionamento por cabeçalho e limite de requisições
        assertThat(doc).doesNotContain("X-Idempotency-Key").doesNotContain("X-API-Version");
        assertThat(doc).doesNotContain("\"429\"").doesNotContain("Retry-After");
        assertThat(doc).doesNotContain("\"401\"");
        // Campos e valores extras
        assertThat(doc).doesNotContain("\"senha\"").doesNotContain("retirarAte").doesNotContain("RESERVADO");
    }

    @Test
    void versao1TemOAuthorizeParaExecutarAsRotasQueGravam() throws Exception {
        String doc = v1();

        // Só o botão Authorize e o cadeado: sem a chave, nenhuma rota que grava executaria no Swagger.
        assertThat(doc).contains("securitySchemes").contains("chaveApi").contains("Versão 2");
        assertThat(doc).contains("\"security\":[{\"chaveApi\":[]}]");
    }

    @Test
    void ordemDosGruposComecaPelasEntidadesDaParte1() throws Exception {
        String doc = v2();

        assertThat(doc.indexOf("\"name\":\"Categorias\"")).isLessThan(doc.indexOf("\"name\":\"Empréstimos\""));
        assertThat(doc.indexOf("\"name\":\"Empréstimos\"")).isLessThan(doc.indexOf("\"name\":\"Chaves de API\""));
    }

    @Test
    void oExemploDeIdempotenciaNaoTemUuidFixo() throws Exception {
        // Com um UUID fixo preenchido, o segundo clique devolveria a resposta guardada e pareceria defeito.
        assertThat(v2()).contains("X-Idempotency-Key").doesNotContain("\"example\":\"3f1c9a52");
    }

    @Test
    void versao1MantemOsErrosDaParte1() throws Exception {
        String doc = v1();

        assertThat(doc).contains("ErroResposta").contains("\"404\"").contains("\"400\"").contains("\"409\"");
    }

    @Test
    void versao2MostraAParte1EMaisAParte2() throws Exception {
        String doc = v2();

        // Parte 1
        for (String rota : new String[]{"\"/livros\"", "\"/autores\"", "\"/categorias\"", "\"/usuarios\"", "\"/carteirinhas\"", "\"/emprestimos\""}) {
            assertThat(doc).as("rota da Parte 1 na versão 2 " + rota).contains(rota);
        }
        // Parte 2: chave de API, idempotência, limite de requisições, versionamento
        assertThat(doc).contains("\"/chaves-api\"").contains("\"/reservas\"").contains("\"/auth/login\"").contains("/senha");
        assertThat(doc).contains("securitySchemes").contains("X-API-Key").contains("chaveApi");
        assertThat(doc).contains("X-Idempotency-Key").contains("X-API-Version");
        assertThat(doc).contains("\"429\"").contains("Retry-After").contains("\"401\"").contains("\"403\"");
    }

    @Test
    void cadaVersaoExplicaOQueMostraEUsaServidorRelativo() throws Exception {
        assertThat(v1()).contains("Versão 1 (Parte 1)").contains("\"url\":\"/\"").doesNotContain("localhost:8080");
        assertThat(v2()).contains("Versão 2 (Parte 1 + Parte 2)").contains("\"url\":\"/\"").doesNotContain("localhost:8080");
    }
}

package com.senac.biblioteca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Versionamento pelo cabeçalho X-API-Version: GET /livros e GET /livros/{id} têm as versões 1 e 2.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class VersionamentoTest extends ApiHttpTeste {

    private long livro;

    @BeforeEach
    void criarLivro() throws Exception {
        String codigo = "V" + unico().substring(0, 5);
        long categoria = id(comAdmin("POST", "/categorias", "{\"nome\":\"Romance " + codigo + "\",\"codigo\":\"" + codigo + "\"}"));
        long autor = id(comAdmin("POST", "/autores", "{\"nome\":\"Machado de Assis\",\"nacionalidade\":\"Brasileira\"}"));
        String isbn = "97" + Math.abs(UUID.randomUUID().getMostSignificantBits() % 10_000_000_000L);
        livro = id(comAdmin("POST", "/livros", "{\"titulo\":\"Dom Casmurro\",\"isbn\":\"" + isbn + "\",\"anoPublicacao\":1899,"
            + "\"editora\":\"Garnier\",\"numeroPaginas\":256,\"categoriaId\":" + categoria + ",\"autoresIds\":[" + autor + "]}"));
    }

    @Test
    void semOCabecalhoValeAVersao1ComObjetosCompletos() throws Exception {
        Resposta r = chamar("GET", "/livros/" + livro, null);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("X-API-Version")).isEqualTo("1");
        assertThat(r.corpo()).contains("\"categoria\":{").contains("\"autores\":[{");
    }

    @Test
    void versao1ExplicitaEhIgualAoPadrao() throws Exception {
        Resposta padrao = chamar("GET", "/livros/" + livro, null);
        Resposta v1 = chamar("GET", "/livros/" + livro, null, "X-API-Version", "1");

        assertThat(v1.status()).isEqualTo(200);
        assertThat(v1.cabecalho("X-API-Version")).isEqualTo("1");
        assertThat(v1.corpo()).isEqualTo(padrao.corpo());
    }

    @Test
    void versao2TrazCategoriaEAutoresPeloNome() throws Exception {
        Resposta r = chamar("GET", "/livros/" + livro, null, "X-API-Version", "2");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("X-API-Version")).isEqualTo("2");
        assertThat(r.corpo())
            .contains("\"categoria\":\"Romance ")
            .contains("\"autores\":[\"Machado de Assis\"]")
            .contains("\"quantidadeAutores\":1")
            .contains("\"self\":{\"href\":\"http://localhost");
    }

    @Test
    void listagemNaVersao2UsaOutraChaveNoEmbedded() throws Exception {
        Resposta v1 = chamar("GET", "/livros", null);
        Resposta v2 = chamar("GET", "/livros", null, "X-API-Version", "2");

        assertThat(v1.corpo()).contains("\"livroList\"");
        assertThat(v2.status()).isEqualTo(200);
        assertThat(v2.cabecalho("X-API-Version")).isEqualTo("2");
        assertThat(v2.corpo()).contains("\"_embedded\":{\"livros\"").contains("\"page\"");
    }

    @Test
    void rotasSemVersaoAtendemQualquerVersaoSuportada() throws Exception {
        assertThat(chamar("GET", "/autores", null, "X-API-Version", "2").status()).isEqualTo(200);
        assertThat(chamar("GET", "/categorias", null, "X-API-Version", "1").status()).isEqualTo(200);
    }

    @Test
    void versaoInexistenteResponde400DizendoAsVersoesDisponiveis() throws Exception {
        Resposta r = chamar("GET", "/livros", null, "X-API-Version", "3");

        assertThat(r.status()).isEqualTo(400);
        assertThat(campo(r, "erro")).isEqualTo("Versão da API não suportada");
        assertThat(campo(r, "mensagem")).contains("'3'").contains("1, 2");
        assertThat(chamar("GET", "/livros", null, "X-API-Version", "abc").status()).isEqualTo(400);
    }

    @Test
    void swaggerTemUmaDocumentacaoPorVersao() throws Exception {
        String configuracao = chamar("GET", "/v3/api-docs/swagger-config", null).corpo();
        assertThat(configuracao).contains("/v3/api-docs/v1").contains("/v3/api-docs/v2");

        String v1 = chamar("GET", "/v3/api-docs/v1", null).corpo();
        String v2 = chamar("GET", "/v3/api-docs/v2", null).corpo();
        assertThat(v1).contains("Buscar livro por ID (versão 1)").doesNotContain("(versão 2)");
        assertThat(v2).contains("Buscar livro por ID (versão 2)").contains("LivroV2").doesNotContain("(versão 1)");
        // As rotas sem versão aparecem nas duas, e as melhorias da documentação valem para ambas.
        assertThat(v2).contains("/autores").contains("ErroResposta").contains("X-API-Key");
    }
}

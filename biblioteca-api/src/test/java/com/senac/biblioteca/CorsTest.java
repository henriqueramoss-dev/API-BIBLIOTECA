package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CORS: o navegador só deixa o site chamar a API se as respostas autorizarem a origem dele.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsTest extends ApiHttpTeste {

    @Test
    void verificacaoPreviaDeOrigemPermitidaInformaMetodosECabecalhos() throws Exception {
        Resposta r = chamar("OPTIONS", "/livros", null,
            "Origin", "http://localhost:5173",
            "Access-Control-Request-Method", "POST",
            "Access-Control-Request-Headers", "content-type");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
        assertThat(r.cabecalho("Access-Control-Allow-Methods")).contains("GET", "POST", "PUT", "DELETE");
        assertThat(r.cabecalho("Access-Control-Allow-Headers").toLowerCase()).contains("content-type");
        assertThat(r.cabecalho("Access-Control-Max-Age")).isEqualTo("3600");
    }

    @Test
    void origemNaoPermitidaEhRecusada() throws Exception {
        Resposta r = chamar("OPTIONS", "/livros", null,
            "Origin", "https://site-desconhecido.com",
            "Access-Control-Request-Method", "GET");

        assertThat(r.status()).isEqualTo(403);
        assertThat(r.cabecalho("Access-Control-Allow-Origin")).isEmpty();
    }

    @Test
    void metodoNaoPermitidoEhRecusadoNaVerificacaoPrevia() throws Exception {
        Resposta r = chamar("OPTIONS", "/livros", null,
            "Origin", "http://localhost:5173",
            "Access-Control-Request-Method", "PATCH");

        assertThat(r.status()).isEqualTo(403);
    }

    @Test
    void requisicaoComumDeOrigemPermitidaRecebeOsCabecalhosDeCors() throws Exception {
        Resposta r = chamar("GET", "/livros", null, "Origin", "http://127.0.0.1:3000", "X-API-Key", CHAVE_CATALOGO);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("Access-Control-Allow-Origin")).isEqualTo("http://127.0.0.1:3000");
        assertThat(r.cabecalho("Access-Control-Expose-Headers")).contains("Location");
    }
}

package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Limite de requisições (rate limiting), com limites baixos para conseguir passar deles.
 *
 * Cada teste usa um "cliente" diferente (o IP, a chave ADMIN), porque as contagens são
 * por cliente e duram 1 minuto.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        "biblioteca.limite.requisicoes-por-minuto.sem-chave=3",
        "biblioteca.limite.requisicoes-por-minuto.com-chave=8"
    }
)
class LimiteDeRequisicoesTest extends ApiHttpTeste {

    @Test
    void semChaveOLimiteEhPorIpEPassandoDeleResponde429ComRetryAfter() throws Exception {
        for (int restantes = 2; restantes >= 0; restantes--) {
            Resposta r = chamar("GET", "/livros", null);
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.cabecalho("X-RateLimit-Limit")).isEqualTo("3");
            assertThat(r.cabecalho("X-RateLimit-Remaining")).isEqualTo(String.valueOf(restantes));
            assertThat(Long.parseLong(r.cabecalho("X-RateLimit-Reset"))).isBetween(1L, 60L);
        }

        Resposta bloqueada = chamar("GET", "/livros", null);
        assertThat(bloqueada.status()).isEqualTo(429);
        assertThat(Long.parseLong(bloqueada.cabecalho("Retry-After"))).isBetween(1L, 60L);
        assertThat(bloqueada.cabecalho("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(campo(bloqueada, "erro")).isEqualTo("Muitas requisições");
        assertThat(campo(bloqueada, "mensagem")).contains("Limite de 3 requisições por minuto");

        // Uma chave falsa não abre uma contagem nova: continua contando pelo IP.
        assertThat(chamar("GET", "/livros", null, "X-API-Key", "bib_chave_falsa_qualquer").status()).isEqualTo(429);
        // E o login também é limitado, o que dificulta tentar senhas sem parar.
        assertThat(chamar("POST", "/auth/login", "{\"email\":\"a@a.com\",\"senha\":\"x\"}").status()).isEqualTo(429);
    }

    @Test
    void comChaveOLimiteEhDaChaveEMaior() throws Exception {
        for (int i = 0; i < 8; i++) {
            Resposta r = comAdmin("GET", "/livros", null);
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.cabecalho("X-RateLimit-Limit")).isEqualTo("8");
        }

        Resposta bloqueada = comAdmin("GET", "/usuarios", null);
        assertThat(bloqueada.status()).isEqualTo(429);
        assertThat(bloqueada.cabecalho("Retry-After")).isNotEmpty();
    }

    @Test
    void swaggerEDocumentacaoNaoEntramNoLimite() throws Exception {
        for (int i = 0; i < 5; i++) {
            Resposta r = chamar("GET", "/v3/api-docs", null);
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.cabecalho("X-RateLimit-Limit")).isEmpty();
        }
    }

    @Test
    void swaggerDocumentaO429() throws Exception {
        String api = chamar("GET", "/v3/api-docs/v2", null).corpo();

        assertThat(api).contains("\"429\"").contains("Retry-After").contains("Muitas requisições");
    }
}

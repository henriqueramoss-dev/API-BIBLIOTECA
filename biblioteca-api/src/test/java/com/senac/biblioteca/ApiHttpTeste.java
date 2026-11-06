package com.senac.biblioteca;

import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base dos testes de integração dos recursos avançados (CORS, chave de API, limite de
 * requisições, idempotência e versionamento): chama a API de verdade por HTTP.
 */
abstract class ApiHttpTeste {

    @Value("${local.server.port}")
    private int porta;

    private final HttpClient http = HttpClient.newHttpClient();

    record Resposta(int status, String corpo, HttpResponse<String> http) {
        String cabecalho(String nome) {
            return http.headers().firstValue(nome).orElse("");
        }
    }

    /** Faz uma requisição. Os cabeçalhos vão em pares: nome, valor, nome, valor... */
    Resposta chamar(String metodo, String caminho, String corpo, String... cabecalhos) throws Exception {
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

    /** O id do registro está no link "self" da resposta HATEOAS. */
    long id(Resposta r) {
        Matcher m = Pattern.compile("\"self\":\\{\"href\":\"[^\"]*/(\\d+)\"").matcher(r.corpo());
        assertThat(m.find()).as("link self na resposta: " + r.corpo()).isTrue();
        return Long.parseLong(m.group(1));
    }

    /** Lê um campo de texto simples do JSON da resposta. */
    String campo(Resposta r, String nome) {
        Matcher m = Pattern.compile("\"" + nome + "\":\"([^\"]*)\"").matcher(r.corpo());
        assertThat(m.find()).as("campo " + nome + " na resposta: " + r.corpo()).isTrue();
        return m.group(1);
    }

    String unico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

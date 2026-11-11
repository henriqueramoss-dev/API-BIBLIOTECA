package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O acervo de exemplo (biblioteca.dados-exemplo=true) deixa o Swagger e o site com o que mostrar,
 * e os exemplos do Swagger cadastram na primeira tentativa.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE,
        "biblioteca.dados-exemplo=true"
    }
)
class DadosDeExemploTest extends ApiHttpTeste {

    private int total(String caminho) throws Exception {
        Resposta r = comAdmin("GET", caminho + "?size=1", null);
        assertThat(r.status()).as(r.corpo()).isEqualTo(200);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"totalElements\":(\\d+)").matcher(r.corpo());
        assertThat(m.find()).isTrue();
        return Integer.parseInt(m.group(1));
    }

    @Test
    void acervoDeExemploTemDadosEmTodasAsEntidades() throws Exception {
        assertThat(total("/categorias")).isGreaterThanOrEqualTo(5);
        assertThat(total("/autores")).isGreaterThanOrEqualTo(6);
        assertThat(total("/livros")).isGreaterThanOrEqualTo(12);
        assertThat(total("/usuarios")).isGreaterThanOrEqualTo(6);
        assertThat(total("/carteirinhas")).isGreaterThanOrEqualTo(4);
        assertThat(total("/emprestimos")).isGreaterThanOrEqualTo(4);
    }

    @Test
    void catalogoMostraLivrosEmprestadosEDisponiveis() throws Exception {
        String livros = chamar("GET", "/livros?size=100", null, "X-API-Key", CHAVE_CATALOGO).corpo();

        assertThat(livros).contains("\"situacao\":\"EMPRESTADO\"").contains("\"situacao\":\"DISPONIVEL\"");
    }

    @Test
    void clienteDeExemploEntraComAContaDocumentadaNoSwagger() throws Exception {
        Resposta r = chamar("POST", "/chaves-api", "{\"email\":\"beatriz@exemplo.com\",\"senha\":\"Cliente12345\",\"nome\":\"Swagger\",\"nivel\":\"LEITURA\"}");

        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        assertThat(campo(r, "usuarioTipo")).isEqualTo("ALUNO");
        // Os exemplos do corpo de POST /chaves-api funcionam como estão.
        assertThat(chamar("GET", "/v3/api-docs/v2", null).corpo()).contains("beatriz@exemplo.com").contains("Cliente12345");
    }

    @Test
    void osExemplosDeCadastroDoSwaggerFuncionamNaPrimeiraTentativa() throws Exception {
        // POST /carteirinhas: o exemplo usa o usuário 6, que não tem carteirinha.
        Resposta carteirinha = comAdmin("POST", "/carteirinhas",
            "{\"numero\":\"CB-000123\",\"dataEmissao\":\"2026-02-01\",\"dataValidade\":\"2027-02-01\",\"status\":\"ATIVA\",\"limiteEmprestimos\":3,\"usuarioId\":6}");
        assertThat(carteirinha.status()).as(carteirinha.corpo()).isEqualTo(201);

        // POST /livros: título e ISBN do exemplo não colidem com o acervo de exemplo.
        Resposta livro = comAdmin("POST", "/livros",
            "{\"titulo\":\"Dom Casmurro\",\"isbn\":\"9788535910663\",\"anoPublicacao\":1899,\"editora\":\"Companhia das Letras\",\"numeroPaginas\":256,\"categoriaId\":1,\"autoresIds\":[1]}");
        assertThat(livro.status()).as(livro.corpo()).isEqualTo(201);

        // POST /usuarios: o e-mail e o CPF do exemplo também estão livres.
        Resposta usuario = comAdmin("POST", "/usuarios",
            "{\"nome\":\"Henrique Ramos\",\"email\":\"henrique@email.com\",\"cpf\":\"12345678909\",\"tipo\":\"ALUNO\",\"telefone\":\"11999990000\","
                + "\"endereco\":\"Rua das Flores, 10\",\"cidade\":\"São Paulo\",\"estado\":\"SP\",\"cep\":\"01001000\",\"dataNascimento\":\"2000-05-10\"}");
        assertThat(usuario.status()).as(usuario.corpo()).isEqualTo(201);

        // POST /reservas: o livro 1 está na estante.
        Resposta reserva = chamar("POST", "/reservas", "{\"livroId\":1}", "X-API-Key", campo(
            chamar("POST", "/chaves-api", "{\"email\":\"beatriz@exemplo.com\",\"senha\":\"Cliente12345\",\"nome\":\"Swagger\"}"), "chave"));
        assertThat(reserva.status()).as(reserva.corpo()).isEqualTo(201);
    }
}

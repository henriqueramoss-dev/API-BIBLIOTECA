package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A conta do dono é criada ao iniciar a API quando e-mail e senha vêm da configuração.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE,
        "biblioteca.dono.email=dono@biblioteca.com",
        "biblioteca.dono.senha=senhaDoDono123",
        "biblioteca.dono.nome=Dona Maria"
    }
)
class DonoInicialTest extends ApiHttpTeste {

    private Resposta entrar(String email, String senha, String nivel) throws Exception {
        return chamar("POST", "/chaves-api", "{\"email\":\"" + email + "\",\"senha\":\"" + senha
            + "\",\"nome\":\"Site\",\"nivel\":\"" + nivel + "\"}");
    }

    @Test
    void donoConfiguradoEntraComoFuncionarioEPodeTerChaveAdmin() throws Exception {
        Resposta r = entrar("dono@biblioteca.com", "senhaDoDono123", "ADMIN");

        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        assertThat(campo(r, "usuarioTipo")).isEqualTo("FUNCIONARIO");
        assertThat(campo(r, "usuarioNome")).isEqualTo("Dona Maria");
        assertThat(campo(r, "nivel")).isEqualTo("ADMIN");
    }

    @Test
    void senhaErradaNaoEntra() throws Exception {
        assertThat(entrar("dono@biblioteca.com", "outraSenha999", "ADMIN").status()).isEqualTo(401);
    }

    @Test
    void senhaDoDonoNaoApareceNasRespostas() throws Exception {
        Resposta lista = comAdmin("GET", "/usuarios?size=100", null);

        assertThat(lista.corpo()).contains("dono@biblioteca.com").doesNotContain("senhaDoDono123").doesNotContain("$2");
    }
}

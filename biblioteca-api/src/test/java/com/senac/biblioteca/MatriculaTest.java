package com.senac.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A matrícula é gerada pela API: quem cadastra não escolhe nem altera o número.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        ApiHttpTeste.PROPRIEDADE_CHAVE_ADMIN,
        ApiHttpTeste.SEM_LIMITE_SEM_CHAVE,
        ApiHttpTeste.SEM_LIMITE_COM_CHAVE
    }
)
class MatriculaTest extends ApiHttpTeste {

    private String esperada(long id) {
        return LocalDate.now().getYear() + String.format("%06d", id);
    }

    @Test
    void cadastroGeraAMatriculaSozinho() throws Exception {
        Resposta r = comAdmin("POST", "/usuarios", usuarioJson("mat-" + unico() + "@email.com", "ALUNO", null));

        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        assertThat(campo(r, "matricula")).isEqualTo(esperada(id(r)));
    }

    @Test
    void matriculaEnviadaNoCadastroEhIgnorada() throws Exception {
        String corpo = usuarioJson("mat-" + unico() + "@email.com", "VISITANTE", null).replace("{", "{\"matricula\":\"MINHA-123\",");

        Resposta r = chamar("POST", "/usuarios", corpo); // autocadastro, sem chave

        assertThat(r.status()).as(r.corpo()).isEqualTo(201);
        assertThat(campo(r, "matricula")).isEqualTo(esperada(id(r))).isNotEqualTo("MINHA-123");
    }

    @Test
    void cadaUsuarioRecebeUmaMatriculaDiferente() throws Exception {
        Resposta a = comAdmin("POST", "/usuarios", usuarioJson("mat-" + unico() + "@email.com", "ALUNO", null));
        Resposta b = comAdmin("POST", "/usuarios", usuarioJson("mat-" + unico() + "@email.com", "ALUNO", null));

        assertThat(campo(a, "matricula")).isNotEqualTo(campo(b, "matricula"));
    }

    @Test
    void editarUsuarioNaoMudaAMatricula() throws Exception {
        Resposta criado = comAdmin("POST", "/usuarios", usuarioJson("mat-" + unico() + "@email.com", "ALUNO", null));
        String original = campo(criado, "matricula");
        String corpo = usuarioJson("mat-" + unico() + "@email.com", "ALUNO", null).replace("{", "{\"matricula\":\"TROCADA-999\",");

        Resposta atualizado = comAdmin("PUT", "/usuarios/" + id(criado), corpo);

        assertThat(atualizado.status()).as(atualizado.corpo()).isEqualTo(200);
        assertThat(campo(atualizado, "matricula")).isEqualTo(original);
    }
}

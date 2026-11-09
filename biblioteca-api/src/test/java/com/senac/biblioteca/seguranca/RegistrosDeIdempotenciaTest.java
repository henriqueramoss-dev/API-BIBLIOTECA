package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.seguranca.RegistrosDeIdempotencia.RespostaGuardada;
import com.senac.biblioteca.seguranca.RegistrosDeIdempotencia.Situacao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de unidade dos registros de idempotência (sem subir a API).
 */
class RegistrosDeIdempotenciaTest {

    private final RegistrosDeIdempotencia registros = new RegistrosDeIdempotencia(24);
    private final RespostaGuardada resposta = new RespostaGuardada(201, "application/json", "/autores/1", "{}".getBytes());

    @Test
    void primeiraVezEhNovaEEnquantoNaoTerminaFicaEmAndamento() {
        assertThat(registros.iniciar("cliente|chave", "impressao").situacao()).isEqualTo(Situacao.NOVA);
        assertThat(registros.iniciar("cliente|chave", "impressao").situacao()).isEqualTo(Situacao.EM_ANDAMENTO);
    }

    @Test
    void depoisDeConcluidaARespostaEhRepetida() {
        registros.iniciar("cliente|chave", "impressao");
        registros.concluir("cliente|chave", resposta);

        RegistrosDeIdempotencia.Resultado repetida = registros.iniciar("cliente|chave", "impressao");
        assertThat(repetida.situacao()).isEqualTo(Situacao.REPETIDA);
        assertThat(repetida.resposta()).isSameAs(resposta);
    }

    @Test
    void mesmaChaveComOutraRequisicaoEhReutilizada() {
        registros.iniciar("cliente|chave", "impressao");
        registros.concluir("cliente|chave", resposta);

        assertThat(registros.iniciar("cliente|chave", "outra").situacao()).isEqualTo(Situacao.REUTILIZADA);
    }

    @Test
    void cancelarLiberaAChave() {
        registros.iniciar("cliente|chave", "impressao");
        registros.cancelar("cliente|chave");

        assertThat(registros.iniciar("cliente|chave", "outra").situacao()).isEqualTo(Situacao.NOVA);
    }

    @Test
    void registroVencidoEhIgnorado() {
        RegistrosDeIdempotencia semValidade = new RegistrosDeIdempotencia(0);
        semValidade.iniciar("cliente|chave", "impressao");
        semValidade.concluir("cliente|chave", resposta);

        assertThat(semValidade.iniciar("cliente|chave", "outra").situacao()).isEqualTo(Situacao.NOVA);
    }
}

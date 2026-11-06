package com.senac.biblioteca.seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Guarda, por um tempo, o resultado dos POSTs enviados com X-Idempotency-Key.
 *
 * Os registros ficam na memória da API e somem ao reiniciar (o banco também é em memória).
 * Com mais de uma instância da API, o certo seria guardar num lugar compartilhado, como o Redis
 * ou uma tabela no banco.
 */
@Component
public class RegistrosDeIdempotencia {

    /** A cada quantas requisições os registros vencidos são apagados da memória. */
    private static final int LIMPEZA_A_CADA = 500;

    /** Resposta de sucesso guardada para ser repetida. */
    public record RespostaGuardada(int status, String tipoDeConteudo, String endereco, byte[] corpo) {
    }

    /** O que fazer com a requisição que chegou. */
    public enum Situacao {
        /** Primeira vez com esta chave: processar normalmente. */
        NOVA,
        /** Mesma requisição já concluída: devolver a resposta guardada. */
        REPETIDA,
        /** A primeira requisição com esta chave ainda não terminou. */
        EM_ANDAMENTO,
        /** A chave já foi usada com uma requisição diferente. */
        REUTILIZADA
    }

    public record Resultado(Situacao situacao, RespostaGuardada resposta) {
    }

    private record Registro(String impressao, long expiraEm, RespostaGuardada resposta) {
        boolean venceu(long agora) {
            return agora >= expiraEm;
        }
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final AtomicInteger requisicoesDesdeALimpeza = new AtomicInteger();
    private final long validadeEmMilissegundos;

    public RegistrosDeIdempotencia(@Value("${biblioteca.idempotencia.validade-horas:24}") long validadeEmHoras) {
        this.validadeEmMilissegundos = Duration.ofHours(validadeEmHoras).toMillis();
    }

    /**
     * Registra o início de uma requisição.
     *
     * @param escopo    cliente + chave de idempotência (chaves iguais de clientes diferentes não se misturam)
     * @param impressao resumo do método, do endereço e do corpo, para saber se é a mesma requisição
     */
    public Resultado iniciar(String escopo, String impressao) {
        long agora = System.currentTimeMillis();
        limparDeVezEmQuando(agora);

        AtomicReference<Resultado> resultado = new AtomicReference<>();
        // compute() é atômico por escopo: com duas requisições iguais ao mesmo tempo, só uma é NOVA.
        registros.compute(escopo, (chave, atual) -> {
            if (atual == null || atual.venceu(agora)) {
                resultado.set(new Resultado(Situacao.NOVA, null));
                return new Registro(impressao, agora + validadeEmMilissegundos, null);
            }
            if (!atual.impressao().equals(impressao)) {
                resultado.set(new Resultado(Situacao.REUTILIZADA, null));
            } else if (atual.resposta() == null) {
                resultado.set(new Resultado(Situacao.EM_ANDAMENTO, null));
            } else {
                resultado.set(new Resultado(Situacao.REPETIDA, atual.resposta()));
            }
            return atual;
        });
        return resultado.get();
    }

    /** Guarda a resposta de sucesso da requisição. */
    public void concluir(String escopo, RespostaGuardada resposta) {
        registros.computeIfPresent(escopo, (chave, atual) ->
            new Registro(atual.impressao(), atual.expiraEm(), resposta));
    }

    /** Esquece a chave (a requisição falhou): o cliente pode corrigir e tentar de novo com ela. */
    public void cancelar(String escopo) {
        registros.remove(escopo);
    }

    private void limparDeVezEmQuando(long agora) {
        if (requisicoesDesdeALimpeza.incrementAndGet() >= LIMPEZA_A_CADA) {
            requisicoesDesdeALimpeza.set(0);
            registros.values().removeIf(registro -> registro.venceu(agora));
        }
    }
}

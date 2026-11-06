package com.senac.biblioteca.seguranca;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conta as requisições de cada cliente em janelas fixas de 1 minuto.
 *
 * Exemplo com limite 60: a primeira requisição abre uma janela de 60 segundos; as próximas
 * 59 são aceitas; da 61ª em diante, até a janela acabar, a resposta é 429. Quando a janela
 * termina, a contagem recomeça do zero.
 *
 * As contagens ficam na memória da API (somem ao reiniciar). Com mais de uma instância da API,
 * cada uma contaria separado; nesse caso, o certo seria guardar as contagens num lugar
 * compartilhado, como o Redis.
 */
@Component
public class LimitadorDeRequisicoes {

    /** Duração de cada janela de contagem. */
    static final long JANELA_EM_MILISSEGUNDOS = 60_000;

    /** A cada quantas requisições as janelas vencidas são apagadas da memória. */
    private static final int LIMPEZA_A_CADA = 1_000;

    /** Resultado de uma requisição contada. */
    public record Consumo(boolean permitido, int limite, int restantes, long segundosParaReiniciar) {
    }

    private record Janela(long inicio, AtomicInteger contagem) {
        boolean venceu(long agora) {
            return agora - inicio >= JANELA_EM_MILISSEGUNDOS;
        }
    }

    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final AtomicInteger requisicoesDesdeALimpeza = new AtomicInteger();

    /** Conta mais uma requisição do cliente e diz se ela pode seguir. */
    public Consumo registrar(String cliente, int limite) {
        long agora = System.currentTimeMillis();
        limparDeVezEmQuando(agora);

        // compute() é atômico por cliente: duas requisições ao mesmo tempo não se perdem na contagem.
        Janela janela = janelas.compute(cliente, (chave, atual) ->
            atual == null || atual.venceu(agora) ? new Janela(agora, new AtomicInteger()) : atual);
        int usadas = janela.contagem().incrementAndGet();

        long milissegundosRestantes = janela.inicio() + JANELA_EM_MILISSEGUNDOS - agora;
        // Arredonda para cima: com 0,2 s faltando, o cliente deve esperar 1 s, não 0.
        long segundosParaReiniciar = Math.max(1, (milissegundosRestantes + 999) / 1000);

        return new Consumo(usadas <= limite, limite, Math.max(0, limite - usadas), segundosParaReiniciar);
    }

    private void limparDeVezEmQuando(long agora) {
        if (requisicoesDesdeALimpeza.incrementAndGet() >= LIMPEZA_A_CADA) {
            requisicoesDesdeALimpeza.set(0);
            janelas.values().removeIf(janela -> janela.venceu(agora));
        }
    }
}

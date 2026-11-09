package com.senac.biblioteca.seguranca;

import org.springframework.core.Ordered;

/**
 * Ordem em que os filtros da API rodam (do menor número para o maior).
 *
 *  1. CORS (CorsConfig): Ordered.HIGHEST_PRECEDENCE, antes de todos;
 *  2. limite de requisições: recusa com 429 quem passou do limite, antes de qualquer outro trabalho;
 *  3. chave de API: confere se quem chama pode usar a rota;
 *  4. idempotência: repete a resposta guardada de um POST já processado. Fica depois da chave
 *     de API para nunca devolver uma resposta guardada a quem não pode usar a rota.
 */
public final class OrdemDosFiltros {

    public static final int LIMITE_DE_REQUISICOES = Ordered.HIGHEST_PRECEDENCE + 10;

    public static final int CHAVE_API = Ordered.HIGHEST_PRECEDENCE + 20;

    public static final int IDEMPOTENCIA = Ordered.HIGHEST_PRECEDENCE + 30;

    private OrdemDosFiltros() {
    }
}

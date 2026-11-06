package com.senac.biblioteca.seguranca;

import org.springframework.core.Ordered;

/**
 * Ordem em que os filtros da API rodam (do menor número para o maior).
 *
 *  1. CORS (CorsConfig): Ordered.HIGHEST_PRECEDENCE, antes de todos;
 *  2. limite de requisições: recusa com 429 quem passou do limite, antes de qualquer outro trabalho;
 *  3. chave de API: confere se quem chama pode usar a rota.
 */
public final class OrdemDosFiltros {

    public static final int LIMITE_DE_REQUISICOES = Ordered.HIGHEST_PRECEDENCE + 10;

    public static final int CHAVE_API = Ordered.HIGHEST_PRECEDENCE + 20;

    private OrdemDosFiltros() {
    }
}

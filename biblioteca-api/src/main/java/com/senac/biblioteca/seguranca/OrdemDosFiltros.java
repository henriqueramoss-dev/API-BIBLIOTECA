package com.senac.biblioteca.seguranca;

import org.springframework.core.Ordered;

/**
 * Ordem em que os filtros da API rodam (do menor número para o maior).
 *
 *  1. CORS (CorsConfig): Ordered.HIGHEST_PRECEDENCE, antes de todos;
 *  2. chave de API: identifica quem chama e confere se pode usar a rota.
 */
public final class OrdemDosFiltros {

    public static final int CHAVE_API = Ordered.HIGHEST_PRECEDENCE + 20;

    private OrdemDosFiltros() {
    }
}

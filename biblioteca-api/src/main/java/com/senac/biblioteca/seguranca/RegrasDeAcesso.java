package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.enums.NivelAcesso;

import java.util.List;

/**
 * Diz qual nível de chave de API cada rota exige.
 *
 * É o único lugar com essas regras: o filtro ChaveApiFilter usa para liberar ou recusar
 * as requisições, e o Swagger usa para mostrar quais rotas pedem a chave.
 *
 * Rotas públicas (sem chave):
 *  - consultas do catálogo: GET de livros, autores e categorias (menos o histórico de
 *    empréstimos de um livro, que mostra quem pegou o livro);
 *  - POST /auth/login e POST /chaves-api (conferem e-mail e senha no corpo);
 *  - POST /usuarios (autocadastro: sem chave de ESCRITA, só aceita ALUNO e VISITANTE);
 *  - PUT /usuarios/{id}/senha (exige a senha atual; definir a primeira senha exige chave de ESCRITA);
 *  - Swagger e demais endereços que não são da API.
 *
 * Rotas protegidas:
 *  - LEITURA: GET de usuários, carteirinhas, empréstimos e das próprias chaves;
 *    também revogar (DELETE) uma chave própria;
 *  - ESCRITA: POST e PUT;
 *  - ADMIN: DELETE.
 */
public final class RegrasDeAcesso {

    /** Recursos da API. Endereços fora desta lista (Swagger, /v3/api-docs...) são públicos. */
    private static final List<String> RECURSOS = List.of(
        "/usuarios", "/carteirinhas", "/emprestimos", "/livros", "/autores", "/categorias", "/chaves-api", "/reservas", "/auth"
    );

    /** Recursos que formam o catálogo público: qualquer um pode consultar. */
    private static final List<String> CATALOGO = List.of("/livros", "/autores", "/categorias");

    private RegrasDeAcesso() {
    }

    /**
     * Nível exigido para o método e o caminho informados, ou null quando a rota é pública.
     * O caminho pode ser o real (/livros/1) ou o modelo do Swagger (/livros/{id}).
     */
    public static NivelAcesso nivelExigido(String metodo, String caminho) {
        String recurso = recursoDe(caminho);
        if (recurso == null || "OPTIONS".equals(metodo) || "HEAD".equals(metodo)) {
            return null;
        }

        if ("/auth".equals(recurso)
            || ("POST".equals(metodo) && caminho.equals("/chaves-api"))
            || ("POST".equals(metodo) && caminho.equals("/usuarios"))
            || ("PUT".equals(metodo) && caminho.matches("/usuarios/[^/]+/senha"))) {
            return null;
        }

        // Reservas e chaves são "do próprio cliente": basta a chave de LEITURA e o serviço confere quem é o dono.
        if ("/reservas".equals(recurso)) {
            return NivelAcesso.LEITURA;
        }
        return switch (metodo) {
            case "GET" -> {
                if (CATALOGO.contains(recurso) && !caminho.endsWith("/emprestimos")) {
                    yield null;
                }
                // Dados de outras pessoas (usuários, carteirinhas, empréstimos) são da equipe da biblioteca:
                // clientes só têm chave de LEITURA, então não conseguem ler o que é de outros clientes.
                yield "/chaves-api".equals(recurso) ? NivelAcesso.LEITURA : NivelAcesso.ESCRITA;
            }
            // Cada um revoga as próprias chaves; o serviço confere quem é o dono.
            case "DELETE" -> "/chaves-api".equals(recurso) ? NivelAcesso.LEITURA : NivelAcesso.ADMIN;
            default -> NivelAcesso.ESCRITA;
        };
    }

    /** Diz se o caminho é de um recurso da API (e não do Swagger ou de outro endereço). */
    public static boolean ehRotaDaApi(String caminho) {
        return recursoDe(caminho) != null;
    }

    private static String recursoDe(String caminho) {
        for (String recurso : RECURSOS) {
            if (caminho.equals(recurso) || caminho.startsWith(recurso + "/")) {
                return recurso;
            }
        }
        return null;
    }
}

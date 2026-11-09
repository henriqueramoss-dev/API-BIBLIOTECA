package com.senac.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.entity.Livro;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.util.Comparator;
import java.util.List;

/**
 * Livro na versão 2 da API (cabeçalho X-API-Version: 2).
 *
 * Diferença para a versão 1: a categoria e os autores vêm só pelo nome, em vez de objetos
 * completos, o que deixa a resposta menor e mais fácil de mostrar numa lista. Os IDs continuam
 * disponíveis nos links HATEOAS (categoria e autores). Na listagem, os itens ficam em
 * "_embedded.livros" (na versão 1, em "_embedded.livroList").
 */
@Relation(itemRelation = "livro", collectionRelation = "livros")
@JsonPropertyOrder({"id", "titulo", "isbn", "anoPublicacao", "editora", "numeroPaginas", "categoria", "autores", "quantidadeAutores"})
@Schema(name = "LivroV2", description = "Livro no formato da versão 2: categoria e autores pelo nome")
public class LivroV2 {

    @Schema(description = "Identificador do livro", example = "1")
    private final Long id;

    @Schema(description = "Título do livro", example = "Dom Casmurro")
    private final String titulo;

    @Schema(description = "ISBN do livro", example = "9788535910663")
    private final String isbn;

    @Schema(description = "Ano de publicação", example = "1899")
    private final Integer anoPublicacao;

    @Schema(description = "Editora do livro", example = "Companhia das Letras")
    private final String editora;

    @Schema(description = "Número de páginas", example = "256")
    private final Integer numeroPaginas;

    @Schema(description = "Nome da categoria", example = "Romance")
    private final String categoria;

    @Schema(description = "Nomes dos autores, em ordem alfabética", example = "[\"Machado de Assis\"]")
    private final List<String> autores;

    public LivroV2(Livro livro) {
        this.id = livro.getId();
        this.titulo = livro.getTitulo();
        this.isbn = livro.getIsbn();
        this.anoPublicacao = livro.getAnoPublicacao();
        this.editora = livro.getEditora();
        this.numeroPaginas = livro.getNumeroPaginas();
        this.categoria = livro.getCategoria() == null ? null : livro.getCategoria().getNome();
        this.autores = livro.getAutores().stream()
            .map(Autor::getNome)
            .sorted(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getIsbn() { return isbn; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public String getEditora() { return editora; }
    public Integer getNumeroPaginas() { return numeroPaginas; }
    public String getCategoria() { return categoria; }
    public List<String> getAutores() { return autores; }

    @Schema(description = "Quantidade de autores", example = "1")
    public int getQuantidadeAutores() {
        return autores.size();
    }
}

package com.senac.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Dados recebidos para criar ou atualizar um livro.
 * Categoria e autores são informados somente pelos seus IDs.
 */
public class LivroRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    @Schema(description = "Título do livro", example = "Dom Casmurro")
    private String titulo;

    @NotBlank(message = "O ISBN é obrigatório")
    @Size(max = 20, message = "O ISBN deve ter no máximo 20 caracteres")
    @Schema(description = "ISBN do livro", example = "9788535910663")
    private String isbn;

    @NotNull(message = "O ano de publicação é obrigatório")
    @Min(value = 1, message = "O ano de publicação deve ser válido")
    @Schema(description = "Ano de publicação", example = "1899")
    private Integer anoPublicacao;

    @NotBlank(message = "A editora é obrigatória")
    @Size(max = 100, message = "A editora deve ter no máximo 100 caracteres")
    @Schema(description = "Editora do livro", example = "Companhia das Letras")
    private String editora;

    @NotNull(message = "O número de páginas é obrigatório")
    @Min(value = 1, message = "O número de páginas deve ser maior que zero")
    @Schema(description = "Número de páginas", example = "256")
    private Integer numeroPaginas;

    @NotNull(message = "O ID da categoria é obrigatório")
    @Schema(description = "ID de uma categoria já cadastrada", example = "1")
    private Long categoriaId;

    @Schema(description = "IDs dos autores já cadastrados", example = "[1]")
    private Set<Long> autoresIds = new HashSet<>();

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }
    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }
    public Integer getNumeroPaginas() { return numeroPaginas; }
    public void setNumeroPaginas(Integer numeroPaginas) { this.numeroPaginas = numeroPaginas; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public Set<Long> getAutoresIds() { return autoresIds; }
    public void setAutoresIds(Set<Long> autoresIds) { this.autoresIds = autoresIds; }
}

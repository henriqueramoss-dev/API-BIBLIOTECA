package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade usada para classificar os livros do acervo.
 * Exemplos: Romance, Tecnologia, História e Ficção.
 */
@Entity
@Table(name = "categorias")
@Schema(description = "Categoria utilizada para classificar livros do acervo")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    // Nome obrigatório da categoria.
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
    @Schema(description = "Nome da categoria", example = "Romance")
    private String nome;

    // Código curto e único usado na etiqueta dos livros.
    @NotBlank(message = "O código é obrigatório")
    @Size(max = 10, message = "O código deve ter no máximo 10 caracteres")
    @Column(nullable = false, unique = true, length = 10)
    @Schema(description = "Código único da categoria (gravado em maiúsculas)", example = "ROM")
    private String codigo;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição da categoria (opcional)", example = "Narrativas de ficção em prosa.")
    private String descricao;

    @Size(max = 50, message = "A localização deve ter no máximo 50 caracteres")
    @Schema(description = "Onde os livros ficam na biblioteca (opcional)", example = "Estante A")
    private String localizacao;

    @Size(max = 50, message = "O público-alvo deve ter no máximo 50 caracteres")
    @Schema(description = "Público-alvo (opcional)", example = "Adulto")
    private String publicoAlvo;

    // One-to-Many: uma categoria pode possuir vários livros.
    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Livro> livros = new ArrayList<>();

    public Categoria() {
    }

    public Long getId() {
        return id;
    }

    /**
     * Usado pela camada Service para garantir que novos cadastros
     * sempre recebam o ID gerado automaticamente pelo banco.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    /**
     * O código é gravado sem espaços nas pontas e em maiúsculas ("rom" vira "ROM"),
     * para que a regra "código único" não seja contornada trocando maiúsculas por minúsculas.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getPublicoAlvo() {
        return publicoAlvo;
    }

    public void setPublicoAlvo(String publicoAlvo) {
        this.publicoAlvo = publicoAlvo;
    }

    public List<Livro> getLivros() {
        return livros;
    }
}

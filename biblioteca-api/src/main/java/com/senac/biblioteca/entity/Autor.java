package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade que representa um autor cadastrado no acervo.
 * Um autor pode participar de vários livros.
 */
@Entity
@Table(name = "autores")
@Schema(description = "Autor responsável por uma ou mais obras do acervo")
public class Autor {

    // Chave primária gerada automaticamente pelo banco.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    // Nome obrigatório do autor, validado antes de salvar.
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo do autor", example = "Machado de Assis")
    private String nome;

    @NotBlank(message = "A nacionalidade é obrigatória")
    @Size(max = 60, message = "A nacionalidade deve ter no máximo 60 caracteres")
    @Schema(description = "Nacionalidade do autor", example = "Brasileira")
    private String nacionalidade;

    @Past(message = "A data de nascimento deve estar no passado")
    @Schema(description = "Data de nascimento do autor (opcional)", example = "1839-06-21")
    private LocalDate dataNascimento;

    @Size(max = 60, message = "O gênero literário deve ter no máximo 60 caracteres")
    @Schema(description = "Gênero literário principal (opcional)", example = "Romance")
    private String generoLiterario;

    @Size(max = 1000, message = "A biografia deve ter no máximo 1000 caracteres")
    @Column(length = 1000)
    @Schema(description = "Biografia resumida (opcional)", example = "Fundador da Academia Brasileira de Letras.")
    private String biografia;

    // Lado inverso do Many-to-Many: um autor pode estar relacionado a vários livros.
    @JsonIgnore
    @ManyToMany(mappedBy = "autores")
    private Set<Livro> livros = new HashSet<>();

    public Autor() {
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

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getGeneroLiterario() {
        return generoLiterario;
    }

    public void setGeneroLiterario(String generoLiterario) {
        this.generoLiterario = generoLiterario;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public Set<Livro> getLivros() {
        return livros;
    }
}

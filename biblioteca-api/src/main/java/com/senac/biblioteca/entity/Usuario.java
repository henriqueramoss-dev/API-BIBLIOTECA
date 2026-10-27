package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senac.biblioteca.enums.TipoUsuario;
import com.senac.biblioteca.validation.Cpf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma pessoa cadastrada na biblioteca.
 * @Entity informa ao JPA que a classe será persistida no banco de dados.
 */
@Entity
@Table(name = "usuarios")
@Schema(description = "Usuário cadastrado na biblioteca")
public class Usuario {

    // Identificador único gerado automaticamente pelo banco.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    // Bean Validation impede nome vazio e limita seu tamanho.
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo do usuário", example = "Henrique Ramos")
    private String nome;

    // O e-mail é obrigatório, deve ter formato válido e não pode se repetir.
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Column(nullable = false, unique = true, length = 150)
    @Schema(description = "E-mail do usuário", example = "henrique@email.com")
    private String email;

    // CPF com 11 dígitos, sem pontos ou traço, e único no sistema.
    @NotBlank(message = "O CPF é obrigatório")
    @Cpf
    @Column(nullable = false, unique = true, length = 11)
    @Schema(description = "CPF do usuário (somente números)", example = "12345678909")
    private String cpf;

    @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres")
    @Schema(description = "Matrícula na instituição (opcional)", example = "2026001")
    private String matricula;

    @NotNull(message = "O tipo de usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de usuário", example = "ALUNO")
    private TipoUsuario tipo;

    // Relacionamento One-to-One: um usuário possui um único perfil.
    @JsonIgnore
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Perfil perfil;

    // Relacionamento One-to-Many: um usuário pode possuir vários empréstimos.
    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<Emprestimo> emprestimos = new ArrayList<>();

    // Construtor vazio exigido pelo JPA.
    public Usuario() {
    }

    // Getters permitem a leitura dos atributos; setters permitem suas alterações.
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public List<Emprestimo> getEmprestimos() {
        return emprestimos;
    }
}

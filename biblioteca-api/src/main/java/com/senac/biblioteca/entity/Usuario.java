package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.senac.biblioteca.enums.TipoUsuario;
import com.senac.biblioteca.validation.Cpf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma pessoa cadastrada na biblioteca, com os dados de contato e de nascimento.
 * (Antes esses dados ficavam em uma entidade Perfil separada; hoje o cadastro é um só.)
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
    @Schema(description = "Matrícula gerada automaticamente no cadastro (ano + número sequencial). Não pode ser informada nem alterada.",
        accessMode = Schema.AccessMode.READ_ONLY, example = "2026000012")
    private String matricula;

    @NotNull(message = "O tipo de usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de usuário", example = "ALUNO")
    private TipoUsuario tipo;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Schema(description = "Telefone do usuário", example = "11999999999")
    private String telefone;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    @Schema(description = "Endereço do usuário", example = "Rua Exemplo, 100")
    private String endereco;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
    @Schema(description = "Cidade", example = "São Paulo")
    private String cidade;

    @NotBlank(message = "O estado é obrigatório")
    @Pattern(regexp = "[A-Z]{2}", message = "O estado deve ser a sigla com 2 letras maiúsculas (ex.: SP)")
    @Schema(description = "Sigla do estado", example = "SP")
    private String estado;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 dígitos, sem traço")
    @Schema(description = "CEP (somente números)", example = "01001000")
    private String cep;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Schema(description = "Data de nascimento", example = "2000-05-10")
    private LocalDate dataNascimento;

    // Senha em texto puro: só chega pelo JSON do POST /usuarios (WRITE_ONLY), nunca é gravada nem devolvida.
    // O Service transforma esta senha em hash (BCrypt) e guarda o hash em senhaHash.
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres")
    @Schema(
        description = "Senha de acesso (opcional, de 8 a 72 caracteres). Só é lida no cadastro e nunca é devolvida. "
            + "Para trocá-la depois, use PUT /usuarios/{id}/senha.",
        accessMode = Schema.AccessMode.WRITE_ONLY,
        example = "minhaSenha123"
    )
    private String senha;

    // Hash BCrypt da senha (60 caracteres). @JsonIgnore: nunca aparece nas respostas nem nas requisições.
    @JsonIgnore
    @Column(length = 60)
    private String senhaHash;

    // Relacionamento One-to-One: um usuário possui uma única carteirinha da biblioteca.
    @JsonIgnore
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Carteirinha carteirinha;

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Carteirinha getCarteirinha() {
        return carteirinha;
    }

    public void setCarteirinha(Carteirinha carteirinha) {
        this.carteirinha = carteirinha;
    }

    public List<Emprestimo> getEmprestimos() {
        return emprestimos;
    }
}

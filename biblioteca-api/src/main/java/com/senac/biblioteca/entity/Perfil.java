package com.senac.biblioteca.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Entidade que guarda informações complementares do usuário.
 * Ela demonstra o relacionamento One-to-One exigido no projeto.
 */
@Entity
@Table(name = "perfis")
@Schema(description = "Perfil com informações complementares do usuário")
public class Perfil {

    // Identificador único do perfil.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador gerado automaticamente pelo banco", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    // Valida o telefone informado pelo cliente da API.
    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Schema(description = "Telefone do usuário", example = "11999999999")
    private String telefone;

    // Endereço é obrigatório e possui limite de tamanho.
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

    // Relacionamento One-to-One: cada perfil pertence a um único usuário.
    @NotNull(message = "O usuário é obrigatório")
    // O CPF do usuário não é repetido aqui: ele só aparece nas rotas de /usuarios.
    @JsonIgnoreProperties("cpf")
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Construtor vazio necessário para o JPA.
    public Perfil() {
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}

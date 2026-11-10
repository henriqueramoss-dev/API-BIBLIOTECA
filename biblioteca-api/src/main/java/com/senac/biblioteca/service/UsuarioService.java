package com.senac.biblioteca.service;

import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.enums.TipoUsuario;
import com.senac.biblioteca.exception.AcessoNegadoException;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Camada de serviço de Usuario.
 * Concentra as regras de negócio e faz a ligação entre Controller e Repository.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // Lista os usuários respeitando a paginação recebida pelo Controller.
    public Page<Usuario> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca um usuário pelo id; Optional representa a possibilidade de não existir.
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    /**
     * Salva um novo usuário no banco.
     *
     * nivelDoChamador é o nível da chave de API usada (null quando não veio chave). Sem uma chave
     * de ESCRITA, é um autocadastro e só aceita os tipos ALUNO e VISITANTE: assim ninguém se
     * cadastra sozinho como FUNCIONARIO para depois gerar uma chave ADMIN.
     */
    public Usuario criar(Usuario usuario, NivelAcesso nivelDoChamador) {
        boolean podeEscrever = nivelDoChamador != null && nivelDoChamador.permite(NivelAcesso.ESCRITA);
        if (!podeEscrever && usuario.getTipo() != TipoUsuario.ALUNO && usuario.getTipo() != TipoUsuario.VISITANTE) {
            throw new AcessoNegadoException("Sem uma chave de API de nível ESCRITA, o cadastro só aceita os tipos "
                + "ALUNO e VISITANTE. Usuários " + usuario.getTipo() + " são cadastrados pela biblioteca.");
        }

        // A matrícula nunca vem de quem cadastra: é gerada abaixo, depois que o banco define o ID.
        usuario.setMatricula(null);

        // O ID de um novo recurso sempre deve ser gerado pelo banco.
        // Isso também evita erro caso o Swagger envie um ID de exemplo.
        usuario.setId(null);

        // Se veio uma senha, só o hash dela é guardado; a senha em texto puro é descartada.
        if (usuario.getSenha() != null) {
            usuario.setSenhaHash(encoder.encode(usuario.getSenha()));
            usuario.setSenha(null);
        }

        Usuario salvo = repository.save(usuario);
        // Matrícula = ano do cadastro + ID com 6 dígitos (ex.: 2026000012). Como o ID é único, ela também é.
        salvo.setMatricula(LocalDate.now().getYear() + String.format("%06d", salvo.getId()));
        return repository.save(salvo);
    }

    // Atualiza somente se o usuário informado existir. A senha não é alterada aqui (use PUT /usuarios/{id}/senha).
    public Optional<Usuario> atualizar(Long id, Usuario dados) {
        return repository.findById(id)
            .map(usuario -> {
                usuario.setNome(dados.getNome());
                usuario.setEmail(dados.getEmail());
                usuario.setCpf(dados.getCpf());
                // A matrícula não muda: ela é gerada no cadastro.
                usuario.setTipo(dados.getTipo());
                usuario.setTelefone(dados.getTelefone());
                usuario.setEndereco(dados.getEndereco());
                usuario.setCidade(dados.getCidade());
                usuario.setEstado(dados.getEstado());
                usuario.setCep(dados.getCep());
                usuario.setDataNascimento(dados.getDataNascimento());
                return repository.save(usuario);
            });
    }

    // Retorna false se o id não existir e true quando a exclusão for realizada.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por parte do nome, também paginada.
    public Page<Usuario> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}

package com.senac.biblioteca.service;

import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // Salva um novo usuário no banco.
    public Usuario criar(Usuario usuario) {
        // O ID de um novo recurso sempre deve ser gerado pelo banco.
        // Isso também evita erro caso o Swagger envie um ID de exemplo.
        usuario.setId(null);

        // Se veio uma senha, só o hash dela é guardado; a senha em texto puro é descartada.
        if (usuario.getSenha() != null) {
            usuario.setSenhaHash(encoder.encode(usuario.getSenha()));
            usuario.setSenha(null);
        }

        return repository.save(usuario);
    }

    // Atualiza somente se o usuário informado existir. A senha não é alterada aqui (use PUT /usuarios/{id}/senha).
    public Optional<Usuario> atualizar(Long id, Usuario dados) {
        return repository.findById(id)
            .map(usuario -> {
                usuario.setNome(dados.getNome());
                usuario.setEmail(dados.getEmail());
                usuario.setCpf(dados.getCpf());
                usuario.setMatricula(dados.getMatricula());
                usuario.setTipo(dados.getTipo());
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

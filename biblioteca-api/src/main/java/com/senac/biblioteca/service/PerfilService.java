package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.PerfilRequest;
import com.senac.biblioteca.entity.Perfil;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.exception.RecursoRelacionadoNaoEncontradoException;
import com.senac.biblioteca.repository.PerfilRepository;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Camada de serviço de Perfil.
 * Além do CRUD, valida se o usuário relacionado realmente existe.
 */
@Service
public class PerfilService {

    private final PerfilRepository repository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(PerfilRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lista perfis utilizando a paginação informada pelo Controller.
    public Page<Perfil> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca um perfil pelo identificador.
    public Optional<Perfil> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Antes de salvar, confirma se o usuário informado existe.
    public Perfil criar(PerfilRequest dados) {
        Perfil perfil = new Perfil();
        perfil.setTelefone(dados.getTelefone());
        perfil.setEndereco(dados.getEndereco());
        perfil.setCidade(dados.getCidade());
        perfil.setEstado(dados.getEstado());
        perfil.setCep(dados.getCep());
        perfil.setDataNascimento(dados.getDataNascimento());
        perfil.setUsuario(buscarUsuario(dados.getUsuarioId()));

        return repository.save(perfil);
    }

    // Atualiza os dados do perfil quando o id é válido.
    public Optional<Perfil> atualizar(Long id, PerfilRequest dados) {
        Optional<Perfil> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = buscarUsuario(dados.getUsuarioId());

        Perfil perfil = existente.get();
        perfil.setTelefone(dados.getTelefone());
        perfil.setEndereco(dados.getEndereco());
        perfil.setCidade(dados.getCidade());
        perfil.setEstado(dados.getEstado());
        perfil.setCep(dados.getCep());
        perfil.setDataNascimento(dados.getDataNascimento());
        perfil.setUsuario(usuario);

        return Optional.of(repository.save(perfil));
    }

    // Garante que o usuário informado existe.
    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Usuário", usuarioId));
    }

    // Exclui o perfil se ele existir.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por telefone com paginação.
    public Page<Perfil> buscarPorTelefone(String telefone, Pageable pageable) {
        return repository.findByTelefoneContainingIgnoreCase(telefone, pageable);
    }
}

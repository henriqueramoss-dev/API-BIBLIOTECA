package com.senac.biblioteca.service;

import com.senac.biblioteca.entity.Categoria;
import com.senac.biblioteca.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Camada de serviço de Categoria.
 * Separa as regras de negócio da camada de acesso ao banco.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    // Lista categorias com paginação.
    public Page<Categoria> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca uma categoria pelo id.
    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Salva uma nova categoria.
    public Categoria criar(Categoria categoria) {
        categoria.setId(null);
        return repository.save(categoria);
    }

    // Atualiza apenas quando a categoria já existe.
    public Optional<Categoria> atualizar(Long id, Categoria dados) {
        return repository.findById(id)
            .map(categoria -> {
                categoria.setNome(dados.getNome());
                categoria.setCodigo(dados.getCodigo());
                categoria.setDescricao(dados.getDescricao());
                categoria.setLocalizacao(dados.getLocalizacao());
                categoria.setPublicoAlvo(dados.getPublicoAlvo());
                return repository.save(categoria);
            });
    }

    // Remove a categoria se o id existir.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por nome, mantendo a paginação.
    public Page<Categoria> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}

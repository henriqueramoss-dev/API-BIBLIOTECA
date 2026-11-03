package com.senac.biblioteca.service;

import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.repository.AutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Camada de serviço de Autor.
 * Mantém as regras de negócio separadas do Controller.
 */
@Service
public class AutorService {

    private final AutorRepository repository;

    public AutorService(AutorRepository repository) {
        this.repository = repository;
    }

    // Lista autores com paginação.
    public Page<Autor> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca um autor pelo identificador.
    public Optional<Autor> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Persiste um novo autor.
    public Autor criar(Autor autor) {
        autor.setId(null);
        return repository.save(autor);
    }

    // Atualiza o autor somente quando o id existe.
    public Optional<Autor> atualizar(Long id, Autor dados) {
        return repository.findById(id)
            .map(autor -> {
                autor.setNome(dados.getNome());
                autor.setNacionalidade(dados.getNacionalidade());
                autor.setDataNascimento(dados.getDataNascimento());
                autor.setGeneroLiterario(dados.getGeneroLiterario());
                autor.setBiografia(dados.getBiografia());
                return repository.save(autor);
            });
    }

    // Exclui pelo id e informa se a operação foi possível.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por nome com paginação.
    public Page<Autor> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}

package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.LivroRequest;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.entity.Categoria;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.exception.RecursoRelacionadoNaoEncontradoException;
import com.senac.biblioteca.repository.AutorRepository;
import com.senac.biblioteca.repository.CategoriaRepository;
import com.senac.biblioteca.repository.LivroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Camada de serviço de Livro.
 * Valida os relacionamentos com Categoria e Autor antes de salvar o livro.
 */
@Service
public class LivroService {

    private final LivroRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;

    public LivroService(
        LivroRepository repository,
        CategoriaRepository categoriaRepository,
        AutorRepository autorRepository
    ) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
    }

    // Lista livros com paginação.
    public Page<Livro> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca um livro pelo identificador.
    public Optional<Livro> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Valida categoria e autores antes de persistir o novo livro.
    public Livro criar(LivroRequest dados) {
        Categoria categoria = buscarCategoria(dados.getCategoriaId());
        Set<Autor> autores = buscarAutores(dados.getAutoresIds());

        Livro livro = new Livro();
        livro.setTitulo(dados.getTitulo());
        livro.setIsbn(dados.getIsbn());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setEditora(dados.getEditora());
        livro.setNumeroPaginas(dados.getNumeroPaginas());
        livro.setCategoria(categoria);
        livro.setAutores(autores);

        return repository.save(livro);
    }

    // Atualiza os dados principais se o livro existir.
    public Optional<Livro> atualizar(Long id, LivroRequest dados) {
        Optional<Livro> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        Categoria categoria = buscarCategoria(dados.getCategoriaId());
        Set<Autor> autores = buscarAutores(dados.getAutoresIds());

        Livro livro = existente.get();
        livro.setTitulo(dados.getTitulo());
        livro.setIsbn(dados.getIsbn());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setEditora(dados.getEditora());
        livro.setNumeroPaginas(dados.getNumeroPaginas());
        livro.setCategoria(categoria);
        livro.setAutores(autores);
        return Optional.of(repository.save(livro));
    }

    // Exclui o livro pelo id quando encontrado.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por parte do título, com paginação.
    public Page<Livro> buscarPorTitulo(String titulo, Pageable pageable) {
        return repository.findByTituloContainingIgnoreCase(titulo, pageable);
    }

    // Livros de uma categoria; 404 se a categoria não existir.
    public Page<Livro> listarPorCategoria(Long categoriaId, Pageable pageable) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw RecursoNaoEncontradoException.categoria(categoriaId);
        }

        return repository.findByCategoriaId(categoriaId, pageable);
    }

    // Livros de um autor; 404 se o autor não existir.
    public Page<Livro> listarPorAutor(Long autorId, Pageable pageable) {
        if (!autorRepository.existsById(autorId)) {
            throw RecursoNaoEncontradoException.autor(autorId);
        }

        return repository.findByAutoresId(autorId, pageable);
    }

    // Garante que a categoria informada existe.
    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Categoria", categoriaId));
    }

    // Garante que todos os autores informados existem.
    private Set<Autor> buscarAutores(Set<Long> autoresIds) {
        Set<Autor> autores = new HashSet<>();
        if (autoresIds == null) {
            return autores;
        }

        for (Long autorId : autoresIds) {
            if (autorId == null) {
                throw new RecursoRelacionadoNaoEncontradoException("Autor", null);
            }
            autores.add(autorRepository.findById(autorId)
                .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Autor", autorId)));
        }
        return autores;
    }
}

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

import com.senac.biblioteca.enums.StatusLivro;
import com.senac.biblioteca.repository.EmprestimoRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
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
    private final EmprestimoRepository emprestimoRepository;

    public LivroService(
        LivroRepository repository,
        CategoriaRepository categoriaRepository,
        AutorRepository autorRepository,
        EmprestimoRepository emprestimoRepository
    ) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    /**
     * Preenche o campo "situacao" dos livros (DISPONIVEL, EMPRESTADO ou INDISPONIVEL), com duas
     * consultas para a página inteira. É assim que o catálogo sabe o que pode ser reservado,
     * sem uma rota própria para isso.
     */
    private <T extends Collection<Livro>> T comSituacao(T livros) {
        if (livros.isEmpty()) {
            return livros;
        }
        List<Long> ids = livros.stream().map(Livro::getId).toList();
        Set<Long> emprestados = new HashSet<>(emprestimoRepository.idsDeLivrosEmprestados(ids));
        Set<Long> reservados = new HashSet<>(emprestimoRepository.idsDeLivrosReservados(ids, LocalDate.now()));
        livros.forEach(livro -> livro.setSituacao(emprestados.contains(livro.getId()) ? StatusLivro.EMPRESTADO
            : reservados.contains(livro.getId()) ? StatusLivro.INDISPONIVEL : StatusLivro.DISPONIVEL));
        return livros;
    }

    private Livro comSituacao(Livro livro) {
        comSituacao(List.of(livro));
        return livro;
    }

    private Page<Livro> comSituacao(Page<Livro> pagina) {
        comSituacao(pagina.getContent());
        return pagina;
    }

    // Lista livros com paginação.
    public Page<Livro> listar(Pageable pageable) {
        return comSituacao(repository.findAll(pageable));
    }

    // Busca um livro pelo identificador.
    public Optional<Livro> buscarPorId(Long id) {
        return repository.findById(id).map(this::comSituacao);
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

        return comSituacao(repository.save(livro));
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
        return Optional.of(comSituacao(repository.save(livro)));
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
        return comSituacao(repository.findByTituloContainingIgnoreCase(titulo, pageable));
    }

    // Livros de uma categoria; 404 se a categoria não existir.
    public Page<Livro> listarPorCategoria(Long categoriaId, Pageable pageable) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw RecursoNaoEncontradoException.categoria(categoriaId);
        }

        return comSituacao(repository.findByCategoriaId(categoriaId, pageable));
    }

    // Livros de um autor; 404 se o autor não existir.
    public Page<Livro> listarPorAutor(Long autorId, Pageable pageable) {
        if (!autorRepository.existsById(autorId)) {
            throw RecursoNaoEncontradoException.autor(autorId);
        }

        return comSituacao(repository.findByAutoresId(autorId, pageable));
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

package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.EmprestimoRequest;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.StatusEmprestimo;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.exception.RecursoRelacionadoNaoEncontradoException;
import com.senac.biblioteca.exception.ValidacaoNegocioException;
import com.senac.biblioteca.repository.EmprestimoRepository;
import com.senac.biblioteca.repository.LivroRepository;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Camada de serviço de Emprestimo.
 * Valida a existência do usuário e do livro antes de criar um empréstimo.
 */
@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(
        EmprestimoRepository repository,
        UsuarioRepository usuarioRepository,
        LivroRepository livroRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
    }

    // Lista empréstimos com paginação.
    public Page<Emprestimo> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca um empréstimo pelo identificador.
    public Optional<Emprestimo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Só cria o empréstimo quando as datas fazem sentido e usuário e livro informados existem.
    public Emprestimo criar(EmprestimoRequest dados) {
        conferirDatasEStatus(dados);
        Usuario usuario = buscarUsuario(dados.getUsuarioId());
        Livro livro = buscarLivro(dados.getLivroId());

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataEmprestimo(dados.getDataEmprestimo());
        emprestimo.setDataPrevistaDevolucao(dados.getDataPrevistaDevolucao());
        emprestimo.setDataDevolucao(dados.getDataDevolucao());
        emprestimo.setStatus(dados.getStatus());
        emprestimo.setObservacao(dados.getObservacao());
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);

        return repository.save(emprestimo);
    }

    // Atualiza datas e status quando o empréstimo existe.
    public Optional<Emprestimo> atualizar(Long id, EmprestimoRequest dados) {
        Optional<Emprestimo> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        conferirDatasEStatus(dados);
        Usuario usuario = buscarUsuario(dados.getUsuarioId());
        Livro livro = buscarLivro(dados.getLivroId());

        Emprestimo emprestimo = existente.get();
        emprestimo.setDataEmprestimo(dados.getDataEmprestimo());
        emprestimo.setDataPrevistaDevolucao(dados.getDataPrevistaDevolucao());
        emprestimo.setDataDevolucao(dados.getDataDevolucao());
        emprestimo.setStatus(dados.getStatus());
        emprestimo.setObservacao(dados.getObservacao());
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);

        // Salva a atualização e busca novamente o empréstimo com o
        // EntityGraph do Repository. Assim, Livro, Categoria e Autores
        // já chegam carregados para a resposta JSON/HATEOAS.
        repository.save(emprestimo);
        return Optional.of(repository.findById(id).orElse(emprestimo));
    }

    /**
     * Regras que o Bean Validation não cobre, por envolverem mais de um campo:
     *  - as datas de devolução não podem ser anteriores à data do empréstimo;
     *  - empréstimo DEVOLVIDO precisa da data de devolução;
     *  - só empréstimo DEVOLVIDO pode ter data de devolução.
     */
    private void conferirDatasEStatus(EmprestimoRequest dados) {
        LocalDate emprestimo = dados.getDataEmprestimo();
        LocalDate prevista = dados.getDataPrevistaDevolucao();
        LocalDate devolucao = dados.getDataDevolucao();
        StatusEmprestimo status = dados.getStatus();

        if (emprestimo != null && prevista != null && prevista.isBefore(emprestimo)) {
            throw new ValidacaoNegocioException("dataPrevistaDevolucao",
                "A devolução prevista deve ser igual ou posterior à data do empréstimo.");
        }

        if (emprestimo != null && devolucao != null && devolucao.isBefore(emprestimo)) {
            throw new ValidacaoNegocioException("dataDevolucao",
                "A data de devolução deve ser igual ou posterior à data do empréstimo.");
        }

        if (status == StatusEmprestimo.DEVOLVIDO && devolucao == null) {
            throw new ValidacaoNegocioException("dataDevolucao",
                "Informe a data de devolução quando o status for DEVOLVIDO.");
        }

        if (status != StatusEmprestimo.DEVOLVIDO && devolucao != null) {
            throw new ValidacaoNegocioException("status",
                "Só um empréstimo com status DEVOLVIDO pode ter data de devolução.");
        }
    }

    // Garante que o usuário informado existe.
    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Usuário", usuarioId));
    }

    // Garante que o livro informado existe.
    private Livro buscarLivro(Long livroId) {
        return livroRepository.findById(livroId)
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Livro", livroId));
    }

    // Exclui pelo id quando encontrado.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada pelo enum StatusEmprestimo, também paginada.
    public Page<Emprestimo> buscarPorStatus(StatusEmprestimo status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    // Empréstimos de um usuário; 404 se o usuário não existir.
    public Page<Emprestimo> listarPorUsuario(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw RecursoNaoEncontradoException.usuario(usuarioId);
        }

        return repository.findByUsuarioId(usuarioId, pageable);
    }

    // Histórico de empréstimos de um livro; 404 se o livro não existir.
    public Page<Emprestimo> listarPorLivro(Long livroId, Pageable pageable) {
        if (!livroRepository.existsById(livroId)) {
            throw RecursoNaoEncontradoException.livro(livroId);
        }

        return repository.findByLivroId(livroId, pageable);
    }
}

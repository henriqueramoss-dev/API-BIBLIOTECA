package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.ReservaRequest;
import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.enums.StatusEmprestimo;
import com.senac.biblioteca.exception.AcessoNegadoException;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.exception.RecursoRelacionadoNaoEncontradoException;
import com.senac.biblioteca.exception.ReservaNaoPermitidaException;
import com.senac.biblioteca.repository.EmprestimoRepository;
import com.senac.biblioteca.repository.LivroRepository;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Reservas feitas pelo cliente: o livro fica separado na biblioteca até o dia limite de retirada.
 * Uma reserva é um Emprestimo com status RESERVADO; quando o cliente retira o livro, a equipe
 * da biblioteca muda o status para ATIVO.
 *
 * Quem reserva é sempre o dono da chave de API usada: o cliente não escolhe o usuário.
 */
@Service
public class ReservaService {

    /** Quantos dias o empréstimo dura depois da retirada (usado só para a data prevista inicial). */
    private static final int DIAS_DE_EMPRESTIMO = 14;

    private final EmprestimoRepository repository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final int diasParaRetirar;
    private final int maximoPorUsuario;

    public ReservaService(
        EmprestimoRepository repository,
        LivroRepository livroRepository,
        UsuarioRepository usuarioRepository,
        @Value("${biblioteca.reservas.dias-para-retirar:3}") int diasParaRetirar,
        @Value("${biblioteca.reservas.maximo-por-usuario:3}") int maximoPorUsuario
    ) {
        this.repository = repository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.diasParaRetirar = diasParaRetirar;
        this.maximoPorUsuario = maximoPorUsuario;
    }

    /**
     * Reserva um livro para o dono da chave. Não é @Transactional de propósito: o método é
     * synchronized e cada save() confirma na hora, então duas pessoas reservando o mesmo livro
     * ao mesmo tempo não passam juntas pela conferência (esta API roda em uma única instância).
     */
    public synchronized Emprestimo reservar(ChaveApi chave, ReservaRequest dados) {
        Usuario usuario = donoDaChave(chave);
        Livro livro = livroRepository.findById(dados.getLivroId())
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Livro", dados.getLivroId()));
        LocalDate hoje = LocalDate.now();

        long emAberto = repository.countByUsuarioIdAndStatusAndRetirarAteGreaterThanEqual(
            usuario.getId(), StatusEmprestimo.RESERVADO, hoje);
        if (emAberto >= maximoPorUsuario) {
            throw new ReservaNaoPermitidaException("Você já tem " + emAberto + " reservas aguardando retirada (o máximo é "
                + maximoPorUsuario + "). Retire ou cancele uma delas para reservar outro livro.");
        }
        if (repository.livroIndisponivel(livro.getId(), hoje)) {
            throw new ReservaNaoPermitidaException("O livro \"" + livro.getTitulo() + "\" não está disponível no momento: "
                + "está emprestado ou reservado por outra pessoa.");
        }

        Emprestimo reserva = new Emprestimo();
        reserva.setStatus(StatusEmprestimo.RESERVADO);
        reserva.setUsuario(usuario);
        reserva.setLivro(livro);
        reserva.setDataEmprestimo(hoje);
        reserva.setRetirarAte(hoje.plusDays(diasParaRetirar));
        reserva.setDataPrevistaDevolucao(reserva.getRetirarAte().plusDays(DIAS_DE_EMPRESTIMO));
        reserva.setObservacao(dados.getObservacao() == null || dados.getObservacao().isBlank() ? null : dados.getObservacao().trim());
        Emprestimo salva = repository.save(reserva);
        return repository.findById(salva.getId()).orElse(salva);
    }

    /** Reservas e empréstimos do dono da chave (do mais novo para o mais antigo, por padrão do controller). */
    public Page<Emprestimo> minhas(ChaveApi chave, Pageable pageable) {
        return repository.findByUsuarioId(donoDaChave(chave).getId(), pageable);
    }

    /**
     * Cancela uma reserva que ainda não foi retirada. Cada cliente cancela só as próprias;
     * a de outra pessoa responde 404 (não revela que ela existe). A chave ADMIN cancela qualquer uma.
     */
    public void cancelar(ChaveApi chave, Long id) {
        Emprestimo reserva = repository.findById(id).orElseThrow(() -> RecursoNaoEncontradoException.emprestimo(id));
        boolean admin = chave.getNivel() == NivelAcesso.ADMIN;
        boolean dono = chave.getUsuarioId() != null && chave.getUsuarioId().equals(reserva.getUsuario().getId());
        if (!admin && !dono) {
            throw RecursoNaoEncontradoException.emprestimo(id);
        }
        if (reserva.getStatus() != StatusEmprestimo.RESERVADO) {
            throw new ReservaNaoPermitidaException("Só é possível cancelar reservas que ainda não foram retiradas.");
        }
        repository.delete(reserva);
    }

    private Usuario donoDaChave(ChaveApi chave) {
        if (chave.getUsuarioId() == null) {
            throw new AcessoNegadoException("Esta chave de API não pertence a um usuário, por isso não tem reservas. "
                + "Entre com o e-mail e a senha de um usuário.");
        }
        return usuarioRepository.findById(chave.getUsuarioId())
            .orElseThrow(() -> RecursoNaoEncontradoException.usuario(chave.getUsuarioId()));
    }
}

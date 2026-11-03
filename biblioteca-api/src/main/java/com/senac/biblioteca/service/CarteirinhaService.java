package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.CarteirinhaRequest;
import com.senac.biblioteca.entity.Carteirinha;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.exception.RecursoRelacionadoNaoEncontradoException;
import com.senac.biblioteca.exception.ValidacaoNegocioException;
import com.senac.biblioteca.repository.CarteirinhaRepository;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Camada de serviço de Carteirinha.
 * Além do CRUD, valida se o usuário relacionado existe e se as datas fazem sentido.
 */
@Service
public class CarteirinhaService {

    private final CarteirinhaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public CarteirinhaService(CarteirinhaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lista carteirinhas utilizando a paginação informada pelo Controller.
    public Page<Carteirinha> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca uma carteirinha pelo identificador.
    public Optional<Carteirinha> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Antes de salvar, confirma se o usuário informado existe e se as datas são coerentes.
    public Carteirinha criar(CarteirinhaRequest dados) {
        Carteirinha carteirinha = new Carteirinha();
        preencher(carteirinha, dados);
        return repository.save(carteirinha);
    }

    // Atualiza os dados da carteirinha quando o id é válido.
    public Optional<Carteirinha> atualizar(Long id, CarteirinhaRequest dados) {
        Optional<Carteirinha> existente = repository.findById(id);

        if (existente.isEmpty()) {
            return Optional.empty();
        }

        Carteirinha carteirinha = existente.get();
        preencher(carteirinha, dados);
        return Optional.of(repository.save(carteirinha));
    }

    // Copia os dados recebidos para a entidade, depois de conferir as regras de negócio.
    private void preencher(Carteirinha carteirinha, CarteirinhaRequest dados) {
        if (dados.getDataValidade().isBefore(dados.getDataEmissao())) {
            throw new ValidacaoNegocioException("dataValidade", "A validade não pode ser anterior à data de emissão.");
        }

        Usuario usuario = usuarioRepository.findById(dados.getUsuarioId())
            .orElseThrow(() -> new RecursoRelacionadoNaoEncontradoException("Usuário", dados.getUsuarioId()));

        carteirinha.setNumero(dados.getNumero().toUpperCase());
        carteirinha.setDataEmissao(dados.getDataEmissao());
        carteirinha.setDataValidade(dados.getDataValidade());
        carteirinha.setStatus(dados.getStatus());
        carteirinha.setLimiteEmprestimos(dados.getLimiteEmprestimos());
        carteirinha.setUsuario(usuario);
    }

    // Exclui a carteirinha se ela existir.
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    // Consulta personalizada por número com paginação.
    public Page<Carteirinha> buscarPorNumero(String numero, Pageable pageable) {
        return repository.findByNumeroContainingIgnoreCase(numero, pageable);
    }
}

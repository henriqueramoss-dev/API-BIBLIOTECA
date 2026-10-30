package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.LoginRequest;
import com.senac.biblioteca.dto.TrocaSenhaRequest;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.exception.CredenciaisInvalidasException;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.exception.ValidacaoNegocioException;
import com.senac.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Regras de login e de troca de senha.
 *
 * Importante: a API ainda não emite token nem sessão. O login só confere e-mail e senha e devolve o
 * usuário; as demais rotas continuam abertas (veja docs/seguranca-e-limitacoes.md).
 */
@Service
public class AutenticacaoService {

    // Hash de uma senha qualquer, usado para gastar o mesmo tempo quando o e-mail não existe.
    private final String hashFalso;

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public AutenticacaoService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
        this.hashFalso = encoder.encode("senha-que-ninguem-usa");
    }

    // Confere e-mail e senha. Qualquer falha lança a mesma exceção (HTTP 401).
    public Usuario autenticar(LoginRequest dados) {
        Usuario usuario = repository.findByEmailIgnoreCase(dados.getEmail().trim()).orElse(null);

        // Sempre calcula um hash, mesmo sem usuário, para o tempo de resposta não revelar se o e-mail existe.
        String hash = usuario != null && usuario.getSenhaHash() != null ? usuario.getSenhaHash() : hashFalso;
        boolean confere = encoder.matches(dados.getSenha(), hash);

        if (usuario == null || usuario.getSenhaHash() == null || !confere) {
            throw new CredenciaisInvalidasException();
        }

        return usuario;
    }

    // Troca (ou define pela primeira vez) a senha de um usuário.
    public void trocarSenha(Long id, TrocaSenhaRequest dados) {
        Usuario usuario = repository.findById(id)
            .orElseThrow(() -> RecursoNaoEncontradoException.usuario(id));

        if (usuario.getSenhaHash() != null) {
            if (dados.getSenhaAtual() == null || dados.getSenhaAtual().isBlank()) {
                throw new ValidacaoNegocioException("senhaAtual", "Informe a senha atual.");
            }
            if (!encoder.matches(dados.getSenhaAtual(), usuario.getSenhaHash())) {
                throw new ValidacaoNegocioException("senhaAtual", "A senha atual está incorreta.");
            }
            if (dados.getSenhaAtual().equals(dados.getNovaSenha())) {
                throw new ValidacaoNegocioException("novaSenha", "A nova senha deve ser diferente da atual.");
            }
        }

        usuario.setSenhaHash(encoder.encode(dados.getNovaSenha()));
        repository.save(usuario);
    }
}

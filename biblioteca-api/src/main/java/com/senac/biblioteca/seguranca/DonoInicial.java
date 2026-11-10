package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.enums.TipoUsuario;
import com.senac.biblioteca.repository.UsuarioRepository;
import com.senac.biblioteca.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Cria a conta do dono da biblioteca (tipo FUNCIONARIO) ao iniciar a API, quando o e-mail e a
 * senha dele são informados nas variáveis de ambiente BIBLIOTECA_DONO_EMAIL e BIBLIOTECA_DONO_SENHA.
 *
 * Como o banco é em memória, os dados somem quando a API reinicia (na Render gratuita isso acontece
 * toda vez que ela "dorme"). Com estas variáveis, o dono volta sozinho e pode entrar no site na hora,
 * sem precisar da chave de administrador. A senha só é guardada como hash (BCrypt), como as demais.
 *
 * Os demais dados são provisórios, e o dono ajusta pelo painel (Usuários, Editar).
 */
@Component
public class DonoInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DonoInicial.class);

    /** Tamanho mínimo da senha, o mesmo da API (8 a 72 caracteres). */
    static final int TAMANHO_MINIMO_SENHA = 8;

    // CPF válido (confere os dígitos verificadores), só para preencher o campo obrigatório.
    private static final String CPF_PROVISORIO = "00000000191";

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final String email;
    private final String senha;
    private final String nome;

    public DonoInicial(
        UsuarioService usuarioService,
        UsuarioRepository usuarioRepository,
        @Value("${biblioteca.dono.email:}") String email,
        @Value("${biblioteca.dono.senha:}") String senha,
        @Value("${biblioteca.dono.nome:Dono da Biblioteca}") String nome
    ) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.email = email == null ? "" : email.trim();
        this.senha = senha == null ? "" : senha;
        this.nome = nome == null || nome.isBlank() ? "Dono da Biblioteca" : nome.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() && senha.isEmpty()) {
            return; // não configurado: só a chave de administrador cadastra o primeiro funcionário
        }
        if (email.isEmpty() || senha.length() < TAMANHO_MINIMO_SENHA || senha.length() > 72) {
            log.warn("Dono inicial ignorado: BIBLIOTECA_DONO_EMAIL e BIBLIOTECA_DONO_SENHA precisam estar definidos "
                + "e a senha deve ter de {} a 72 caracteres.", TAMANHO_MINIMO_SENHA);
            return;
        }
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            log.info("Dono inicial já existe ({}).", email);
            return;
        }

        Usuario dono = new Usuario();
        dono.setNome(nome);
        dono.setEmail(email);
        dono.setCpf(CPF_PROVISORIO);
        dono.setTipo(TipoUsuario.FUNCIONARIO);
        dono.setTelefone("00000000000");
        dono.setEndereco("A definir (edite no painel)");
        dono.setCidade("A definir");
        dono.setEstado("SP");
        dono.setCep("00000000");
        dono.setDataNascimento(LocalDate.of(1990, 1, 1));
        dono.setSenha(senha);
        try {
            usuarioService.criar(dono, NivelAcesso.ADMIN);
            log.info("Conta do dono criada: entre no site com o e-mail {} e a senha definida em BIBLIOTECA_DONO_SENHA.", email);
        } catch (RuntimeException e) {
            // A API continua subindo mesmo se a conta não puder ser criada (e-mail inválido, por exemplo).
            log.warn("Não foi possível criar a conta do dono: {}", e.getMessage());
        }
    }
}

package com.senac.biblioteca.service;

import com.senac.biblioteca.dto.ChaveApiCriada;
import com.senac.biblioteca.dto.ChaveApiRequest;
import com.senac.biblioteca.dto.LoginRequest;
import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.exception.AcessoNegadoException;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.repository.ChaveApiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Geração, consulta e revogação das chaves de API.
 *
 * Como uma senha, a chave nunca é gravada: o banco guarda só o hash SHA-256 dela. Diferente
 * de uma senha, a chave é longa e aleatória (32 bytes), então o SHA-256 basta e a conferência
 * a cada requisição é rápida.
 */
@Service
public class ChaveApiService {

    /** Todas as chaves começam assim, para serem reconhecidas facilmente (por exemplo, num vazamento). */
    static final String INICIO_DA_CHAVE = "bib_";

    private static final int TAMANHO_DO_PREFIXO = 10;

    private final SecureRandom aleatorio = new SecureRandom();
    private final ChaveApiRepository repository;
    private final AutenticacaoService autenticacaoService;

    public ChaveApiService(ChaveApiRepository repository, AutenticacaoService autenticacaoService) {
        this.repository = repository;
        this.autenticacaoService = autenticacaoService;
    }

    /**
     * Gera uma chave para o usuário dono do e-mail e da senha informados.
     * O nível pedido não pode passar do permitido para o tipo do usuário.
     */
    public ChaveApiCriada gerar(ChaveApiRequest dados) {
        LoginRequest login = new LoginRequest();
        login.setEmail(dados.getEmail());
        login.setSenha(dados.getSenha());
        Usuario usuario = autenticacaoService.autenticar(login);

        NivelAcesso nivel = dados.getNivel() == null ? NivelAcesso.LEITURA : dados.getNivel();
        NivelAcesso maximo = NivelAcesso.maximoPara(usuario.getTipo());
        if (!maximo.permite(nivel)) {
            throw new AcessoNegadoException("Usuários do tipo " + usuario.getTipo()
                + " podem gerar chaves de no máximo nível " + maximo + ".");
        }

        String chave = novaChave();
        return new ChaveApiCriada(salvar(dados.getNome().trim(), nivel, usuario, chave), chave, usuario);
    }

    /**
     * Registra uma chave já conhecida (usada para a chave de administrador inicial).
     * Não faz nada se a chave já estiver cadastrada.
     */
    public void registrarSeNaoExistir(String nome, NivelAcesso nivel, String chave) {
        if (repository.findByHash(hash(chave)).isEmpty()) {
            salvar(nome, nivel, null, chave);
        }
    }

    /** Gera uma chave nova, aleatória, no formato bib_XXXX (43 caracteres depois do bib_). */
    public String novaChave() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return INICIO_DA_CHAVE + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Procura uma chave ativa a partir do valor recebido no cabeçalho X-API-Key. */
    public Optional<ChaveApi> identificar(String chave) {
        if (chave == null || chave.isBlank()) {
            return Optional.empty();
        }
        return repository.findByHash(hash(chave.trim())).filter(ChaveApi::isAtiva);
    }

    /** Anota o último uso da chave, no máximo uma vez por minuto (para não gravar a cada requisição). */
    public void registrarUso(ChaveApi chave) {
        LocalDateTime agora = LocalDateTime.now();
        if (chave.getUltimoUsoEm() == null || chave.getUltimoUsoEm().isBefore(agora.minusMinutes(1))) {
            chave.setUltimoUsoEm(agora);
            repository.save(chave);
        }
    }

    /** Lista as chaves visíveis para quem está chamando: ADMIN vê todas; os demais, só as próprias. */
    public Page<ChaveApi> listar(ChaveApi atual, Pageable pageable) {
        if (atual.getNivel() == NivelAcesso.ADMIN) {
            return repository.findAll(pageable);
        }
        return repository.findByUsuarioId(atual.getUsuarioId(), pageable);
    }

    /** Busca uma chave. A chave de outra pessoa responde 404, como se não existisse. */
    public ChaveApi buscar(Long id, ChaveApi atual) {
        return repository.findById(id)
            .filter(chave -> podeVer(atual, chave))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Chave de API com ID " + id + " não encontrada."));
    }

    /** Revoga a chave: ela continua listada, mas deixa de funcionar na hora. */
    public void revogar(Long id, ChaveApi atual) {
        ChaveApi chave = buscar(id, atual);
        if (chave.isAtiva()) {
            chave.setAtiva(false);
            chave.setRevogadaEm(LocalDateTime.now());
            repository.save(chave);
        }
    }

    private boolean podeVer(ChaveApi atual, ChaveApi chave) {
        return atual.getNivel() == NivelAcesso.ADMIN
            || (atual.getUsuarioId() != null && atual.getUsuarioId().equals(chave.getUsuarioId()));
    }

    private ChaveApi salvar(String nome, NivelAcesso nivel, Usuario usuario, String chave) {
        ChaveApi registro = new ChaveApi();
        registro.setNome(nome);
        registro.setNivel(nivel);
        registro.setUsuario(usuario);
        registro.setPrefixo(chave.substring(0, Math.min(TAMANHO_DO_PREFIXO, chave.length())));
        registro.setHash(hash(chave));
        registro.setCriadaEm(LocalDateTime.now());
        return repository.save(registro);
    }

    static String hash(String chave) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(chave.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível nesta JVM", e);
        }
    }
}

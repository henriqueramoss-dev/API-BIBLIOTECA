package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.service.ChaveApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Descobre quem está fazendo a requisição: a chave de API (cabeçalho X-API-Key), quando ela
 * é válida, ou o endereço IP.
 *
 * O resultado fica guardado na própria requisição, então o banco é consultado uma vez só,
 * mesmo com o limite de requisições e a autenticação usando a mesma informação.
 */
@Component
public class ClienteDaRequisicao {

    public static final String CABECALHO_CHAVE = "X-API-Key";

    private static final String ATRIBUTO_CHAVE = ClienteDaRequisicao.class.getName() + ".chave";
    private static final String ATRIBUTO_VERIFICADA = ClienteDaRequisicao.class.getName() + ".verificada";

    private final ChaveApiService service;

    public ClienteDaRequisicao(ChaveApiService service) {
        this.service = service;
    }

    /** Diz se a requisição trouxe algum valor no cabeçalho X-API-Key. */
    public boolean enviouChave(HttpServletRequest request) {
        String valor = request.getHeader(CABECALHO_CHAVE);
        return valor != null && !valor.isBlank();
    }

    /** Chave de API válida e ativa enviada na requisição, se houver. */
    public Optional<ChaveApi> chave(HttpServletRequest request) {
        if (request.getAttribute(ATRIBUTO_VERIFICADA) == null) {
            request.setAttribute(ATRIBUTO_VERIFICADA, Boolean.TRUE);
            service.identificar(request.getHeader(CABECALHO_CHAVE))
                .ifPresent(chave -> request.setAttribute(ATRIBUTO_CHAVE, chave));
        }
        return chaveJaIdentificada(request);
    }

    /**
     * Identificador usado no limite de requisições: "chave:ID" para quem usa uma chave válida,
     * "ip:ENDEREÇO" para os demais (inclusive quem envia uma chave falsa).
     */
    public String identificador(HttpServletRequest request) {
        return chaveDeConta(request)
            .map(chave -> "chave:" + chave.getId())
            .orElse("ip:" + request.getRemoteAddr());
    }

    /**
     * Chave que conta como "de uma conta" para o limite de requisições: a de um usuário ou a de administrador.
     * A chave do catálogo é pública e usada por todos os visitantes do site e do app: contar tudo junto
     * bloquearia todo mundo por causa de um só. Ela é contada por IP, com o limite de quem não tem chave.
     */
    public Optional<ChaveApi> chaveDeConta(HttpServletRequest request) {
        return chave(request)
            .filter(chave -> chave.getUsuarioId() != null || chave.getNivel() == com.senac.biblioteca.enums.NivelAcesso.ADMIN);
    }

    /** Chave já identificada nesta requisição (sem consultar o banco). */
    public static Optional<ChaveApi> chaveJaIdentificada(HttpServletRequest request) {
        return Optional.ofNullable((ChaveApi) request.getAttribute(ATRIBUTO_CHAVE));
    }
}

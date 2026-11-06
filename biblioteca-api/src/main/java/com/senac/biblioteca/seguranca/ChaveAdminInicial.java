package com.senac.biblioteca.seguranca;

import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.service.ChaveApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria a primeira chave de API, de nível ADMIN, ao iniciar a API.
 *
 * Sem ela não haveria como cadastrar o primeiro funcionário, que depois gera as próprias chaves.
 *
 *  - Se a variável de ambiente BIBLIOTECA_CHAVE_ADMIN estiver definida, essa é a chave.
 *  - Se não estiver, a API gera uma chave aleatória e mostra no log (como o Spring Security
 *    faz com a senha padrão). Como o banco é em memória, uma nova chave é gerada a cada início.
 */
@Component
public class ChaveAdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChaveAdminInicial.class);

    /** Tamanho mínimo aceito para a chave definida na variável de ambiente. */
    static final int TAMANHO_MINIMO = 16;

    private final ChaveApiService service;
    private final String chaveConfigurada;

    public ChaveAdminInicial(
        ChaveApiService service,
        @Value("${biblioteca.chave-api.admin-inicial:}") String chaveConfigurada
    ) {
        this.service = service;
        this.chaveConfigurada = chaveConfigurada == null ? "" : chaveConfigurada.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (chaveConfigurada.length() >= TAMANHO_MINIMO) {
            service.registrarSeNaoExistir("Administrador inicial", NivelAcesso.ADMIN, chaveConfigurada);
            log.info("Chave de API de administrador carregada da configuração (BIBLIOTECA_CHAVE_ADMIN).");
            return;
        }

        if (!chaveConfigurada.isEmpty()) {
            log.warn("A chave em BIBLIOTECA_CHAVE_ADMIN tem menos de {} caracteres e foi ignorada.", TAMANHO_MINIMO);
        }

        String chave = service.novaChave();
        service.registrarSeNaoExistir("Administrador inicial", NivelAcesso.ADMIN, chave);
        log.warn("""

            Chave de API de administrador gerada para esta execução:

                {}

            Use-a no cabeçalho X-API-Key. Para usar sempre a mesma chave, defina a variável de
            ambiente BIBLIOTECA_CHAVE_ADMIN (mínimo de {} caracteres). Não use esta chave em produção.
            """, chave, TAMANHO_MINIMO);
    }
}

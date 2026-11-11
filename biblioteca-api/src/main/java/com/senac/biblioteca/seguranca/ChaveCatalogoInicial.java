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
 * Cria a chave de leitura do catálogo ao iniciar a API.
 *
 * O catálogo (livros, autores e categorias) exige pelo menos uma chave de LEITURA. Visitantes do site e do
 * app não têm conta, então os dois usam esta chave: ela é pública por natureza (fica no código do site),
 * não pertence a ninguém e só lê o catálogo. Não vê dados pessoais (isso exige ESCRITA), não reserva (a
 * reserva é do dono da chave) e não lista chaves. Quem tem conta usa a própria chave.
 *
 * O valor vem de biblioteca.chave-api.catalogo (variável BIBLIOTECA_CHAVE_API_CATALOGO). Vazio desliga.
 */
@Component
public class ChaveCatalogoInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChaveCatalogoInicial.class);

    private final ChaveApiService service;
    private final String chave;

    public ChaveCatalogoInicial(ChaveApiService service, @Value("${biblioteca.chave-api.catalogo:}") String chave) {
        this.service = service;
        this.chave = chave == null ? "" : chave.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (chave.isEmpty()) {
            return;
        }
        service.registrarSeNaoExistir("Catálogo (leitura pública)", NivelAcesso.LEITURA, chave);
        log.info("Chave de leitura do catálogo carregada (somente livros, autores e categorias).");
    }
}

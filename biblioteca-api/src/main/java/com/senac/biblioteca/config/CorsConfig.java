package com.senac.biblioteca.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Política de CORS (Cross-Origin Resource Sharing) da API.
 *
 * O CORS diz ao NAVEGADOR quais sites podem chamar a API. Ele não protege a API de outros
 * programas (curl, Postman, o app): para isso existe a chave de API.
 *
 * É um filtro com a maior prioridade, e não a configuração do Spring MVC, porque roda antes
 * de todos os outros filtros. Assim até as respostas de erro dadas por eles (401, 429...)
 * levam os cabeçalhos de CORS e o site consegue ler a mensagem de erro.
 */
@Configuration
public class CorsConfig {

    /** Métodos HTTP aceitos em requisições vindas de outra origem. */
    static final List<String> METODOS_PERMITIDOS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /** Cabeçalhos que o site pode enviar. */
    static final List<String> CABECALHOS_PERMITIDOS = List.of(
        "Content-Type",
        "Accept",
        "X-API-Key"
    );

    /** Cabeçalhos da resposta que o JavaScript do site pode ler (os demais ficam ocultos pelo navegador). */
    static final List<String> CABECALHOS_EXPOSTOS = List.of(
        "Location",
        "Allow",
        "WWW-Authenticate"
    );

    private final List<String> origensPermitidas;

    /**
     * As origens vêm de biblioteca.cors.origens-permitidas (application.properties).
     * Em produção, troque pelos endereços reais do site.
     */
    public CorsConfig(@Value("${biblioteca.cors.origens-permitidas}") List<String> origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origensPermitidas);
        config.setAllowedMethods(METODOS_PERMITIDOS);
        config.setAllowedHeaders(CABECALHOS_PERMITIDOS);
        config.setExposedHeaders(CABECALHOS_EXPOSTOS);
        // A API não usa cookies; a identificação é feita pela chave de API no cabeçalho.
        config.setAllowCredentials(false);
        // O navegador pode guardar a resposta da verificação prévia (preflight) por 1 hora.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(origem));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}

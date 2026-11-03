package com.senac.biblioteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Aplica o tema visual personalizado ao Swagger UI.
 *
 * A página do Swagger já carrega o arquivo /swagger-ui/index.css.
 * Aqui esse endereço passa a apontar para o nosso arquivo em
 * resources/static/swagger/index.css. A própria página (index.html) também
 * é substituída, só para trocar o título da aba e o ícone pelo da Biblioteca.
 */
@Configuration
public class SwaggerUiConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // O "*" no final faz o Spring extrair o nome do arquivo (index.css) da URL.
        registry.addResourceHandler("/swagger-ui/index.css*", "/swagger-ui/index.html*", "/swagger-ui/favicon.svg*")
            .addResourceLocations("classpath:/static/swagger/");
    }
}
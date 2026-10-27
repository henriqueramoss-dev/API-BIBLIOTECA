package com.senac.biblioteca.config;

import com.senac.biblioteca.dto.ErroResposta;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuração central da documentação Swagger/OpenAPI.
 *
 * Esta classe define as informações gerais exibidas no topo do Swagger UI,
 * como nome da API, descrição, versão, responsável, termos de serviço,
 * licença, servidor local e documentação externa.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API de Biblioteca - Gestão de Acervo e Empréstimos",
        version = "1.0.0",
        description = """
            API REST desenvolvida para o gerenciamento de uma biblioteca.

            A aplicação permite cadastrar, consultar, atualizar e excluir
            usuários, perfis, autores, categorias, livros e empréstimos.

            As rotas de listagem utilizam paginação, os dados de entrada são
            validados com Bean Validation e as respostas utilizam códigos HTTP
            adequados. A API também utiliza Spring Data JPA para persistência,
            banco H2 para ambiente de testes, Swagger/OpenAPI para documentação
            e HATEOAS para fornecer links de navegação entre os recursos.

            Esta API foi desenvolvida para fins acadêmicos e deve ser utilizada
            em ambiente local de desenvolvimento e testes, somente com dados fictícios.

            Veja a [Política de Privacidade](https://github.com/henriqueramoss-dev/API-DE-JAVA-/blob/develop/biblioteca-api/PRIVACY_POLICY.md)
            e a [documentação do projeto](https://github.com/henriqueramoss-dev/API-DE-JAVA-/blob/develop/docs/README.md).
            """,
        termsOfService = "https://github.com/henriqueramoss-dev/API-DE-JAVA-/blob/develop/biblioteca-api/TERMS_OF_SERVICE.md",
        contact = @Contact(
            name = "Henrique Ramos",
            url = "https://github.com/henriqueramoss-dev/API-DE-JAVA-"
        ),
        license = @License(
            name = "Uso acadêmico - Projeto Senac",
            url = "https://github.com/henriqueramoss-dev/API-DE-JAVA-/blob/develop/README.md"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Servidor local de desenvolvimento e testes"
        )
    },
    externalDocs = @ExternalDocumentation(
        description = "Código-fonte e documentação completa do projeto",
        url = "https://github.com/henriqueramoss-dev/API-DE-JAVA-/tree/develop/biblioteca-api"
    )
)
public class OpenApiConfig {

    // As informações gerais do Swagger são definidas pelas anotações acima.

    private static final String SUFIXO_PAGINA_INEXISTENTE = " ou página inexistente";

    /**
     * Completa a documentação gerada automaticamente:
     *  - o corpo das respostas de erro (4xx e 5xx) passa a usar o esquema ErroResposta, com exemplo;
     *  - as listagens paginadas explicam os limites de "page", "size" e "sort" e documentam o 404
     *    de página inexistente.
     *
     * Só altera o que existe: partes ausentes da especificação são ignoradas, nunca causam erro.
     */
    @Bean
    public OpenApiCustomizer respostasDeErro(PaginacaoConfig paginacao) {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            Components componentes = openApi.getComponents() != null ? openApi.getComponents() : new Components();
            openApi.setComponents(componentes);
            ModelConverters.getInstance().read(ErroResposta.class).forEach(componentes::addSchemas);
            Schema<?> esquema = new Schema<>().$ref("#/components/schemas/ErroResposta");

            openApi.getPaths().values().forEach(caminho -> caminho.readOperations().forEach(operacao -> {
                if (operacao.getResponses() == null) {
                    return;
                }

                if (ehListagemPaginada(operacao)) {
                    descreverPaginacao(operacao, paginacao.getTamanhoMaximo());
                }

                boolean recebeCorpo = operacao.getRequestBody() != null;
                operacao.getResponses().forEach((codigo, resposta) -> {
                    if (codigo.startsWith("4") || codigo.startsWith("5")) {
                        resposta.setContent(new Content().addMediaType("application/json",
                            new MediaType().schema(esquema).example(exemplo(codigo, resposta.getDescription(), recebeCorpo))));
                    }
                });
            }));
        };
    }

    private boolean ehListagemPaginada(Operation operacao) {
        return operacao.getParameters() != null
            && operacao.getParameters().stream().anyMatch(parametro -> "size".equals(parametro.getName()));
    }

    private void descreverPaginacao(Operation operacao, int tamanhoMaximo) {
        operacao.getParameters().forEach(parametro -> {
            switch (String.valueOf(parametro.getName())) {
                case "page" -> parametro.setDescription(
                    "Número da página, começando em 0. Se passar da última página, a API responde 404.");
                case "size" -> parametro.setDescription("Registros por página: de 1 a " + tamanhoMaximo
                    + " (padrão 10). Fora desse limite, a API responde 400.");
                case "sort" -> parametro.setDescription(
                    "Ordenação no formato campo,(asc|desc). Ex.: titulo,asc. Pode repetir para ordenar por mais de um campo.");
                default -> { }
            }
        });

        ApiResponse naoEncontrado = operacao.getResponses().get("404");
        if (naoEncontrado == null) {
            operacao.getResponses().addApiResponse("404", new ApiResponse().description("Página inexistente"));
        } else {
            String descricao = naoEncontrado.getDescription() == null ? "Não encontrado" : naoEncontrado.getDescription();
            if (!descricao.endsWith(SUFIXO_PAGINA_INEXISTENTE) && !descricao.equals("Página inexistente")) {
                naoEncontrado.setDescription(descricao + SUFIXO_PAGINA_INEXISTENTE);
            }
        }
    }

    /**
     * Exemplo do corpo de erro. O 400 de uma operação que recebe corpo (POST/PUT) mostra o objeto
     * "campos" dos erros de validação; nas demais, a mensagem é a própria descrição da resposta.
     */
    private Map<String, Object> exemplo(String codigo, String descricao, boolean recebeCorpo) {
        String titulo = switch (codigo) {
            case "400" -> recebeCorpo ? "Dados inválidos" : "Requisição inválida";
            case "404" -> "Não encontrado";
            case "405" -> "Método não permitido";
            case "409" -> "Conflito de dados";
            case "500" -> "Erro interno";
            default -> "Erro";
        };

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", "2026-10-27T16:53:52");
        corpo.put("status", Integer.parseInt(codigo));
        corpo.put("erro", titulo);

        if ("400".equals(codigo) && recebeCorpo) {
            corpo.put("mensagem", "Um ou mais campos possuem valores inválidos.");
            corpo.put("campos", Map.of("nome", "O nome é obrigatório"));
        } else {
            corpo.put("mensagem", descricao == null ? titulo + "." : descricao + ".");
        }

        return corpo;
    }
}

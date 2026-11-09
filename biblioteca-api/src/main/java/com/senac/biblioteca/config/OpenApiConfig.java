package com.senac.biblioteca.config;

import com.senac.biblioteca.controller.LivroController;
import com.senac.biblioteca.dto.ErroResposta;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.seguranca.IdempotenciaFilter;
import com.senac.biblioteca.seguranca.RegrasDeAcesso;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
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
            usuários, carteirinhas, autores, categorias, livros e empréstimos.

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
@SecurityScheme(
    name = OpenApiConfig.ESQUEMA_CHAVE_API,
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-API-Key",
    description = "Chave de API gerada em POST /chaves-api. Clique em Authorize e cole a chave para testar as rotas protegidas."
)
public class OpenApiConfig {

    // As informações gerais do Swagger são definidas pelas anotações acima.

    /** Nome do esquema de segurança da chave de API no Swagger. */
    static final String ESQUEMA_CHAVE_API = "chaveApi";

    private static final String SUFIXO_PAGINA_INEXISTENTE = " ou página inexistente";

    /**
     * Rotas públicas em que a chave de API é opcional, mas muda o resultado: o cadastro de
     * usuários FUNCIONARIO/PROFESSOR e a definição da primeira senha exigem chave de ESCRITA.
     */
    private static final List<String> CHAVE_OPCIONAL = List.of("POST /usuarios", "PUT /usuarios/{id}/senha");

    /**
     * Documentação da versão 1 da API (a padrão). No Swagger UI, escolha a versão no seletor do topo.
     * Cada grupo mostra as rotas da sua versão mais as rotas sem versão, que valem para as duas.
     */
    @Bean
    public GroupedOpenApi versao1() {
        return documentacaoDaVersao("1", "Versão 1 (padrão)");
    }

    /** Documentação da versão 2 da API (cabeçalho X-API-Version: 2). */
    @Bean
    public GroupedOpenApi versao2() {
        return documentacaoDaVersao("2", "Versão 2");
    }

    private GroupedOpenApi documentacaoDaVersao(String versao, String nome) {
        return GroupedOpenApi.builder()
            .group("v" + versao)
            .displayName(nome)
            .pathsToMatch("/**")
            .addOpenApiMethodFilter(metodo -> {
                RequestMapping mapeamento = AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class);
                return mapeamento == null || mapeamento.version().isEmpty() || mapeamento.version().equals(versao);
            })
            .addOperationCustomizer((operacao, metodo) -> {
                if (operacao.getParameters() != null) {
                    operacao.getParameters().stream()
                        .filter(parametro -> LivroController.CABECALHO_VERSAO.equals(parametro.getName()))
                        .forEach(parametro -> parametro
                            .description("Versão da API: 1 (padrão, quando o cabeçalho não é enviado) ou 2.")
                            .example(versao));
                }
                return operacao;
            })
            .build();
    }

    /**
     * Completa a documentação gerada automaticamente:
     *  - o corpo das respostas de erro (4xx e 5xx) passa a usar o esquema ErroResposta, com exemplo;
     *  - as listagens paginadas explicam os limites de "page", "size" e "sort" e documentam o 404
     *    de página inexistente.
     *
     * É "Global" para valer também na documentação de cada versão (grupos v1 e v2).
     *
     * Só altera o que existe: partes ausentes da especificação são ignoradas, nunca causam erro.
     */
    @Bean
    public GlobalOpenApiCustomizer respostasDeErro(PaginacaoConfig paginacao) {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            Components componentes = openApi.getComponents() != null ? openApi.getComponents() : new Components();
            openApi.setComponents(componentes);
            ModelConverters.getInstance().read(ErroResposta.class).forEach(componentes::addSchemas);
            Schema<?> esquema = new Schema<>().$ref("#/components/schemas/ErroResposta");

            openApi.getPaths().forEach((rota, caminho) -> caminho.readOperationsMap().forEach((metodo, operacao) -> {
                if (operacao.getResponses() == null) {
                    return;
                }

                documentarProtecoes(metodo.name(), rota, operacao);

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

    /**
     * Documenta o limite de requisições (429) em todas as rotas da API, o cabeçalho de
     * idempotência nos POSTs e quais rotas exigem a chave de API, usando as mesmas regras dos filtros.
     * Rotas protegidas ganham o cadeado, o nível exigido na descrição e as respostas 401 e 403.
     */
    private void documentarProtecoes(String metodo, String rota, Operation operacao) {
        if (!rota.startsWith("/") || rota.startsWith("/v3") || rota.startsWith("/swagger")) {
            return;
        }

        operacao.getResponses().putIfAbsent("429", new ApiResponse()
            .description("Limite de requisições por minuto atingido (veja o cabeçalho Retry-After)")
            .addHeaderObject("Retry-After", new Header()
                .description("Segundos de espera até poder tentar de novo")
                .schema(new IntegerSchema())));

        if (IdempotenciaFilter.aceitaIdempotencia(metodo, rota)) {
            documentarIdempotencia(operacao);
        }

        NivelAcesso exigido = RegrasDeAcesso.nivelExigido(metodo, rota);
        if (exigido == null) {
            // Rotas públicas em que a chave muda o resultado: o Swagger a envia se você clicou em Authorize.
            if (CHAVE_OPCIONAL.contains(metodo + " " + rota)) {
                operacao.setSecurity(List.of(new SecurityRequirement(), new SecurityRequirement().addList(ESQUEMA_CHAVE_API)));
            }
            return;
        }

        operacao.setSecurity(List.of(new SecurityRequirement().addList(ESQUEMA_CHAVE_API)));
        String descricao = operacao.getDescription() == null ? "" : operacao.getDescription() + "\n\n";
        operacao.setDescription(descricao + "**Exige chave de API (X-API-Key) de nível " + exigido + " ou superior.**");
        operacao.getResponses().putIfAbsent("401",
            new ApiResponse().description("Chave de API ausente, inexistente ou revogada"));
        operacao.getResponses().putIfAbsent("403",
            new ApiResponse().description("A chave de API não tem o nível " + exigido + " exigido por esta operação"));
    }

    /** Documenta o cabeçalho X-Idempotency-Key e as respostas 409 e 422 nos POSTs que o aceitam. */
    private void documentarIdempotencia(Operation operacao) {
        operacao.addParametersItem(new HeaderParameter()
            .name(IdempotenciaFilter.CABECALHO)
            .required(false)
            .description("Opcional. Identificador único desta operação (sugestão: um UUID). Reenviando a mesma "
                + "requisição com a mesma chave em até 24 horas, a API devolve a resposta original, com o "
                + "cabeçalho X-Idempotency-Replayed: true, em vez de cadastrar de novo.")
            .schema(new StringSchema().maxLength(100))
            .example("3f1c9a52-7d4e-4b8a-9c6f-2e5d8b1a0c47"));

        ApiResponse conflito = operacao.getResponses().get("409");
        String emAndamento = "Requisição com a mesma X-Idempotency-Key ainda em andamento";
        if (conflito == null) {
            operacao.getResponses().addApiResponse("409", new ApiResponse().description(emAndamento));
        } else if (!conflito.getDescription().contains("X-Idempotency-Key")) {
            conflito.setDescription(conflito.getDescription() + ", ou " + emAndamento.toLowerCase());
        }
        operacao.getResponses().putIfAbsent("422",
            new ApiResponse().description("X-Idempotency-Key já usada com outra requisição"));
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
            case "401" -> "Não autorizado";
            case "403" -> "Acesso negado";
            case "404" -> "Não encontrado";
            case "405" -> "Método não permitido";
            case "409" -> "Conflito de dados";
            case "422" -> "Chave de idempotência reutilizada";
            case "429" -> "Muitas requisições";
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

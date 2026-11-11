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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
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

    /** Rotas da Parte 1: CRUD das entidades, paginação, consultas personalizadas e HATEOAS. */
    private static final String[] ROTAS_DA_PARTE_1 = {
        "/livros/**", "/autores/**", "/categorias/**", "/usuarios/**", "/carteirinhas/**", "/emprestimos/**"
    };

    private static final String MARCADOR_VERSAO = "**Versão ";

    private static final String RESUMO_VERSAO_1 = """
        **Versão 1 (Parte 1).** Mostra o CRUD das entidades (livros, autores, categorias, usuários, carteirinhas e \
        empréstimos), a paginação, as consultas personalizadas, os links HATEOAS e as respostas de erro. \
        Os recursos avançados (chave de API, limite de requisições, idempotência, versionamento por cabeçalho) \
        aparecem na **Versão 2**: na API publicada, as rotas que alteram dados ou mostram dados pessoais exigem a \
        chave de API descrita lá.""";

    private static final String RESUMO_VERSAO_2 = """
        **Versão 2 (Parte 1 + Parte 2).** Tudo da versão 1 mais os recursos avançados: autenticação por chave de API \
        (`X-API-Key`, com `/chaves-api` e o botão Authorize), limite de requisições (429 e `Retry-After`), idempotência \
        nos POSTs (`X-Idempotency-Key`), CORS, versionamento por cabeçalho (`X-API-Version`, com duas versões de \
        `GET /livros`) e as reservas do cliente.""";

    /**
     * Documentação da versão 1 da API (a padrão): só o que a Parte 1 pede. No Swagger UI, escolha a
     * versão no seletor do topo.
     */
    @Bean
    public GroupedOpenApi versao1(PaginacaoConfig paginacao, @Value("${biblioteca.dados-exemplo:false}") boolean exemplos) {
        return documentacaoDaVersao("1", "Versão 1 (Parte 1)", paginacao, exemplos);
    }

    /** Documentação da versão 2 da API (cabeçalho X-API-Version: 2): Parte 1 mais os recursos avançados da Parte 2. */
    @Bean
    public GroupedOpenApi versao2(PaginacaoConfig paginacao, @Value("${biblioteca.dados-exemplo:false}") boolean exemplos) {
        return documentacaoDaVersao("2", "Versão 2 (Parte 1 + Parte 2)", paginacao, exemplos);
    }

    private GroupedOpenApi documentacaoDaVersao(String versao, String nome, PaginacaoConfig paginacao, boolean exemplos) {
        boolean completa = "2".equals(versao);
        GroupedOpenApi.Builder grupo = GroupedOpenApi.builder().group("v" + versao).displayName(nome);
        if (completa) {
            grupo.pathsToMatch("/**");
        } else {
            // A versão 1 não mostra as rotas da Parte 2 nem a troca de senha (parte do login por chave de API).
            grupo.pathsToMatch(ROTAS_DA_PARTE_1).pathsToExclude("/usuarios/*/senha");
        }
        return grupo
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
            .addOpenApiCustomizer(openApi -> completar(openApi, completa, paginacao.getTamanhoMaximo(), exemplos))
            .build();
    }

    /**
     * Completa a documentação gerada automaticamente, para cada versão:
     *  - o corpo das respostas de erro (4xx e 5xx) passa a usar o esquema ErroResposta, com exemplo;
     *  - as listagens paginadas explicam os limites de "page", "size" e "sort" e documentam o 404
     *    de página inexistente;
     *  - só na versão 2 (Parte 1 + Parte 2): chave de API, limite de requisições e idempotência;
     *  - na versão 1 (Parte 1), tira tudo o que é da Parte 2.
     *
     * Só altera o que existe: partes ausentes da especificação são ignoradas, nunca causam erro.
     */
    private void completar(OpenAPI openApi, boolean completa, int tamanhoMaximo, boolean exemplos) {
        if (openApi.getPaths() == null) {
            return;
        }

        descreverVersao(openApi, completa, exemplos);
        ordenarGrupos(openApi);

        if (!completa) {
            ajustarParaAParte1(openApi);
        }

        Components componentes = openApi.getComponents() != null ? openApi.getComponents() : new Components();
        openApi.setComponents(componentes);
        ModelConverters.getInstance().read(ErroResposta.class).forEach(componentes::addSchemas);
        Schema<?> esquema = new Schema<>().$ref("#/components/schemas/ErroResposta");

        openApi.getPaths().forEach((rota, caminho) -> caminho.readOperationsMap().forEach((metodo, operacao) -> {
            if (operacao.getResponses() == null) {
                return;
            }

            if (completa) {
                documentarProtecoes(metodo.name(), rota, operacao);
            } else {
                marcarCadeado(metodo.name(), rota, operacao);
            }

            if (ehListagemPaginada(operacao)) {
                descreverPaginacao(operacao, tamanhoMaximo);
            }

            boolean recebeCorpo = operacao.getRequestBody() != null;
            operacao.getResponses().forEach((codigo, resposta) -> {
                if (codigo.startsWith("4") || codigo.startsWith("5")) {
                    resposta.setContent(new Content().addMediaType("application/json",
                        new MediaType().schema(esquema).example(exemplo(codigo, resposta.getDescription(), recebeCorpo))));
                }
            });
        }));
    }

    /** Título da versão, passo a passo de teste e servidor relativo (o Try it out funciona em qualquer endereço). */
    private void descreverVersao(OpenAPI openApi, boolean completa, boolean exemplos) {
        io.swagger.v3.oas.models.info.Info info = openApi.getInfo();
        if (info != null && (info.getDescription() == null || !info.getDescription().contains(MARCADOR_VERSAO))) {
            String base = info.getDescription() == null ? "" : info.getDescription() + "\n\n";
            info.setDescription(base + (completa ? RESUMO_VERSAO_2 : RESUMO_VERSAO_1) + "\n\n" + passoAPasso(completa, exemplos));
            info.setVersion(completa ? "2.0.0" : "1.0.0");
        }
        // Um servidor com endereço fixo faria o Try it out chamar outra máquina que não a da API publicada.
        // Com "/", o Swagger usa o mesmo endereço em que foi aberto, seja ele qual for.
        openApi.setServers(List.of(new io.swagger.v3.oas.models.servers.Server().url("/").description("Este servidor")));
    }

    /** Como executar as rotas no Swagger: o catálogo é aberto; o resto precisa da chave (botão Authorize). */
    private String passoAPasso(boolean completa, boolean exemplos) {
        String contas = exemplos
            ? "\n\n**Conta de cliente de exemplo:** `" + DadosDeExemplo.EMAIL_DE_EXEMPLO + "` / `" + DadosDeExemplo.SENHA_DOS_CLIENTES
                + "` (nível LEITURA: só reservas). Para ser funcionário, use a conta do dono da biblioteca."
            : "";
        if (!completa) {
            return "**Para testar:** as listagens e buscas do catálogo (livros, autores e categorias) abrem sem chave. "
                + "Para cadastrar, alterar, excluir ou ver dados pessoais, clique em **Authorize** e cole uma chave de API de "
                + "funcionário (como gerar: na **Versão 2**, `POST /chaves-api`). A chave fica guardada neste navegador, "
                + "inclusive ao trocar de versão." + contas;
        }
        return "**Passo a passo para testar:**\n"
            + "1. Abra `POST /chaves-api`, clique em **Try it out** e informe e-mail e senha de um usuário "
            + "(`nivel`: `ADMIN` para funcionário, `LEITURA` para cliente).\n"
            + "2. Copie o campo `chave` da resposta (ela só aparece uma vez), clique em **Authorize**, cole e confirme.\n"
            + "3. Pronto: as rotas com cadeado funcionam. Para ver a idempotência, execute um POST duas vezes com o mesmo "
            + "`X-Idempotency-Key`; para o limite de requisições, repita uma rota até receber 429."
            + contas;
    }

    /** Ordem dos grupos no Swagger: primeiro as entidades da Parte 1, depois os recursos avançados. */
    private static final List<String> ORDEM_DOS_GRUPOS = List.of(
        "Categorias", "Autores", "Livros", "Usuários", "Carteirinhas", "Empréstimos", "Autenticação", "Chaves de API", "Reservas");

    private void ordenarGrupos(OpenAPI openApi) {
        if (openApi.getTags() == null) {
            return;
        }
        List<io.swagger.v3.oas.models.tags.Tag> ordenados = new java.util.ArrayList<>(openApi.getTags());
        ordenados.sort(java.util.Comparator.comparingInt(tag -> {
            int posicao = ORDEM_DOS_GRUPOS.indexOf(tag.getName());
            return posicao < 0 ? ORDEM_DOS_GRUPOS.size() : posicao;
        }));
        openApi.setTags(ordenados);
    }

    /** Versão 1: só o cadeado (e o Authorize) nas rotas protegidas, sem as respostas 401/403/429 da Parte 2. */
    private void marcarCadeado(String metodo, String rota, Operation operacao) {
        if (!rota.startsWith("/") || rota.startsWith("/v3") || rota.startsWith("/swagger")) {
            return;
        }
        if (RegrasDeAcesso.nivelExigido(metodo, rota) != null) {
            operacao.setSecurity(List.of(new SecurityRequirement().addList(ESQUEMA_CHAVE_API)));
        } else if (CHAVE_OPCIONAL.contains(metodo + " " + rota)) {
            operacao.setSecurity(List.of(new SecurityRequirement(), new SecurityRequirement().addList(ESQUEMA_CHAVE_API)));
        }
    }

    /**
     * Versão 1 = só a Parte 1: tira o que é dos recursos avançados (chave de API, cabeçalho de
     * versão) e dos extras que vieram com eles (senha, reservas), nas rotas que ela mostra.
     */
    private void ajustarParaAParte1(OpenAPI openApi) {
        // O botão Authorize fica na versão 1: sem a chave, nenhuma rota que grava dados executa no Swagger.
        Components componentes = openApi.getComponents();
        if (componentes != null) {
            io.swagger.v3.oas.models.security.SecurityScheme esquemaDaChave = componentes.getSecuritySchemes() == null ? null : componentes.getSecuritySchemes().get(ESQUEMA_CHAVE_API);
            if (esquemaDaChave != null) {
                esquemaDaChave.setDescription("Chave de API (cabeçalho X-API-Key), necessária para executar as rotas que gravam dados "
                    + "ou mostram dados pessoais. Para gerar uma, use a **Versão 2**: POST /chaves-api.");
            }
            if (componentes.getSchemas() != null) {
                // Vale para todos os esquemas, inclusive as versões com links (EntityModelUsuario...).
                componentes.getSchemas().values().forEach(esquema -> {
                    removerPropriedade(esquema, "senha");
                    removerPropriedade(esquema, "retirarAte");
                    removerValorReservado(esquema);
                });
            }
        }

        openApi.getPaths().forEach((rota, caminho) -> caminho.readOperationsMap().forEach((metodo, operacao) -> {
            if (operacao.getParameters() != null) {
                operacao.getParameters().removeIf(parametro -> LivroController.CABECALHO_VERSAO.equals(parametro.getName()));
                operacao.getParameters().forEach(parametro -> removerValorReservado(parametro.getSchema()));
            }
            if (operacao.getResponses() == null) {
                return;
            }
            switch (metodo.name() + " " + rota) {
                case "GET /livros" -> {
                    operacao.setSummary("Listar livros");
                    operacao.setDescription("Retorna os livros cadastrados de forma paginada, com a categoria e os autores completos.");
                    operacao.getResponses().get("400").setDescription("Parâmetro de paginação ou ordenação inválido");
                }
                case "GET /livros/{id}" -> {
                    operacao.setSummary("Buscar livro por ID");
                    operacao.setDescription("Retorna um livro a partir do seu identificador, com a categoria e os autores completos.");
                    operacao.getResponses().get("400").setDescription("ID informado em formato inválido");
                }
                case "POST /usuarios" -> {
                    operacao.setDescription("Cria um novo usuário na biblioteca. E-mail e CPF não podem se repetir.");
                    operacao.getResponses().remove("403");
                }
                case "PUT /usuarios/{id}" -> operacao.setDescription("Atualiza nome, e-mail, CPF e tipo de um usuário existente.");
                default -> { }
            }
        }));
    }

    private void removerPropriedade(Schema<?> esquema, String propriedade) {
        if (esquema != null && esquema.getProperties() != null) {
            esquema.getProperties().remove(propriedade);
        }
    }

    /** RESERVADO é um status das reservas (extra): fora da versão 1. */
    private void removerValorReservado(Schema<?> esquema) {
        if (esquema == null) {
            return;
        }
        if (esquema.getEnum() != null) {
            esquema.getEnum().removeIf("RESERVADO"::equals);
        }
        if (esquema.getProperties() != null) {
            esquema.getProperties().values().forEach(this::removerValorReservado);
        }
        removerValorReservado(esquema.getItems());
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
            .description("Opcional (deixe em branco para um POST normal). Para testar a idempotência, informe um "
                + "identificador único, como um UUID, e execute DUAS vezes com o mesmo valor: a segunda devolve a "
                + "resposta original, com o cabeçalho X-Idempotency-Replayed: true, em vez de cadastrar de novo. "
                + "Exemplo: 3f1c9a52-7d4e-4b8a-9c6f-2e5d8b1a0c47")
            .schema(new StringSchema().maxLength(100)));

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

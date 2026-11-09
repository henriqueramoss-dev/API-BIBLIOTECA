package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.ChaveApiModelAssembler;
import com.senac.biblioteca.config.Paginas;
import com.senac.biblioteca.dto.ChaveApiCriada;
import com.senac.biblioteca.dto.ChaveApiRequest;
import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.exception.NaoAutenticadoException;
import com.senac.biblioteca.seguranca.ChaveApiFilter;
import com.senac.biblioteca.service.ChaveApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Geração e gerenciamento das chaves de API usadas no cabeçalho X-API-Key.
 */
@RestController
@RequestMapping("/chaves-api")
@Tag(name = "Chaves de API", description = "Geração e gerenciamento das chaves de acesso (cabeçalho X-API-Key)")
public class ChaveApiController {

    private final ChaveApiService service;
    private final ChaveApiModelAssembler assembler;
    private final PagedResourcesAssembler<ChaveApi> pagedResourcesAssembler;

    public ChaveApiController(
        ChaveApiService service,
        ChaveApiModelAssembler assembler,
        PagedResourcesAssembler<ChaveApi> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    // POST /chaves-api: gera uma chave conferindo e-mail e senha (rota pública).
    @PostMapping
    @Operation(
        summary = "Gerar chave de API",
        description = "Confere o e-mail e a senha e gera uma chave para esse usuário. A chave completa aparece "
            + "SOMENTE nesta resposta: guarde-a e envie-a no cabeçalho X-API-Key. "
            + "Níveis: LEITURA (padrão), ESCRITA e ADMIN; só usuários FUNCIONARIO podem pedir ESCRITA ou ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Chave gerada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos"),
        @ApiResponse(responseCode = "403", description = "Nível pedido acima do permitido para o tipo do usuário")
    })
    public ResponseEntity<EntityModel<ChaveApiCriada>> gerar(@Valid @RequestBody ChaveApiRequest item) {
        ChaveApiCriada criada = service.gerar(item);
        return ResponseEntity
            .created(linkTo(ChaveApiController.class).slash(criada.getId()).toUri())
            .body(assembler.toModel(criada));
    }

    // GET /chaves-api: lista as chaves (ADMIN vê todas; os demais, só as próprias).
    @GetMapping
    @Operation(
        summary = "Listar chaves de API",
        description = "Lista as chaves do dono da chave usada na requisição. Com uma chave ADMIN, lista todas. "
            + "A chave completa nunca aparece, só o prefixo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<ChaveApi>>> listar(
        HttpServletRequest request,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<ChaveApi> pagina = Paginas.conferir(service.listar(chaveAtual(request), pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // GET /chaves-api/atual: mostra a chave usada na própria requisição.
    @GetMapping("/atual")
    @Operation(
        summary = "Consultar a chave em uso",
        description = "Mostra os dados (sem a chave completa) da chave enviada no cabeçalho X-API-Key."
    )
    @ApiResponse(responseCode = "200", description = "Dados da chave em uso")
    public ResponseEntity<EntityModel<ChaveApi>> atual(HttpServletRequest request) {
        return ResponseEntity.ok(assembler.toModel(chaveAtual(request)));
    }

    // GET /chaves-api/{id}: consulta uma chave própria (ou qualquer uma, com ADMIN).
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar chave de API por ID",
        description = "Retorna uma chave do mesmo dono da chave usada (ou qualquer chave, com ADMIN)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chave encontrada"),
        @ApiResponse(responseCode = "404", description = "Chave não encontrada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<ChaveApi>> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id, chaveAtual(request))));
    }

    // DELETE /chaves-api/{id}: revoga uma chave.
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Revogar chave de API",
        description = "A chave deixa de funcionar imediatamente, mas continua na listagem como inativa. "
            + "Cada um revoga as próprias chaves; com ADMIN, qualquer chave."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Chave revogada"),
        @ApiResponse(responseCode = "404", description = "Chave não encontrada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<Void> revogar(@PathVariable Long id, HttpServletRequest request) {
        service.revogar(id, chaveAtual(request));
        return ResponseEntity.noContent().build();
    }

    // O ChaveApiFilter já garante a chave nestas rotas; a conferência aqui é só uma proteção extra.
    private ChaveApi chaveAtual(HttpServletRequest request) {
        return ChaveApiFilter.chaveDa(request).orElseThrow(NaoAutenticadoException::semChave);
    }
}

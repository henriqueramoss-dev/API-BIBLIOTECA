package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.CarteirinhaModelAssembler;
import com.senac.biblioteca.config.Paginas;
import com.senac.biblioteca.dto.CarteirinhaRequest;
import com.senac.biblioteca.entity.Carteirinha;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.CarteirinhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller das carteirinhas. Expõe o CRUD, a listagem paginada e a busca
 * personalizada por número. A camada Service contém as regras de negócio.
 */
@RestController
@RequestMapping("/carteirinhas")
@Tag(name = "Carteirinhas", description = "Endpoints para gerenciamento das carteirinhas da biblioteca")
public class CarteirinhaController {

    // Service concentra as regras de negócio desta entidade.
    // Acesso às regras de negócio de Carteirinha.
    private final CarteirinhaService service;
    // Assembler adiciona os links HATEOAS nas respostas.
    // Adiciona links HATEOAS ao recurso retornado.
    private final CarteirinhaModelAssembler assembler;
    // Responsável por transformar Page em PagedModel.
    // Monta a resposta paginada no formato HATEOAS.
    private final PagedResourcesAssembler<Carteirinha> pagedResourcesAssembler;

    public CarteirinhaController(
        CarteirinhaService service,
        CarteirinhaModelAssembler assembler,
        PagedResourcesAssembler<Carteirinha> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    // Listagem paginada: aceita page, size e sort.
    @GetMapping
    @Operation(
        summary = "Listar carteirinhas",
        description = "Retorna as carteirinhas cadastradas de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Carteirinha>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Carteirinha> pagina = Paginas.conferir(service.listar(pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // Busca individual pelo identificador.
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar carteirinha por ID",
        description = "Retorna uma carteirinha a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carteirinha encontrada"),
        @ApiResponse(responseCode = "404", description = "Carteirinha não encontrada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Carteirinha>> buscarPorId(@Parameter(description = "ID da carteirinha", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.carteirinha(id));
    }

    // Cadastro de um novo recurso validado com Bean Validation.
    @PostMapping
    @Operation(
        summary = "Cadastrar carteirinha",
        description = "Cria a carteirinha de um usuário já cadastrado. Cada usuário pode ter apenas uma carteirinha; o número não pode se repetir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Carteirinha criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito de dados, como valor duplicado")
    })
    public ResponseEntity<EntityModel<Carteirinha>> criar(@Valid @RequestBody CarteirinhaRequest item) {
        Carteirinha criada = service.criar(item);
        EntityModel<Carteirinha> model = assembler.toModel(criada);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    // Atualização de um recurso existente.
    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar carteirinha",
        description = "Atualiza número, datas, status, limite de empréstimos e usuário de uma carteirinha existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carteirinha atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Carteirinha não encontrada"),
        @ApiResponse(responseCode = "409", description = "Número já usado em outra carteirinha ou usuário que já possui carteirinha")
    })
    public ResponseEntity<EntityModel<Carteirinha>> atualizar(
        @Parameter(description = "ID da carteirinha", example = "1") @PathVariable Long id,
        @Valid @RequestBody CarteirinhaRequest item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.carteirinha(id));
    }

    // Exclusão pelo identificador.
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir carteirinha",
        description = "Exclui uma carteirinha pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Carteirinha excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Carteirinha não encontrada")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da carteirinha", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw RecursoNaoEncontradoException.carteirinha(id);
        }

        return ResponseEntity.noContent().build();
    }

    // Consulta personalizada por número, mantendo a paginação.
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar carteirinhas por número",
        description = "Busca carteirinhas pelo número usando paginação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Carteirinha>>> buscar(
        @Parameter(description = "Parte do número a ser pesquisada", example = "CB-0001")
        @RequestParam String numero,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Carteirinha> pagina = Paginas.conferir(service.buscarPorNumero(numero, pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }
}

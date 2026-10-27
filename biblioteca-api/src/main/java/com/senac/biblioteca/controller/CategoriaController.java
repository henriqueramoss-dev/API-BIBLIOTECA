package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.CategoriaModelAssembler;
import com.senac.biblioteca.assembler.LivroModelAssembler;
import com.senac.biblioteca.entity.Categoria;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.CategoriaService;
import com.senac.biblioteca.service.LivroService;
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
 * Controller responsável pelas rotas de categorias.
 * Organiza as operações HTTP e delega o acesso aos dados para o Service.
 */
@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento das categorias de livros")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaModelAssembler assembler;
    private final PagedResourcesAssembler<Categoria> pagedResourcesAssembler;
    private final LivroService livroService;
    private final LivroModelAssembler livroAssembler;
    private final PagedResourcesAssembler<Livro> livroPagedAssembler;

    public CategoriaController(
        CategoriaService service,
        CategoriaModelAssembler assembler,
        PagedResourcesAssembler<Categoria> pagedResourcesAssembler,
        LivroService livroService,
        LivroModelAssembler livroAssembler,
        PagedResourcesAssembler<Livro> livroPagedAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.livroService = livroService;
        this.livroAssembler = livroAssembler;
        this.livroPagedAssembler = livroPagedAssembler;
    }

    @GetMapping
    @Operation(
        summary = "Listar categorias",
        description = "Retorna as categorias cadastradas de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Categoria> pagina = service.listar(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar categoria por ID",
        description = "Retorna uma categoria a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Categoria>> buscarPorId(@Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar categoria",
        description = "Cria uma nova categoria de livros. O código (ex.: ROM) é obrigatório e não pode se repetir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<Categoria>> criar(@Valid @RequestBody Categoria item) {
        Categoria criada = service.criar(item);
        EntityModel<Categoria> model = assembler.toModel(criada);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar categoria",
        description = "Atualiza nome, código, descrição, localização e público-alvo de uma categoria existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<EntityModel<Categoria>> atualizar(
        @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id,
        @Valid @RequestBody Categoria item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir categoria",
        description = "Exclui uma categoria pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
        @ApiResponse(responseCode = "409", description = "Categoria possui livros e não pode ser excluída")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw naoEncontrado(id);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar categorias por nome",
        description = "Busca categorias cujo nome contém o texto informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> buscar(
        @Parameter(description = "Texto que deve estar contido no nome da categoria", example = "Romance")
        @RequestParam String nome,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Categoria> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}/livros")
    @Operation(
        summary = "Listar livros da categoria",
        description = "Retorna, de forma paginada, os livros que pertencem à categoria informada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID, paginação ou ordenação inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listarLivros(
        @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Livro> pagina = livroService.listarPorCategoria(id, pageable);
        return ResponseEntity.ok(livroPagedAssembler.toModel(pagina, livroAssembler));
    }

    // Erro 404 com mensagem, tratado no ApiExceptionHandler.
    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Categoria com ID " + id + " não encontrada.");
    }
}

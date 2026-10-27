package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.EmprestimoModelAssembler;
import com.senac.biblioteca.assembler.LivroModelAssembler;
import com.senac.biblioteca.dto.LivroRequest;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.EmprestimoService;
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
 * Controller responsável pelas rotas de livros.
 * Disponibiliza CRUD, paginação e busca por título.
 */
@RestController
@RequestMapping("/livros")
@Tag(name = "Livros", description = "Endpoints para gerenciamento dos livros")
public class LivroController {

    private final LivroService service;
    private final LivroModelAssembler assembler;
    private final PagedResourcesAssembler<Livro> pagedResourcesAssembler;
    private final EmprestimoService emprestimoService;
    private final EmprestimoModelAssembler emprestimoAssembler;
    private final PagedResourcesAssembler<Emprestimo> emprestimoPagedAssembler;

    public LivroController(
        LivroService service,
        LivroModelAssembler assembler,
        PagedResourcesAssembler<Livro> pagedResourcesAssembler,
        EmprestimoService emprestimoService,
        EmprestimoModelAssembler emprestimoAssembler,
        PagedResourcesAssembler<Emprestimo> emprestimoPagedAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.emprestimoService = emprestimoService;
        this.emprestimoAssembler = emprestimoAssembler;
        this.emprestimoPagedAssembler = emprestimoPagedAssembler;
    }

    @GetMapping
    @Operation(
        summary = "Listar livros",
        description = "Retorna os livros cadastrados de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Livro> pagina = service.listar(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar livro por ID",
        description = "Retorna um livro a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Livro encontrado"),
        @ApiResponse(responseCode = "404", description = "Livro não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Livro>> buscarPorId(@Parameter(description = "ID do livro", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar livro",
        description = "Cria um livro utilizando uma categoria e autores já cadastrados. ISBN não pode se repetir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito de dados, como valor duplicado")
    })
    public ResponseEntity<EntityModel<Livro>> criar(@Valid @RequestBody LivroRequest item) {
        Livro criado = service.criar(item);
        EntityModel<Livro> model = assembler.toModel(criado);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar livro",
        description = "Atualiza título, ISBN, ano de publicação, editora, número de páginas, categoria e autores de um livro existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Livro não encontrado"),
        @ApiResponse(responseCode = "409", description = "ISBN já cadastrado para outro livro")
    })
    public ResponseEntity<EntityModel<Livro>> atualizar(
        @Parameter(description = "ID do livro", example = "1") @PathVariable Long id,
        @Valid @RequestBody LivroRequest item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir livro",
        description = "Exclui um livro pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Livro excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Livro não encontrado"),
        @ApiResponse(responseCode = "409", description = "Livro possui empréstimos e não pode ser excluído")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do livro", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw naoEncontrado(id);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar livros por título",
        description = "Busca livros cujo título contém o texto informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> buscar(
        @Parameter(description = "Texto que deve estar contido no título do livro", example = "Dom")
        @RequestParam String titulo,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Livro> pagina = service.buscarPorTitulo(titulo, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}/emprestimos")
    @Operation(
        summary = "Histórico de empréstimos do livro",
        description = "Retorna, de forma paginada, todos os empréstimos do livro informado, do mais antigo ao mais recente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID, paginação ou ordenação inválidos"),
        @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listarEmprestimos(
        @Parameter(description = "ID do livro", example = "1") @PathVariable Long id,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Emprestimo> pagina = emprestimoService.listarPorLivro(id, pageable);
        return ResponseEntity.ok(emprestimoPagedAssembler.toModel(pagina, emprestimoAssembler));
    }

    // Erro 404 com mensagem, tratado no ApiExceptionHandler.
    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Livro com ID " + id + " não encontrado.");
    }
}

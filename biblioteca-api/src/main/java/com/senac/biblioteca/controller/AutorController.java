package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.AutorModelAssembler;
import com.senac.biblioteca.assembler.LivroModelAssembler;
import com.senac.biblioteca.config.Paginas;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.AutorService;
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
 * Controller de autores da biblioteca.
 * Expõe as operações REST e utiliza o Service para acessar as regras de negócio.
 */
@RestController
@RequestMapping("/autores")
@Tag(name = "Autores", description = "Endpoints para gerenciamento dos autores")
public class AutorController {

    private final AutorService service;
    private final AutorModelAssembler assembler;
    private final PagedResourcesAssembler<Autor> pagedResourcesAssembler;
    private final LivroService livroService;
    private final LivroModelAssembler livroAssembler;
    private final PagedResourcesAssembler<Livro> livroPagedAssembler;

    public AutorController(
        AutorService service,
        AutorModelAssembler assembler,
        PagedResourcesAssembler<Autor> pagedResourcesAssembler,
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
        summary = "Listar autores",
        description = "Retorna os autores cadastrados de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Autor>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Autor> pagina = Paginas.conferir(service.listar(pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar autor por ID",
        description = "Retorna um autor a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor encontrado"),
        @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Autor>> buscarPorId(@Parameter(description = "ID do autor", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.autor(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar autor",
        description = "Cria um novo autor na biblioteca. Nacionalidade é obrigatória; data de nascimento, gênero literário e biografia são opcionais."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Autor criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<Autor>> criar(@Valid @RequestBody Autor item) {
        Autor criado = service.criar(item);
        EntityModel<Autor> model = assembler.toModel(criado);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar autor",
        description = "Atualiza nome, nacionalidade, data de nascimento, gênero literário e biografia de um autor existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Autor não encontrado")
    })
    public ResponseEntity<EntityModel<Autor>> atualizar(
        @Parameter(description = "ID do autor", example = "1") @PathVariable Long id,
        @Valid @RequestBody Autor item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.autor(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir autor",
        description = "Exclui um autor pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Autor excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
        @ApiResponse(responseCode = "409", description = "Autor está vinculado a livros e não pode ser excluído")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do autor", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw RecursoNaoEncontradoException.autor(id);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar autores por nome",
        description = "Busca autores cujo nome contém o texto informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Autor>>> buscar(
        @Parameter(description = "Texto que deve estar contido no nome do autor", example = "Machado")
        @RequestParam String nome,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Autor> pagina = Paginas.conferir(service.buscarPorNome(nome, pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}/livros")
    @Operation(
        summary = "Listar livros do autor",
        description = "Retorna, de forma paginada, os livros escritos pelo autor informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID, paginação ou ordenação inválidos"),
        @ApiResponse(responseCode = "404", description = "Autor não encontrado")
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listarLivros(
        @Parameter(description = "ID do autor", example = "1") @PathVariable Long id,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Livro> pagina = Paginas.conferir(livroService.listarPorAutor(id, pageable));
        return ResponseEntity.ok(livroPagedAssembler.toModel(pagina, livroAssembler));
    }
}

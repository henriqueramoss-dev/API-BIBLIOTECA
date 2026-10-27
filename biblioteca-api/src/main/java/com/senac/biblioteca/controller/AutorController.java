package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.AutorModelAssembler;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.AutorService;
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
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
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

    public AutorController(
        AutorService service,
        AutorModelAssembler assembler,
        PagedResourcesAssembler<Autor> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
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
        Page<Autor> pagina = service.listar(pageable);
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
    public ResponseEntity<EntityModel<Autor>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar autor",
        description = "Cria um novo autor na biblioteca."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Autor criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<Autor>> criar(@Valid @RequestBody Autor item) {
        Autor criado = service.criar(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(criado));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar autor",
        description = "Atualiza o nome de um autor existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Autor não encontrado")
    })
    public ResponseEntity<EntityModel<Autor>> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody Autor item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
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
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!service.excluir(id)) {
            throw naoEncontrado(id);
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
        Page<Autor> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // Erro 404 com mensagem, tratado no ApiExceptionHandler.
    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Autor com ID " + id + " não encontrado.");
    }
}

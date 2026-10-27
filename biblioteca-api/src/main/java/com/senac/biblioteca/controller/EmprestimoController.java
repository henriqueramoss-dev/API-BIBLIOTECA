package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.EmprestimoModelAssembler;
import com.senac.biblioteca.dto.EmprestimoRequest;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.enums.StatusEmprestimo;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.EmprestimoService;
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
 * Controller de empréstimos da biblioteca.
 */
@RestController
@RequestMapping("/emprestimos")
@Tag(name = "Empréstimos", description = "Endpoints para gerenciamento dos empréstimos")
public class EmprestimoController {

    private final EmprestimoService service;
    private final EmprestimoModelAssembler assembler;
    private final PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler;

    public EmprestimoController(
        EmprestimoService service,
        EmprestimoModelAssembler assembler,
        PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    @Operation(
        summary = "Listar empréstimos",
        description = "Retorna os empréstimos cadastrados de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Emprestimo> pagina = service.listar(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar empréstimo por ID",
        description = "Retorna um empréstimo a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empréstimo encontrado"),
        @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Emprestimo>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar empréstimo",
        description = "Cria um empréstimo para um usuário e um livro já cadastrados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empréstimo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<Emprestimo>> criar(@Valid @RequestBody EmprestimoRequest item) {
        Emprestimo criado = service.criar(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(criado));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar empréstimo",
        description = "Atualiza datas, status, usuário e livro de um empréstimo existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empréstimo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado")
    })
    public ResponseEntity<EntityModel<Emprestimo>> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody EmprestimoRequest item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> naoEncontrado(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir empréstimo",
        description = "Exclui um empréstimo pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Empréstimo excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!service.excluir(id)) {
            throw naoEncontrado(id);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar empréstimos por status",
        description = "Busca empréstimos pelo status informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> buscar(
        @Parameter(description = "Status do empréstimo (ATIVO, DEVOLVIDO ou ATRASADO)", example = "ATIVO")
        @RequestParam StatusEmprestimo status,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Emprestimo> pagina = service.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // Erro 404 com mensagem, tratado no ApiExceptionHandler.
    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Empréstimo com ID " + id + " não encontrado.");
    }
}

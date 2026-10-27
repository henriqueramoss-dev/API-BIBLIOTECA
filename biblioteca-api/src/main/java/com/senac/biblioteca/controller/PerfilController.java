package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.PerfilModelAssembler;
import com.senac.biblioteca.config.Paginas;
import com.senac.biblioteca.dto.PerfilRequest;
import com.senac.biblioteca.entity.Perfil;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.PerfilService;
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
 * Controller dos perfis. Expõe o CRUD, a listagem paginada e a busca
 * personalizada por telefone. A camada Service contém as regras de negócio.
 */
@RestController
@RequestMapping("/perfis")
@Tag(name = "Perfis", description = "Endpoints para gerenciamento dos perfis dos usuários")
public class PerfilController {

    // Service concentra as regras de negócio desta entidade.
    // Acesso às regras de negócio de Perfil.
    private final PerfilService service;
    // Assembler adiciona os links HATEOAS nas respostas.
    // Adiciona links HATEOAS ao recurso retornado.
    private final PerfilModelAssembler assembler;
    // Responsável por transformar Page em PagedModel.
    // Monta a resposta paginada no formato HATEOAS.
    private final PagedResourcesAssembler<Perfil> pagedResourcesAssembler;

    public PerfilController(
        PerfilService service,
        PerfilModelAssembler assembler,
        PagedResourcesAssembler<Perfil> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    // Listagem paginada: aceita page, size e sort.
    @GetMapping
    @Operation(
        summary = "Listar perfis",
        description = "Retorna os perfis cadastrados de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Perfil>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Perfil> pagina = Paginas.conferir(service.listar(pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // Busca individual pelo identificador.
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar perfil por ID",
        description = "Retorna um perfil a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
        @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Perfil>> buscarPorId(@Parameter(description = "ID do perfil", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.perfil(id));
    }

    // Cadastro de um novo recurso validado com Bean Validation.
    @PostMapping
    @Operation(
        summary = "Cadastrar perfil",
        description = "Cria o perfil de um usuário já cadastrado. Cada usuário pode ter apenas um perfil."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito de dados, como valor duplicado")
    })
    public ResponseEntity<EntityModel<Perfil>> criar(@Valid @RequestBody PerfilRequest item) {
        Perfil criado = service.criar(item);
        EntityModel<Perfil> model = assembler.toModel(criado);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    // Atualização de um recurso existente.
    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar perfil",
        description = "Atualiza telefone, endereço, cidade, estado, CEP, data de nascimento e usuário de um perfil existente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Perfil não encontrado"),
        @ApiResponse(responseCode = "409", description = "O usuário informado já possui outro perfil")
    })
    public ResponseEntity<EntityModel<Perfil>> atualizar(
        @Parameter(description = "ID do perfil", example = "1") @PathVariable Long id,
        @Valid @RequestBody PerfilRequest item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.perfil(id));
    }

    // Exclusão pelo identificador.
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir perfil",
        description = "Exclui um perfil pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Perfil excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do perfil", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw RecursoNaoEncontradoException.perfil(id);
        }

        return ResponseEntity.noContent().build();
    }

    // Consulta personalizada por telefone, mantendo a paginação.
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar perfis por telefone",
        description = "Busca perfis pelo telefone usando paginação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Perfil>>> buscar(
        @Parameter(description = "Parte do telefone a ser pesquisada", example = "1199")
        @RequestParam String telefone,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Perfil> pagina = Paginas.conferir(service.buscarPorTelefone(telefone, pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }
}

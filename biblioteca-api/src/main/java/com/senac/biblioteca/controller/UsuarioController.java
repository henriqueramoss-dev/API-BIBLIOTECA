package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.EmprestimoModelAssembler;
import com.senac.biblioteca.assembler.UsuarioModelAssembler;
import com.senac.biblioteca.config.Paginas;
import com.senac.biblioteca.dto.TrocaSenhaRequest;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.exception.RecursoNaoEncontradoException;
import com.senac.biblioteca.service.AutenticacaoService;
import com.senac.biblioteca.service.EmprestimoService;
import com.senac.biblioteca.service.UsuarioService;
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
 * Controller responsável pelos endpoints REST de usuários.
 * Recebe as requisições HTTP, chama a camada Service e devolve respostas
 * com códigos HTTP, paginação e links HATEOAS.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários da biblioteca")
public class UsuarioController {

    // Service contém as regras de negócio relacionadas aos usuários.
    private final UsuarioService service;
    // Assembler adiciona links HATEOAS às respostas.
    private final UsuarioModelAssembler assembler;
    // Converte Page<Usuario> para uma resposta paginada compatível com HATEOAS.
    private final PagedResourcesAssembler<Usuario> pagedResourcesAssembler;
    private final EmprestimoService emprestimoService;
    private final EmprestimoModelAssembler emprestimoAssembler;
    private final PagedResourcesAssembler<Emprestimo> emprestimoPagedAssembler;
    private final AutenticacaoService autenticacaoService;

    public UsuarioController(
        UsuarioService service,
        UsuarioModelAssembler assembler,
        PagedResourcesAssembler<Usuario> pagedResourcesAssembler,
        EmprestimoService emprestimoService,
        EmprestimoModelAssembler emprestimoAssembler,
        PagedResourcesAssembler<Emprestimo> emprestimoPagedAssembler,
        AutenticacaoService autenticacaoService
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.emprestimoService = emprestimoService;
        this.emprestimoAssembler = emprestimoAssembler;
        this.emprestimoPagedAssembler = emprestimoPagedAssembler;
        this.autenticacaoService = autenticacaoService;
    }

    // GET /usuarios: lista usuários usando page, size e sort.
    @GetMapping
    @Operation(
        summary = "Listar usuários",
        description = "Retorna os usuários cadastrados de forma paginada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listar(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Usuario> pagina = Paginas.conferir(service.listar(pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // GET /usuarios/{id}: busca um usuário específico pelo identificador.
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna um usuário a partir do seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<EntityModel<Usuario>> buscarPorId(@Parameter(description = "ID do usuário", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.usuario(id));
    }

    // POST /usuarios: cria um novo usuário após validar o JSON recebido.
    @PostMapping
    @Operation(
        summary = "Cadastrar usuário",
        description = "Cria um novo usuário na biblioteca. E-mail e CPF não podem se repetir. "
            + "O campo senha é opcional; quando enviado, só o hash dele é guardado e ele nunca é devolvido."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito de dados, como valor duplicado")
    })
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario item) {
        Usuario criado = service.criar(item);
        EntityModel<Usuario> model = assembler.toModel(criado);
        // 201 Created com o endereço do novo recurso no cabeçalho Location.
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    // PUT /usuarios/{id}: atualiza os dados de um usuário existente.
    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza nome, e-mail, CPF, matrícula e tipo de um usuário existente. A senha não muda por aqui (use PUT /usuarios/{id}/senha)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado para outro usuário")
    })
    public ResponseEntity<EntityModel<Usuario>> atualizar(
        @Parameter(description = "ID do usuário", example = "1") @PathVariable Long id,
        @Valid @RequestBody Usuario item
    ) {
        return service.atualizar(id, item)
            .map(assembler::toModel)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> RecursoNaoEncontradoException.usuario(id));
    }

    // PUT /usuarios/{id}/senha: troca a senha de um usuário.
    @PutMapping("/{id}/senha")
    @Operation(
        summary = "Trocar senha",
        description = "Troca a senha do usuário. Quando ele já tem senha, a senha atual é obrigatória e precisa estar correta. "
            + "A nova senha deve ter de 8 a 72 caracteres e ser diferente da atual. "
            + "Se o usuário ainda não tem senha, esta rota define a primeira."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nova senha inválida ou senha atual incorreta"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> trocarSenha(
        @Parameter(description = "ID do usuário", example = "1") @PathVariable Long id,
        @Valid @RequestBody TrocaSenhaRequest item
    ) {
        autenticacaoService.trocarSenha(id, item);
        return ResponseEntity.noContent().build();
    }

    // DELETE /usuarios/{id}: remove um usuário existente.
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir usuário",
        description = "Exclui um usuário pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "409", description = "Usuário possui empréstimos e não pode ser excluído")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do usuário", example = "1") @PathVariable Long id) {
        if (!service.excluir(id)) {
            throw RecursoNaoEncontradoException.usuario(id);
        }

        return ResponseEntity.noContent().build();
    }

    // GET /usuarios/buscar: consulta personalizada por nome com paginação.
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar usuários por nome",
        description = "Retorna usuários cujo nome contém o texto informado, usando paginação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de busca ausente ou inválido")
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> buscar(
        @Parameter(description = "Texto que deve estar contido no nome do usuário", example = "Henrique")
        @RequestParam String nome,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Usuario> pagina = Paginas.conferir(service.buscarPorNome(nome, pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    @GetMapping("/{id}/emprestimos")
    @Operation(
        summary = "Listar empréstimos do usuário",
        description = "Retorna, de forma paginada, os empréstimos feitos pelo usuário informado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID, paginação ou ordenação inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listarEmprestimos(
        @Parameter(description = "ID do usuário", example = "1") @PathVariable Long id,
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    ) {
        Page<Emprestimo> pagina = Paginas.conferir(emprestimoService.listarPorUsuario(id, pageable));
        return ResponseEntity.ok(emprestimoPagedAssembler.toModel(pagina, emprestimoAssembler));
    }
}

package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.EmprestimoModelAssembler;
import com.senac.biblioteca.dto.ReservaRequest;
import com.senac.biblioteca.entity.ChaveApi;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.exception.NaoAutenticadoException;
import com.senac.biblioteca.seguranca.ChaveApiFilter;
import com.senac.biblioteca.service.ReservaService;
import com.senac.biblioteca.config.Paginas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Reservas do cliente: escolher um livro no site e reservá-lo para retirar na biblioteca.
 * As rotas valem para o dono da chave de API usada (qualquer nível, inclusive LEITURA).
 */
@RestController
@RequestMapping("/reservas")
@Tag(name = "Reservas", description = "Reserva de livros para retirada na biblioteca (rotas do cliente)")
public class ReservaController {

    private final ReservaService service;
    private final EmprestimoModelAssembler assembler;
    private final PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler;

    public ReservaController(
        ReservaService service,
        EmprestimoModelAssembler assembler,
        PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler
    ) {
        this.service = service;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    // POST /reservas: reserva um livro para o dono da chave.
    @PostMapping
    @Operation(
        summary = "Reservar um livro",
        description = "Reserva o livro para o dono da chave de API (status RESERVADO). O livro fica separado até o "
            + "último dia de retirada (campo retirarAte, 3 dias por padrão). Cada pessoa pode ter até 3 reservas "
            + "aguardando retirada. Quando a pessoa retira o livro, a equipe muda o status para ATIVO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Livro reservado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou livro inexistente"),
        @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
        @ApiResponse(responseCode = "403", description = "A chave não pertence a um usuário"),
        @ApiResponse(responseCode = "409", description = "Livro indisponível ou limite de reservas atingido")
    })
    public ResponseEntity<EntityModel<Emprestimo>> reservar(@Valid @RequestBody ReservaRequest item, HttpServletRequest request) {
        Emprestimo reserva = service.reservar(chaveAtual(request), item);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(reserva));
    }

    // GET /reservas/minhas: reservas e empréstimos do dono da chave.
    @GetMapping("/minhas")
    @Operation(
        summary = "Minhas reservas e empréstimos",
        description = "Lista, de forma paginada, as reservas e os empréstimos do dono da chave de API, do mais novo para o mais antigo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido"),
        @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
        @ApiResponse(responseCode = "403", description = "A chave não pertence a um usuário"),
        @ApiResponse(responseCode = "404", description = "Página inexistente")
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> minhas(
        @ParameterObject @PageableDefault(size = 10, page = 0, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
        HttpServletRequest request
    ) {
        Page<Emprestimo> pagina = Paginas.conferir(service.minhas(chaveAtual(request), pageable));
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(pagina, assembler));
    }

    // DELETE /reservas/{id}: cancela uma reserva ainda não retirada.
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancelar reserva",
        description = "Cancela uma reserva que ainda não foi retirada. Cada pessoa cancela só as próprias reservas "
            + "(a de outra pessoa responde 404); a chave ADMIN cancela qualquer uma."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reserva cancelada"),
        @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
        @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
        @ApiResponse(responseCode = "409", description = "A reserva já foi retirada e não pode ser cancelada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato inválido")
    })
    public ResponseEntity<Void> cancelar(
        @Parameter(description = "ID da reserva", example = "1") @PathVariable Long id,
        HttpServletRequest request
    ) {
        service.cancelar(chaveAtual(request), id);
        return ResponseEntity.noContent().build();
    }

    // O ChaveApiFilter já garante a chave nestas rotas; a conferência aqui é só uma proteção extra.
    private ChaveApi chaveAtual(HttpServletRequest request) {
        return ChaveApiFilter.chaveDa(request).orElseThrow(NaoAutenticadoException::semChave);
    }
}

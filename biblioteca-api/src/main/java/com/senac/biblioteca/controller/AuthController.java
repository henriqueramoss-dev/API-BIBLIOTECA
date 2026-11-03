package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.UsuarioModelAssembler;
import com.senac.biblioteca.dto.LoginRequest;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de login. Confere e-mail e senha e devolve o usuário (sem a senha).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login por e-mail e senha")
public class AuthController {

    private final AutenticacaoService service;
    private final UsuarioModelAssembler assembler;

    public AuthController(AutenticacaoService service, UsuarioModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    // POST /auth/login: confere as credenciais.
    @PostMapping("/login")
    @Operation(
        summary = "Entrar",
        description = "Confere e-mail e senha e devolve os dados do usuário. "
            + "Ainda não gera token: as demais rotas da API continuam abertas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credenciais corretas"),
        @ApiResponse(responseCode = "400", description = "E-mail ou senha não informados"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos")
    })
    public ResponseEntity<EntityModel<Usuario>> entrar(@Valid @RequestBody LoginRequest item) {
        Usuario usuario = service.autenticar(item);
        return ResponseEntity.ok(assembler.toModel(usuario));
    }
}

package com.senac.biblioteca.controller;

import com.senac.biblioteca.assembler.UsuarioModelAssembler;
import com.senac.biblioteca.dto.ChaveApiCriada;
import com.senac.biblioteca.dto.ChaveApiRequest;
import com.senac.biblioteca.dto.LoginRequest;
import com.senac.biblioteca.dto.LoginResposta;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.service.AutenticacaoService;
import com.senac.biblioteca.service.ChaveApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de login. Confere e-mail e senha e devolve o usuário (sem a senha) e uma chave de API nova.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login por e-mail e senha")
public class AuthController {

    private final AutenticacaoService service;
    private final UsuarioModelAssembler assembler;
    private final ChaveApiService chaves;

    public AuthController(AutenticacaoService service, UsuarioModelAssembler assembler, ChaveApiService chaves) {
        this.service = service;
        this.assembler = assembler;
        this.chaves = chaves;
    }

    // POST /auth/login: confere as credenciais.
    @PostMapping("/login")
    @Operation(
        summary = "Entrar",
        description = "Confere e-mail e senha e devolve os dados do usuário junto com uma **chave de API** nova "
            + "(campo `chave`; ADMIN para funcionário/dono, LEITURA para os demais). Copie a chave, clique em "
            + "**Authorize** e cole. Ela só aparece nesta resposta; um novo login gera outra."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credenciais corretas: devolve o usuário e a chave"),
        @ApiResponse(responseCode = "400", description = "E-mail ou senha não informados"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos")
    })
    public ResponseEntity<LoginResposta> entrar(@Valid @RequestBody LoginRequest item) {
        Usuario usuario = service.autenticar(item);
        ChaveApiRequest pedido = new ChaveApiRequest();
        pedido.setEmail(item.getEmail());
        pedido.setSenha(item.getSenha());
        pedido.setNome("Login de " + usuario.getNome());
        pedido.setNivel(NivelAcesso.maximoPara(usuario.getTipo()));
        ChaveApiCriada criada = chaves.gerar(pedido);
        LoginResposta resposta = new LoginResposta(usuario, criada.getChave(), criada.getNivel());
        resposta.add(assembler.toModel(usuario).getLinks());
        return ResponseEntity.ok(resposta);
    }
}

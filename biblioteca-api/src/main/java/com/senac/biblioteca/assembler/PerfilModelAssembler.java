package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.PerfilController;
import com.senac.biblioteca.entity.Perfil;
import com.senac.biblioteca.controller.UsuarioController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Perfil em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class PerfilModelAssembler
        implements RepresentationModelAssembler<Perfil, EntityModel<Perfil>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * usuario -> navega para o usuário dono do perfil
     * perfis -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Perfil> toModel(Perfil item) {
        return EntityModel.of(
            item,
            linkTo(PerfilController.class).slash(item.getId()).withSelfRel(),
            linkTo(PerfilController.class).slash(item.getId()).withRel("update"),
            linkTo(PerfilController.class).slash(item.getId()).withRel("delete"),
            linkTo(PerfilController.class).withRel("perfis"),
            linkTo(UsuarioController.class).slash(item.getUsuario().getId()).withRel("usuario")
        );
    }
}

package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.CarteirinhaController;
import com.senac.biblioteca.controller.UsuarioController;
import com.senac.biblioteca.entity.Carteirinha;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Carteirinha em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class CarteirinhaModelAssembler
        implements RepresentationModelAssembler<Carteirinha, EntityModel<Carteirinha>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * usuario -> navega para o usuário dono da carteirinha
     * carteirinhas -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Carteirinha> toModel(Carteirinha item) {
        return EntityModel.of(
            item,
            linkTo(CarteirinhaController.class).slash(item.getId()).withSelfRel(),
            linkTo(CarteirinhaController.class).slash(item.getId()).withRel("update"),
            linkTo(CarteirinhaController.class).slash(item.getId()).withRel("delete"),
            linkTo(CarteirinhaController.class).withRel("carteirinhas"),
            linkTo(UsuarioController.class).slash(item.getUsuario().getId()).withRel("usuario")
        );
    }
}

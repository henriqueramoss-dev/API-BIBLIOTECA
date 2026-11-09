package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.ChaveApiController;
import com.senac.biblioteca.controller.UsuarioController;
import com.senac.biblioteca.dto.ChaveApiCriada;
import com.senac.biblioteca.entity.ChaveApi;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Transforma uma ChaveApi em um EntityModel HATEOAS.
 */
@Component
public class ChaveApiModelAssembler
        implements RepresentationModelAssembler<ChaveApi, EntityModel<ChaveApi>> {

    /**
     * self    -> consulta a própria chave
     * revogar -> rota para revogar a chave (DELETE), só enquanto ela está ativa
     * chaves-api -> listagem das chaves
     * usuario -> dono da chave, quando houver
     */
    @Override
    public EntityModel<ChaveApi> toModel(ChaveApi item) {
        EntityModel<ChaveApi> model = EntityModel.of(
            item,
            linkTo(ChaveApiController.class).slash(item.getId()).withSelfRel(),
            linkTo(ChaveApiController.class).withRel("chaves-api")
        );

        if (item.isAtiva()) {
            model.add(linkTo(ChaveApiController.class).slash(item.getId()).withRel("revogar"));
        }
        if (item.getUsuarioId() != null) {
            model.add(linkTo(UsuarioController.class).slash(item.getUsuarioId()).withRel("usuario"));
        }

        return model;
    }

    /** Links da resposta de uma chave recém-gerada. */
    public EntityModel<ChaveApiCriada> toModel(ChaveApiCriada item) {
        return EntityModel.of(
            item,
            linkTo(ChaveApiController.class).slash(item.getId()).withSelfRel(),
            linkTo(ChaveApiController.class).slash(item.getId()).withRel("revogar"),
            linkTo(ChaveApiController.class).withRel("chaves-api"),
            linkTo(UsuarioController.class).slash(item.getUsuarioId()).withRel("usuario")
        );
    }
}

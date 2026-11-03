package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.AutorController;
import com.senac.biblioteca.entity.Autor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Autor em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class AutorModelAssembler
        implements RepresentationModelAssembler<Autor, EntityModel<Autor>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * livros -> navega para os livros deste autor
     * autores -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Autor> toModel(Autor item) {
        return EntityModel.of(
            item,
            linkTo(AutorController.class).slash(item.getId()).withSelfRel(),
            linkTo(AutorController.class).slash(item.getId()).withRel("update"),
            linkTo(AutorController.class).slash(item.getId()).withRel("delete"),
            linkTo(AutorController.class).slash(item.getId()).slash("livros").withRel("livros"),
            linkTo(AutorController.class).withRel("autores")
        );
    }
}

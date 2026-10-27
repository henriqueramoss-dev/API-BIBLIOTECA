package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.CategoriaController;
import com.senac.biblioteca.entity.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Categoria em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * livros -> navega para os livros desta categoria
     * categorias -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Categoria> toModel(Categoria item) {
        return EntityModel.of(
            item,
            linkTo(CategoriaController.class).slash(item.getId()).withSelfRel(),
            linkTo(CategoriaController.class).slash(item.getId()).withRel("update"),
            linkTo(CategoriaController.class).slash(item.getId()).withRel("delete"),
            linkTo(CategoriaController.class).slash(item.getId()).slash("livros").withRel("livros"),
            linkTo(CategoriaController.class).withRel("categorias")
        );
    }
}

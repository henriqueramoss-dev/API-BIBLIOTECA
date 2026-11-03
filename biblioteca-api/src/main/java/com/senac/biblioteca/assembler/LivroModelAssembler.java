package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.LivroController;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.entity.Livro;
import com.senac.biblioteca.controller.AutorController;
import com.senac.biblioteca.controller.CategoriaController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Livro em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class LivroModelAssembler
        implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * emprestimos -> navega para o histórico de empréstimos deste livro
     * categoria -> navega para a categoria do livro
     * autores -> navega para cada autor do livro
     * livros -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Livro> toModel(Livro item) {
        EntityModel<Livro> model = EntityModel.of(
            item,
            linkTo(LivroController.class).slash(item.getId()).withSelfRel(),
            linkTo(LivroController.class).slash(item.getId()).withRel("update"),
            linkTo(LivroController.class).slash(item.getId()).withRel("delete"),
            linkTo(LivroController.class).slash(item.getId()).slash("emprestimos").withRel("emprestimos"),
            linkTo(LivroController.class).withRel("livros")
        );

        if (item.getCategoria() != null) {
            model.add(linkTo(CategoriaController.class).slash(item.getCategoria().getId()).withRel("categoria"));
        }

        for (Autor autor : item.getAutores()) {
            model.add(linkTo(AutorController.class).slash(autor.getId()).withRel("autores"));
        }

        return model;
    }
}

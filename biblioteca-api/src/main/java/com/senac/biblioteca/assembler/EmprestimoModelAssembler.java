package com.senac.biblioteca.assembler;

import com.senac.biblioteca.controller.EmprestimoController;
import com.senac.biblioteca.entity.Emprestimo;
import com.senac.biblioteca.controller.LivroController;
import com.senac.biblioteca.controller.UsuarioController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Responsável por transformar um objeto Emprestimo em um EntityModel HATEOAS.
 *
 * O EntityModel mantém os dados da entidade e acrescenta links que ajudam
 * o cliente da API a navegar entre as operações disponíveis.
 */
@Component
public class EmprestimoModelAssembler
        implements RepresentationModelAssembler<Emprestimo, EntityModel<Emprestimo>> {

    /**
     * Adiciona os links HATEOAS à resposta da entidade.
     *
     * self   -> consulta o próprio recurso
     * update -> indica a rota utilizada para atualização
     * delete -> indica a rota utilizada para exclusão
     * usuario -> navega para o usuário que pegou o livro
     * livro -> navega para o livro emprestado
     * emprestimos -> retorna para a listagem paginada do recurso
     */
    @Override
    public EntityModel<Emprestimo> toModel(Emprestimo item) {
        return EntityModel.of(
            item,
            linkTo(EmprestimoController.class).slash(item.getId()).withSelfRel(),
            linkTo(EmprestimoController.class).slash(item.getId()).withRel("update"),
            linkTo(EmprestimoController.class).slash(item.getId()).withRel("delete"),
            linkTo(EmprestimoController.class).withRel("emprestimos"),
            linkTo(UsuarioController.class).slash(item.getUsuario().getId()).withRel("usuario"),
            linkTo(LivroController.class).slash(item.getLivro().getId()).withRel("livro")
        );
    }
}

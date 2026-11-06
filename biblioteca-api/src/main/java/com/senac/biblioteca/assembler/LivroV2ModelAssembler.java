package com.senac.biblioteca.assembler;

import com.senac.biblioteca.dto.LivroV2;
import com.senac.biblioteca.entity.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/**
 * Monta o livro no formato da versão 2 (LivroV2), com os mesmos links HATEOAS da versão 1.
 */
@Component
public class LivroV2ModelAssembler implements RepresentationModelAssembler<Livro, EntityModel<LivroV2>> {

    private final LivroModelAssembler versao1;

    public LivroV2ModelAssembler(LivroModelAssembler versao1) {
        this.versao1 = versao1;
    }

    @Override
    public EntityModel<LivroV2> toModel(Livro item) {
        // Reaproveita os links da versão 1 (self, update, delete, categoria, autores...).
        return EntityModel.of(new LivroV2(item), versao1.toModel(item).getLinks());
    }
}

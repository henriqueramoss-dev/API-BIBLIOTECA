package com.senac.biblioteca.enums;

/**
 * Níveis de acesso de uma chave de API, do menor para o maior.
 * Cada nível inclui as permissões dos anteriores.
 */
public enum NivelAcesso {

    // Consulta os dados pessoais (usuários, carteirinhas e empréstimos).
    LEITURA,

    // LEITURA + cadastra e altera registros (POST e PUT).
    ESCRITA,

    // ESCRITA + exclui registros (DELETE) e gerencia as chaves de todos os usuários.
    ADMIN;

    /** Diz se este nível atende ao nível exigido por uma rota. */
    public boolean permite(NivelAcesso exigido) {
        return this.ordinal() >= exigido.ordinal();
    }

    /**
     * Maior nível que um tipo de usuário pode pedir para as próprias chaves.
     * Só funcionários da biblioteca alteram o acervo e os empréstimos.
     */
    public static NivelAcesso maximoPara(TipoUsuario tipo) {
        return tipo == TipoUsuario.FUNCIONARIO ? ADMIN : LEITURA;
    }
}

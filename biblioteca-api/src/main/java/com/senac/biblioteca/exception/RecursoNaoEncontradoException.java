package com.senac.biblioteca.exception;

/**
 * Lançada quando o registro pedido na URL não existe
 * (por exemplo, GET /livros/999 com um ID que não está cadastrado).
 *
 * É tratada no ApiExceptionHandler e retorna HTTP 404 - Not Found
 * com uma mensagem dizendo qual registro não foi encontrado.
 *
 * As mensagens de cada recurso ficam todas aqui, nos métodos estáticos abaixo.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException autor(Long id) {
        return new RecursoNaoEncontradoException("Autor com ID " + id + " não encontrado.");
    }

    public static RecursoNaoEncontradoException categoria(Long id) {
        return new RecursoNaoEncontradoException("Categoria com ID " + id + " não encontrada.");
    }

    public static RecursoNaoEncontradoException emprestimo(Long id) {
        return new RecursoNaoEncontradoException("Empréstimo com ID " + id + " não encontrado.");
    }

    public static RecursoNaoEncontradoException livro(Long id) {
        return new RecursoNaoEncontradoException("Livro com ID " + id + " não encontrado.");
    }

    public static RecursoNaoEncontradoException perfil(Long id) {
        return new RecursoNaoEncontradoException("Perfil com ID " + id + " não encontrado.");
    }

    public static RecursoNaoEncontradoException usuario(Long id) {
        return new RecursoNaoEncontradoException("Usuário com ID " + id + " não encontrado.");
    }

    public static RecursoNaoEncontradoException pagina(long numero, long totalPaginas) {
        return new RecursoNaoEncontradoException(
            "A página " + numero + " não existe. Esta listagem tem " + totalPaginas
                + (totalPaginas == 1 ? " página" : " páginas") + " (de 0 a " + (totalPaginas - 1) + ")."
        );
    }
}

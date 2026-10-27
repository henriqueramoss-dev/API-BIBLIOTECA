package com.senac.biblioteca.exception;

import com.senac.biblioteca.dto.ErroResposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado das exceções da API.
 *
 * O @RestControllerAdvice permite tratar erros lançados pelos controllers
 * em um único lugar, evitando respostas genéricas ou códigos HTTP inadequados.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Trata erros de Bean Validation, como campos obrigatórios vazios,
     * e-mail inválido ou valores fora das regras definidas nas entidades.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();

        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }

        ErroResposta resposta = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "Dados inválidos",
            "Um ou mais campos possuem valores inválidos.",
            campos
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata violações de integridade do banco.
     *
     * Exemplos:
     * - e-mail de usuário duplicado;
     * - ISBN de livro duplicado;
     * - tentativa de criar um segundo perfil para o mesmo usuário;
     * - exclusão de um registro que ainda está sendo utilizado por outro.
     *
     * Retorna HTTP 409 - Conflict, pois a requisição é válida,
     * mas entra em conflito com o estado atual dos dados.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.CONFLICT,
            "Conflito de dados",
            "A operação não pôde ser concluída porque existe um dado duplicado ou um relacionamento que impede a alteração."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    /**
     * Trata JSON inválido ou valores incompatíveis com o tipo esperado.
     *
     * Exemplo: informar um status diferente de ATIVO, DEVOLVIDO ou ATRASADO.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "JSON inválido",
            "O corpo da requisição está malformado ou contém um valor incompatível com o tipo esperado."
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata parâmetros de URL com tipo inválido.
     *
     * Exemplo: informar um texto onde a API espera um número ou um enum.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "Parâmetro inválido",
            "O parâmetro '" + ex.getName() + "' possui um valor inválido."
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata IDs de registros relacionados que não existem.
     *
     * Exemplo: cadastrar um livro com uma categoria ou autor inexistente.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(RecursoRelacionadoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRelacionamentoInexistente(RecursoRelacionadoNaoEncontradoException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "Relacionamento inválido",
            ex.getMessage()
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata registros que não existem.
     *
     * Exemplo: buscar, atualizar ou excluir /livros/999 quando o ID 999 não está cadastrado.
     *
     * Retorna HTTP 404 - Not Found.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.NOT_FOUND,
            "Não encontrado",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    /**
     * Trata parâmetros de paginação fora dos limites.
     *
     * Exemplo: GET /livros?size=100000 (o máximo é 100 por página) ou page=-1.
     *
     * Retorna HTTP 400 - Bad Request, explicando o limite.
     */
    @ExceptionHandler(PaginacaoInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarPaginacaoInvalida(PaginacaoInvalidaException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "Paginação inválida",
            ex.getMessage()
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata endereços que não existem na API.
     *
     * Exemplo: GET /livross (com erro de digitação).
     *
     * Retorna HTTP 404 - Not Found, no mesmo formato das outras respostas de erro.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarEnderecoInexistente(NoResourceFoundException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.NOT_FOUND,
            "Endereço não encontrado",
            "O endereço '/" + ex.getResourcePath() + "' não existe nesta API."
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    /**
     * Trata métodos HTTP que a rota não aceita.
     *
     * Exemplo: PATCH /livros/1 (a rota aceita apenas GET, PUT e DELETE).
     *
     * Retorna HTTP 405 - Method Not Allowed, informando os métodos aceitos.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        String aceitos = ex.getSupportedHttpMethods() == null ? "" :
            " Métodos aceitos: " + String.join(", ", ex.getSupportedHttpMethods().stream().map(Object::toString).toList()) + ".";
        ErroResposta resposta = criarResposta(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Método não permitido",
            "O método " + ex.getMethod() + " não é aceito neste endereço." + aceitos
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(resposta);
    }

    /**
     * Trata parâmetros obrigatórios ausentes na URL.
     *
     * Exemplo: chamar /livros/buscar sem informar o parâmetro titulo.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroAusente(MissingServletRequestParameterException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "Parâmetro obrigatório ausente",
            "O parâmetro '" + ex.getParameterName() + "' é obrigatório."
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata ordenação por um campo que não existe na entidade.
     *
     * Exemplo: /livros?sort=campoInexistente.
     *
     * Retorna HTTP 400 - Bad Request.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErroResposta> tratarOrdenacaoInvalida(PropertyReferenceException ex) {
        ErroResposta resposta = criarResposta(
            HttpStatus.BAD_REQUEST,
            "Ordenação inválida",
            "O campo '" + ex.getPropertyName() + "' não pode ser usado para ordenação."
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata qualquer erro inesperado, mantendo o mesmo formato de resposta.
     *
     * Retorna HTTP 500 - Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex) throws Exception {
        // Exceções que o Spring já sabe converter (404 de rota, 405, 415...)
        // continuam sendo tratadas por ele.
        if (ex instanceof ErrorResponse) {
            throw ex;
        }

        log.error("Erro inesperado na API", ex);

        ErroResposta resposta = criarResposta(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno",
            "Ocorreu um erro inesperado ao processar a requisição."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }

    /**
     * Monta um formato padrão para as respostas de erro da API.
     */
    private ErroResposta criarResposta(HttpStatus status, String erro, String mensagem) {
        return new ErroResposta(status.value(), erro, mensagem, null);
    }
}

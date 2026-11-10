package com.senac.biblioteca.config;

import com.senac.biblioteca.dto.CarteirinhaRequest;
import com.senac.biblioteca.dto.EmprestimoRequest;
import com.senac.biblioteca.dto.LivroRequest;
import com.senac.biblioteca.entity.Autor;
import com.senac.biblioteca.entity.Categoria;
import com.senac.biblioteca.entity.Usuario;
import com.senac.biblioteca.enums.NivelAcesso;
import com.senac.biblioteca.enums.StatusCarteirinha;
import com.senac.biblioteca.enums.StatusEmprestimo;
import com.senac.biblioteca.enums.TipoUsuario;
import com.senac.biblioteca.repository.LivroRepository;
import com.senac.biblioteca.service.AutorService;
import com.senac.biblioteca.service.CarteirinhaService;
import com.senac.biblioteca.service.CategoriaService;
import com.senac.biblioteca.service.EmprestimoService;
import com.senac.biblioteca.service.LivroService;
import com.senac.biblioteca.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Cria um acervo de exemplo ao iniciar a API (categorias, autores, livros, clientes, carteirinhas e
 * empréstimos), para o Swagger e o site não começarem vazios. Como o banco é em memória, os dados
 * voltam a cada início.
 *
 * Liga e desliga em biblioteca.dados-exemplo (variável BIBLIOTECA_DADOS_EXEMPLO). Só cria se ainda não
 * houver nenhum livro. Os valores únicos (ISBN, CPF, e-mail, número da carteirinha) são diferentes dos
 * exemplos mostrados no Swagger, para o "Try it out" cadastrar na primeira tentativa.
 *
 * Contas de clientes de exemplo: e-mail abaixo e senha {@value #SENHA_DOS_CLIENTES} (chave de LEITURA, só reservas).
 * Nenhuma conta de funcionário é criada aqui: a do dono vem de BIBLIOTECA_DONO_EMAIL e BIBLIOTECA_DONO_SENHA.
 */
@Component
public class DadosDeExemplo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDeExemplo.class);

    /** Senha dos clientes de exemplo. */
    public static final String SENHA_DOS_CLIENTES = "Cliente12345";

    /** E-mail do primeiro cliente de exemplo, citado no texto do Swagger. */
    public static final String EMAIL_DE_EXEMPLO = "beatriz@exemplo.com";

    private final boolean ligado;
    private final LivroRepository livroRepository;
    private final CategoriaService categorias;
    private final AutorService autores;
    private final LivroService livros;
    private final UsuarioService usuarios;
    private final CarteirinhaService carteirinhas;
    private final EmprestimoService emprestimos;

    public DadosDeExemplo(
        @Value("${biblioteca.dados-exemplo:false}") boolean ligado,
        LivroRepository livroRepository,
        CategoriaService categorias,
        AutorService autores,
        LivroService livros,
        UsuarioService usuarios,
        CarteirinhaService carteirinhas,
        EmprestimoService emprestimos
    ) {
        this.ligado = ligado;
        this.livroRepository = livroRepository;
        this.categorias = categorias;
        this.autores = autores;
        this.livros = livros;
        this.usuarios = usuarios;
        this.carteirinhas = carteirinhas;
        this.emprestimos = emprestimos;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!ligado || livroRepository.count() > 0) {
            return;
        }
        try {
            criar();
            log.info("Dados de exemplo criados (acervo, clientes, carteirinhas e empréstimos). "
                + "Cliente de exemplo: {} / {}. Para desligar: BIBLIOTECA_DADOS_EXEMPLO=false.", EMAIL_DE_EXEMPLO, SENHA_DOS_CLIENTES);
        } catch (RuntimeException e) {
            // Os dados de exemplo nunca impedem a API de subir.
            log.warn("Não foi possível criar os dados de exemplo: {}", e.getMessage());
        }
    }

    private void criar() {
        LocalDate hoje = LocalDate.now();

        long literatura = categoria("Literatura Brasileira", "LITBR", "Clássicos e contemporâneos brasileiros", "Estante A", "Jovens e adultos");
        long ficcao = categoria("Ficção Científica", "FICCIE", "Futuros, robôs e viagens espaciais", "Estante B", "Jovens e adultos");
        long tecnologia = categoria("Tecnologia", "TECNO", "Programação e engenharia de software", "Estante C", "Adultos");
        long infantil = categoria("Infantil", "INFANT", "Histórias para os pequenos leitores", "Estante D", "Crianças");
        long historia = categoria("História", "HISTO", "História do Brasil e do mundo", "Estante E", "Adultos");

        long machado = autor("Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21), "Romance realista", "Fundador da Academia Brasileira de Letras.");
        long clarice = autor("Clarice Lispector", "Brasileira", LocalDate.of(1920, 12, 10), "Romance intimista", "Uma das maiores escritoras do século XX.");
        long lobato = autor("Monteiro Lobato", "Brasileira", LocalDate.of(1882, 4, 18), "Literatura infantil", "Criador do Sítio do Picapau Amarelo.");
        long asimov = autor("Isaac Asimov", "Americana", LocalDate.of(1920, 1, 2), "Ficção científica", "Autor de Fundação e das Três Leis da Robótica.");
        long martin = autor("Robert C. Martin", "Americana", LocalDate.of(1952, 12, 5), "Engenharia de software", "Conhecido como Uncle Bob.");
        long sergio = autor("Sérgio Buarque de Holanda", "Brasileira", LocalDate.of(1902, 7, 11), "Ensaio histórico", "Historiador e crítico literário.");

        // O livro 1 fica sempre na estante: é o do exemplo de POST /reservas no Swagger.
        livro("Memórias Póstumas de Brás Cubas", "9788572328357", 1881, "Penguin-Companhia", 368, literatura, machado);
        livro("Dom Casmurro (edição de bolso)", "9788525406958", 1899, "Principis", 256, literatura, machado);
        long l3 = livro("A Hora da Estrela", "9788532508126", 1977, "Rocco", 88, literatura, clarice);
        livro("Laços de Família", "9788532508133", 1960, "Rocco", 152, literatura, clarice);
        livro("O Alienista", "9788594318602", 1882, "Principis", 80, literatura, machado);
        livro("Reinações de Narizinho", "9788525059550", 1931, "Globinho", 204, infantil, lobato);
        long l7 = livro("Fundação", "9788576573302", 1951, "Aleph", 264, ficcao, asimov);
        livro("Eu, Robô", "9788576572008", 1950, "Aleph", 320, ficcao, asimov);
        long l9 = livro("Código Limpo", "9788576082675", 2008, "Alta Books", 425, tecnologia, martin);
        long l10 = livro("Arquitetura Limpa", "9788550804606", 2017, "Alta Books", 432, tecnologia, martin);
        livro("Raízes do Brasil", "9788535918564", 1936, "Companhia das Letras", 220, historia, sergio);
        livro("Clean Code na prática", "9788575228074", 2020, "Novatec", 190, tecnologia, martin);

        long beatriz = usuario("Beatriz Ferreira", EMAIL_DE_EXEMPLO, "16899535009", TipoUsuario.ALUNO, "Campinas");
        long carlos = usuario("Carlos Menezes", "carlos@exemplo.com", "74521108032", TipoUsuario.PROFESSOR, "São Paulo");
        usuario("Daniela Rocha", "daniela@exemplo.com", "32013406002", TipoUsuario.VISITANTE, "Santos");
        long eduardo = usuario("Eduardo Lima", "eduardo@exemplo.com", "50726419028", TipoUsuario.ALUNO, "Sorocaba");
        long fernanda = usuario("Fernanda Alves", "fernanda@exemplo.com", "21436587026", TipoUsuario.ALUNO, "Jundiaí");
        // O usuário 6 fica sem carteirinha: é o do exemplo de POST /carteirinhas no Swagger.
        usuario("Gustavo Pires", "gustavo@exemplo.com", "98675432038", TipoUsuario.VISITANTE, "Osasco");

        carteirinha("BIB-0001", beatriz, hoje.minusMonths(6), hoje.plusMonths(6), StatusCarteirinha.ATIVA, 3);
        carteirinha("BIB-0002", carlos, hoje.minusMonths(12), hoje.plusMonths(12), StatusCarteirinha.ATIVA, 5);
        carteirinha("BIB-0004", eduardo, hoje.minusMonths(20), hoje.minusMonths(2), StatusCarteirinha.SUSPENSA, 2);
        carteirinha("BIB-0005", fernanda, hoje.minusMonths(3), hoje.plusMonths(9), StatusCarteirinha.ATIVA, 3);

        emprestimo(beatriz, l3, hoje.minusDays(5), hoje.plusDays(9), null, StatusEmprestimo.ATIVO, "Retirado no balcão.");
        emprestimo(carlos, l9, hoje.minusDays(30), hoje.minusDays(16), hoje.minusDays(18), StatusEmprestimo.DEVOLVIDO, "Devolvido antes do prazo.");
        emprestimo(eduardo, l7, hoje.minusDays(25), hoje.minusDays(11), null, StatusEmprestimo.ATRASADO, "Prazo vencido: avisar por e-mail.");
        emprestimo(fernanda, l10, hoje.minusDays(2), hoje.plusDays(12), null, StatusEmprestimo.ATIVO, null);
    }

    private long categoria(String nome, String codigo, String descricao, String localizacao, String publico) {
        Categoria c = new Categoria();
        c.setNome(nome);
        c.setCodigo(codigo);
        c.setDescricao(descricao);
        c.setLocalizacao(localizacao);
        c.setPublicoAlvo(publico);
        return categorias.criar(c).getId();
    }

    private long autor(String nome, String nacionalidade, LocalDate nascimento, String genero, String biografia) {
        Autor a = new Autor();
        a.setNome(nome);
        a.setNacionalidade(nacionalidade);
        a.setDataNascimento(nascimento);
        a.setGeneroLiterario(genero);
        a.setBiografia(biografia);
        return autores.criar(a).getId();
    }

    private long livro(String titulo, String isbn, int ano, String editora, int paginas, long categoriaId, long autorId) {
        LivroRequest r = new LivroRequest();
        r.setTitulo(titulo);
        r.setIsbn(isbn);
        r.setAnoPublicacao(ano);
        r.setEditora(editora);
        r.setNumeroPaginas(paginas);
        r.setCategoriaId(categoriaId);
        r.setAutoresIds(Set.of(autorId));
        return livros.criar(r).getId();
    }

    private long usuario(String nome, String email, String cpf, TipoUsuario tipo, String cidade) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setCpf(cpf);
        u.setTipo(tipo);
        u.setTelefone("11" + cpf.substring(0, 9));
        u.setEndereco("Rua das Flores, " + (10 + Integer.parseInt(cpf.substring(0, 2)) % 90));
        u.setCidade(cidade);
        u.setEstado("SP");
        u.setCep("13010000");
        u.setDataNascimento(LocalDate.of(1995, 3, 14));
        u.setSenha(SENHA_DOS_CLIENTES);
        return usuarios.criar(u, NivelAcesso.ADMIN).getId();
    }

    private void carteirinha(String numero, long usuarioId, LocalDate emissao, LocalDate validade, StatusCarteirinha status, int limite) {
        CarteirinhaRequest r = new CarteirinhaRequest();
        r.setNumero(numero);
        r.setDataEmissao(emissao);
        r.setDataValidade(validade);
        r.setStatus(status);
        r.setLimiteEmprestimos(limite);
        r.setUsuarioId(usuarioId);
        carteirinhas.criar(r);
    }

    private void emprestimo(long usuarioId, long livroId, LocalDate saida, LocalDate prevista, LocalDate devolucao,
                            StatusEmprestimo status, String observacao) {
        EmprestimoRequest r = new EmprestimoRequest();
        r.setUsuarioId(usuarioId);
        r.setLivroId(livroId);
        r.setDataEmprestimo(saida);
        r.setDataPrevistaDevolucao(prevista);
        r.setDataDevolucao(devolucao);
        r.setStatus(status);
        r.setObservacao(observacao);
        emprestimos.criar(r);
    }

    /** Usado só para documentar: as contas de cliente de exemplo. */
    public static List<String> contasDeExemplo() {
        return List.of(EMAIL_DE_EXEMPLO);
    }
}

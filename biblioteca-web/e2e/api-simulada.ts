import type { Page, Route } from '@playwright/test';

/**
 * API simulada para os testes: responde no mesmo formato da API real (HAL), sem sair do computador.
 * Contas de teste:
 *   dono@teste.com / Senha12345    -> FUNCIONARIO (painel)
 *   cliente@teste.com / Senha12345 -> ALUNO (catálogo e reservas)
 */
export const API = process.env.E2E_API_URL ?? 'https://api-biblioteca-xgsa.onrender.com';

const categorias = [
  { id: 1, nome: 'Literatura Brasileira', codigo: 'LITBR', descricao: 'Clássicos', localizacao: 'Estante A', publicoAlvo: 'Todos' },
  { id: 2, nome: 'Tecnologia', codigo: 'TEC', descricao: 'Programação', localizacao: 'Estante B', publicoAlvo: 'Todos' },
];
const autor = { id: 1, nome: 'Machado de Assis', nacionalidade: 'Brasileira', dataNascimento: '1839-06-21', generoLiterario: 'Romance', biografia: 'Escritor.' };
const livros = [
  { id: 1, titulo: 'Dom Casmurro', isbn: '9788572328357', editora: 'Penguin', anoPublicacao: 1899, numeroPaginas: 256, situacao: 'DISPONIVEL', categoria: categorias[0], autores: [autor] },
  { id: 2, titulo: 'Clean Code na prática', isbn: '9788576082675', editora: 'Alta Books', anoPublicacao: 2009, numeroPaginas: 420, situacao: 'DISPONIVEL', categoria: categorias[1], autores: [autor] },
  { id: 3, titulo: 'Arquitetura Limpa', isbn: '9788550804606', editora: 'Alta Books', anoPublicacao: 2019, numeroPaginas: 432, situacao: 'INDISPONIVEL', categoria: categorias[1], autores: [autor] },
];

const pagina = (chave: string, itens: unknown[], tamanho = 10) => ({
  _embedded: itens.length ? { [chave]: itens } : {},
  page: { size: tamanho, totalElements: itens.length, totalPages: itens.length ? 1 : 0, number: 0 },
});

const json = (route: Route, status: number, corpo?: unknown) =>
  route.fulfill({ status, contentType: 'application/hal+json', body: corpo === undefined ? '' : JSON.stringify(corpo) });

/** Liga a API simulada na página. Guarda o que o site enviou em `chamadas`, para os testes conferirem. */
export async function simularApi(page: Page) {
  const chamadas: { metodo: string; caminho: string; chave: string | undefined; corpo: any }[] = [];

  await page.route(`${API}/**`, async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const caminho = url.pathname;
    const metodo = req.method();
    let corpo: any;
    try { corpo = req.postDataJSON(); } catch { corpo = undefined; }
    chamadas.push({ metodo, caminho, chave: req.headers()['x-api-key'], corpo });

    if (metodo === 'OPTIONS') return route.fulfill({ status: 204, headers: { 'access-control-allow-origin': '*', 'access-control-allow-headers': '*', 'access-control-allow-methods': '*' } });

    if (caminho === '/chaves-api' && metodo === 'POST') {
      const funcionario = corpo?.email === 'dono@teste.com';
      if (!['dono@teste.com', 'cliente@teste.com'].includes(corpo?.email) || corpo?.senha !== 'Senha12345') {
        return json(route, 401, { status: 401, erro: 'Não autorizado', mensagem: 'E-mail ou senha incorretos.' });
      }
      if (!funcionario && corpo?.nivel !== 'LEITURA') return json(route, 403, { status: 403, mensagem: 'Usuários do tipo ALUNO podem gerar chaves de no máximo nível LEITURA.' });
      return json(route, 201, {
        id: funcionario ? 10 : 11, chave: `bib_simulada_${funcionario ? 'dono' : 'cliente'}`, nivel: funcionario ? 'ADMIN' : 'LEITURA',
        usuarioId: funcionario ? 1 : 2, usuarioNome: funcionario ? 'Dono da Biblioteca' : 'Cliente Teste', usuarioTipo: funcionario ? 'FUNCIONARIO' : 'ALUNO',
      });
    }
    if (caminho.startsWith('/chaves-api/') && metodo === 'DELETE') return json(route, 204);

    if (caminho === '/livros') return json(route, 200, pagina('livroList', livros, 12));
    if (caminho === '/livros/buscar') {
      const termo = (url.searchParams.get('titulo') ?? '').toLowerCase();
      return json(route, 200, pagina('livroList', livros.filter((l) => l.titulo.toLowerCase().includes(termo)), 12));
    }
    const livro = caminho.match(/^\/livros\/(\d+)$/);
    if (livro) return json(route, 200, livros.find((l) => l.id === Number(livro[1])) ?? livros[0]);
    if (caminho === '/categorias') return json(route, 200, pagina('categoriaList', categorias, 100));
    if (caminho === '/autores') return json(route, 200, pagina('autorList', [autor]));
    if (caminho === '/reservas' && metodo === 'POST') {
      return json(route, 201, { id: 99, status: 'RESERVADO', retirarAte: '2030-01-04', livro: livros[0] });
    }
    if (caminho === '/reservas/minhas') return json(route, 200, pagina('emprestimoList', []));
    // Demais listagens (empréstimos, usuários, carteirinhas...): vazias.
    return json(route, 200, pagina('lista', []));
  });
  return chamadas;
}

/** Entra pelo formulário de login do site. */
export async function entrarNoSite(page: Page, email: string, senha: string) {
  await page.goto('/entrar');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(senha);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
}

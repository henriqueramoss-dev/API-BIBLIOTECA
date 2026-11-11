// Cliente HTTP da Biblioteca API (Spring Boot + HATEOAS).

export type Id = number;

export interface Page<T> {
  items: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Erro retornado pelo ApiExceptionHandler da API. */
export class ApiError extends Error {
  status: number;
  campos: Record<string, string>;

  constructor(status: number, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

let baseUrl = 'https://api-biblioteca-xgsa.onrender.com';

export function setBaseUrl(url: string) {
  baseUrl = url.replace(/\/+$/, '');
}

export function getBaseUrl() {
  return baseUrl;
}

// Chave de LEITURA do catálogo: pública por natureza (fica neste código), não pertence a ninguém e só lê livros,
// autores e categorias. É a que os visitantes usam; quem entra passa a usar a própria chave. Para trocar, VITE_API_KEY.
export const CHAVE_DO_CATALOGO: string = import.meta.env.VITE_API_KEY || 'bib_catalogo_aberto_da_biblioteca_2026';

// Chave de API enviada no cabeçalho X-API-Key (a API exige uma em todas as rotas, menos login e cadastro).
let apiKey = CHAVE_DO_CATALOGO;

// Sem chave (sair, sessão que caiu), volta para a chave do catálogo.
export function setApiKey(chave: string | undefined) {
  apiKey = (chave ?? '').trim() || CHAVE_DO_CATALOGO;
}

// Chamada quando a API recusa a chave em uso (401): chave revogada ou inválida. O login limpa a sessão.
let aoNaoAutorizado: (() => void) | undefined;

export function setOnUnauthorized(callback: (() => void) | undefined) {
  aoNaoAutorizado = callback;
}

const DEFAULT_MESSAGES: Record<number, string> = {
  400: 'Dados inválidos. Verifique os campos informados.',
  401: 'Entre com seu e-mail e senha para acessar este recurso.',
  403: 'Seu nível de acesso não permite esta operação.',
  429: 'Muitas requisições em pouco tempo. Aguarde alguns segundos e tente de novo.',
  404: 'Registro não encontrado.',
  409: 'Operação não permitida: dado duplicado ou registro em uso.',
};

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let res: Response;
  try {
    res = await fetch(baseUrl + path, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/hal+json',
        ...(apiKey ? { 'X-API-Key': apiKey } : {}),
        ...init.headers,
      },
    });
  } catch {
    throw new ApiError(0, `Não foi possível conectar à API em ${baseUrl}. Ela está rodando?`);
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  let body: any;
  try {
    body = text ? JSON.parse(text) : undefined;
  } catch {
    body = undefined;
  }

  // 401 com uma chave de conta (revogada, API reiniciada): a sessão terminou. A chave do catálogo não é sessão.
  if (res.status === 401 && apiKey !== CHAVE_DO_CATALOGO) aoNaoAutorizado?.();

  if (!res.ok) {
    const mensagem = body?.mensagem ?? DEFAULT_MESSAGES[res.status] ?? `Erro ${res.status} ao chamar a API.`;
    throw new ApiError(res.status, mensagem, body?.campos ?? {});
  }
  return body as T;
}

// Remove os _links do HATEOAS para trabalhar só com os dados.
function strip<T>(item: any): T {
  const { _links, ...rest } = item ?? {};
  return rest as T;
}

export interface ListParams {
  page?: number;
  size?: number;
  /** Ex.: 'id,desc'. Padrão: 'id'. */
  sort?: string;
  /** Parâmetro do endpoint /buscar (ex.: { titulo: 'Dom' }). */
  search?: Record<string, string>;
}

export async function list<T>(resource: string, embeddedKey: string, params: ListParams = {}): Promise<Page<T>> {
  const qs = new URLSearchParams({ page: String(params.page ?? 0), size: String(params.size ?? 10), sort: params.sort ?? 'id' });
  const search = Object.entries(params.search ?? {}).filter(([, v]) => v.trim() !== '');
  search.forEach(([k, v]) => qs.set(k, v.trim()));

  const path = search.length ? `/${resource}/buscar` : `/${resource}`;
  const body = await request<any>(`${path}?${qs}`);
  return {
    items: (body?._embedded?.[embeddedKey] ?? []).map(strip<T>),
    number: body?.page?.number ?? 0,
    size: body?.page?.size ?? 0,
    totalElements: body?.page?.totalElements ?? 0,
    totalPages: body?.page?.totalPages ?? 0,
  };
}

export async function get<T>(resource: string, id: Id): Promise<T> {
  return strip<T>(await request(`/${resource}/${id}`));
}

export async function create<T>(resource: string, data: unknown): Promise<T> {
  return strip<T>(await request(`/${resource}`, { method: 'POST', body: JSON.stringify(data) }));
}

export async function update<T>(resource: string, id: Id, data: unknown): Promise<T> {
  return strip<T>(await request(`/${resource}/${id}`, { method: 'PUT', body: JSON.stringify(data) }));
}

export async function remove(resource: string, id: Id): Promise<void> {
  await request(`/${resource}/${id}`, { method: 'DELETE' });
}

export type Nivel = 'LEITURA' | 'ESCRITA' | 'ADMIN';

export interface ChaveCriada {
  id: Id;
  chave: string;
  nivel: Nivel;
  usuarioId?: Id;
  usuarioNome?: string;
  /** FUNCIONARIO é a equipe/dono da biblioteca; os demais tipos são clientes. */
  usuarioTipo?: string;
}

/** Gera uma chave de API conferindo e-mail e senha (POST /chaves-api). A chave completa só vem nesta resposta. */
export async function gerarChave(email: string, senha: string, nivel: Nivel): Promise<ChaveCriada> {
  const body = await request<any>('/chaves-api', {
    method: 'POST',
    body: JSON.stringify({ email: email.trim(), senha, nome: 'Site da Biblioteca', nivel }),
  });
  return {
    id: body.id,
    chave: body.chave,
    nivel: body.nivel,
    usuarioId: body.usuarioId,
    usuarioNome: body.usuarioNome,
    usuarioTipo: body.usuarioTipo,
  };
}

/** Revoga uma chave: ela deixa de funcionar na hora. */
export async function revogarChave(id: Id): Promise<void> {
  await request(`/chaves-api/${id}`, { method: 'DELETE' });
}

// ---- Reservas (área do cliente)

/** Reserva um livro para o dono da chave (POST /reservas). Devolve o empréstimo com status RESERVADO e retirarAte. */
export async function reservarLivro<T = any>(livroId: Id, observacao?: string): Promise<T> {
  return strip<T>(await request('/reservas', { method: 'POST', body: JSON.stringify({ livroId, observacao: observacao || null }) }));
}

/** Cancela uma reserva ainda não retirada. */
export async function cancelarReserva(id: Id): Promise<void> {
  await request(`/reservas/${id}`, { method: 'DELETE' });
}

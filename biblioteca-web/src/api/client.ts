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

let baseUrl = 'http://localhost:8080';

export function setBaseUrl(url: string) {
  baseUrl = url.replace(/\/+$/, '');
}

export function getBaseUrl() {
  return baseUrl;
}

const DEFAULT_MESSAGES: Record<number, string> = {
  400: 'Dados inválidos. Verifique os campos informados.',
  404: 'Registro não encontrado.',
  409: 'Operação não permitida: dado duplicado ou registro em uso.',
};

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let res: Response;
  try {
    res = await fetch(baseUrl + path, {
      ...init,
      headers: { 'Content-Type': 'application/json', Accept: 'application/hal+json', ...init.headers },
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

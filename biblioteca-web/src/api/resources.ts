// Descrição de cada recurso da API: campos do formulário, busca e como exibir.
// As telas de listagem e formulário (web e mobile) são montadas a partir daqui.
// O app mobile (repositório REACT-NATIVE) usa um arquivo equivalente.

export type StatusEmprestimo = 'ATIVO' | 'DEVOLVIDO' | 'ATRASADO';

export interface Autor { id: number; nome: string }
export interface Categoria { id: number; nome: string }
export interface Usuario { id: number; nome: string; email: string }
export interface Livro {
  id: number;
  titulo: string;
  isbn: string;
  anoPublicacao: number;
  categoria: Categoria;
  autores: Autor[];
}
export interface Perfil {
  id: number;
  telefone: string;
  endereco: string;
  dataNascimento: string;
  usuario: Usuario;
}
export interface Emprestimo {
  id: number;
  dataEmprestimo: string;
  dataPrevistaDevolucao: string;
  status: StatusEmprestimo;
  usuario: Usuario;
  livro: Livro;
}

export type ResourceKey = 'livros' | 'autores' | 'categorias' | 'usuarios' | 'perfis' | 'emprestimos';

export type FormValues = Record<string, string | string[]>;

export interface Option { value: string; label: string }

export type Field =
  | { name: string; label: string; type: 'text' | 'email' | 'number' | 'date'; required?: boolean; maxLength?: number; placeholder?: string }
  | { name: string; label: string; type: 'enum'; required?: boolean; options: Option[] }
  /** Seleciona um registro de outro recurso (envia o ID). */
  | { name: string; label: string; type: 'relation'; required?: boolean; resource: ResourceKey }
  /** Seleciona vários registros de outro recurso (envia lista de IDs). */
  | { name: string; label: string; type: 'relations'; required?: boolean; resource: ResourceKey };

export interface Resource<T = any> {
  key: ResourceKey;
  label: string;
  singular: string;
  /** Texto do botão de cadastro (ex.: "Nova categoria"). */
  newLabel: string;
  /** Mensagem da lista vazia. */
  emptyLabel: string;
  /** Chave dentro de _embedded na resposta paginada. */
  embeddedKey: string;
  search: { param: string; label: string; options?: Option[] };
  fields: Field[];
  title: (item: T) => string;
  subtitle: (item: T) => string;
  /** Colunas da tabela (web) / detalhes do card (mobile). */
  details: (item: T) => { label: string; value: string }[];
  badge?: (item: T) => { text: string; tone: 'ok' | 'warn' | 'danger' } | undefined;
  toForm: (item?: T) => FormValues;
  toPayload: (values: FormValues) => unknown;
}

export const STATUS_OPTIONS: Option[] = [
  { value: 'ATIVO', label: 'Ativo' },
  { value: 'DEVOLVIDO', label: 'Devolvido' },
  { value: 'ATRASADO', label: 'Atrasado' },
];

const str = (v: string | string[] | undefined) => (Array.isArray(v) ? v[0] ?? '' : v ?? '').trim();
const num = (v: string | string[] | undefined) => (str(v) === '' ? null : Number(str(v)));
const ids = (v: string | string[] | undefined) => (Array.isArray(v) ? v.map(Number) : []);

/** Converte "2026-10-09" em "09/10/2026". */
export function formatDate(iso?: string) {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  return d && m && y ? `${d}/${m}/${y}` : iso;
}

/** Confere formato AAAA-MM-DD e se a data existe (ex.: 31/02 é inválida). */
export function isValidIsoDate(value: string) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const [y, m, d] = value.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  return date.getUTCFullYear() === y && date.getUTCMonth() === m - 1 && date.getUTCDate() === d;
}

export function today(offsetDays = 0) {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const statusTone = { ATIVO: 'ok', DEVOLVIDO: 'warn', ATRASADO: 'danger' } as const;

const livros: Resource<Livro> = {
  key: 'livros',
  label: 'Livros',
  singular: 'Livro',
  newLabel: 'Novo livro',
  emptyLabel: 'Nenhum livro cadastrado.',
  embeddedKey: 'livroList',
  search: { param: 'titulo', label: 'Buscar por título' },
  fields: [
    { name: 'titulo', label: 'Título', type: 'text', required: true, maxLength: 150, placeholder: 'Dom Casmurro' },
    { name: 'isbn', label: 'ISBN', type: 'text', required: true, maxLength: 20, placeholder: '9788535910663' },
    { name: 'anoPublicacao', label: 'Ano de publicação', type: 'number', required: true, placeholder: '1899' },
    { name: 'categoriaId', label: 'Categoria', type: 'relation', required: true, resource: 'categorias' },
    { name: 'autoresIds', label: 'Autores', type: 'relations', resource: 'autores' },
  ],
  title: (l) => l.titulo,
  subtitle: (l) => l.autores.map((a) => a.nome).join(', ') || 'Sem autor',
  details: (l) => [
    { label: 'ISBN', value: l.isbn },
    { label: 'Ano', value: String(l.anoPublicacao) },
    { label: 'Categoria', value: l.categoria?.nome ?? '—' },
  ],
  toForm: (l) => ({
    titulo: l?.titulo ?? '',
    isbn: l?.isbn ?? '',
    anoPublicacao: l ? String(l.anoPublicacao) : '',
    categoriaId: l?.categoria ? String(l.categoria.id) : '',
    autoresIds: l?.autores.map((a) => String(a.id)) ?? [],
  }),
  toPayload: (v) => ({
    titulo: str(v.titulo),
    isbn: str(v.isbn),
    anoPublicacao: num(v.anoPublicacao),
    categoriaId: num(v.categoriaId),
    autoresIds: ids(v.autoresIds),
  }),
};

const autores: Resource<Autor> = {
  key: 'autores',
  label: 'Autores',
  singular: 'Autor',
  newLabel: 'Novo autor',
  emptyLabel: 'Nenhum autor cadastrado.',
  embeddedKey: 'autorList',
  search: { param: 'nome', label: 'Buscar por nome' },
  fields: [{ name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 100, placeholder: 'Machado de Assis' }],
  title: (a) => a.nome,
  subtitle: (a) => `#${a.id}`,
  details: () => [],
  toForm: (a) => ({ nome: a?.nome ?? '' }),
  toPayload: (v) => ({ nome: str(v.nome) }),
};

const categorias: Resource<Categoria> = {
  key: 'categorias',
  label: 'Categorias',
  singular: 'Categoria',
  newLabel: 'Nova categoria',
  emptyLabel: 'Nenhuma categoria cadastrada.',
  embeddedKey: 'categoriaList',
  search: { param: 'nome', label: 'Buscar por nome' },
  fields: [{ name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 80, placeholder: 'Romance' }],
  title: (c) => c.nome,
  subtitle: (c) => `#${c.id}`,
  details: () => [],
  toForm: (c) => ({ nome: c?.nome ?? '' }),
  toPayload: (v) => ({ nome: str(v.nome) }),
};

const usuarios: Resource<Usuario> = {
  key: 'usuarios',
  label: 'Usuários',
  singular: 'Usuário',
  newLabel: 'Novo usuário',
  emptyLabel: 'Nenhum usuário cadastrado.',
  embeddedKey: 'usuarioList',
  search: { param: 'nome', label: 'Buscar por nome' },
  fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 100, placeholder: 'Henrique Ramos' },
    { name: 'email', label: 'E-mail', type: 'email', required: true, maxLength: 150, placeholder: 'henrique@email.com' },
  ],
  title: (u) => u.nome,
  subtitle: (u) => u.email,
  details: () => [],
  toForm: (u) => ({ nome: u?.nome ?? '', email: u?.email ?? '' }),
  toPayload: (v) => ({ nome: str(v.nome), email: str(v.email) }),
};

const perfis: Resource<Perfil> = {
  key: 'perfis',
  label: 'Perfis',
  singular: 'Perfil',
  newLabel: 'Novo perfil',
  emptyLabel: 'Nenhum perfil cadastrado.',
  embeddedKey: 'perfilList',
  search: { param: 'telefone', label: 'Buscar por telefone' },
  fields: [
    { name: 'usuarioId', label: 'Usuário', type: 'relation', required: true, resource: 'usuarios' },
    { name: 'telefone', label: 'Telefone', type: 'text', required: true, maxLength: 20, placeholder: '11999999999' },
    { name: 'endereco', label: 'Endereço', type: 'text', required: true, maxLength: 200, placeholder: 'Rua Exemplo, 100' },
    { name: 'dataNascimento', label: 'Data de nascimento', type: 'date', required: true },
  ],
  title: (p) => p.usuario?.nome ?? `Perfil #${p.id}`,
  subtitle: (p) => p.telefone,
  details: (p) => [
    { label: 'Endereço', value: p.endereco },
    { label: 'Nascimento', value: formatDate(p.dataNascimento) },
  ],
  toForm: (p) => ({
    usuarioId: p?.usuario ? String(p.usuario.id) : '',
    telefone: p?.telefone ?? '',
    endereco: p?.endereco ?? '',
    dataNascimento: p?.dataNascimento ?? '',
  }),
  toPayload: (v) => ({
    usuarioId: num(v.usuarioId),
    telefone: str(v.telefone),
    endereco: str(v.endereco),
    dataNascimento: str(v.dataNascimento) || null,
  }),
};

const emprestimos: Resource<Emprestimo> = {
  key: 'emprestimos',
  label: 'Empréstimos',
  singular: 'Empréstimo',
  newLabel: 'Novo empréstimo',
  emptyLabel: 'Nenhum empréstimo registrado.',
  embeddedKey: 'emprestimoList',
  search: { param: 'status', label: 'Filtrar por status', options: STATUS_OPTIONS },
  fields: [
    { name: 'livroId', label: 'Livro', type: 'relation', required: true, resource: 'livros' },
    { name: 'usuarioId', label: 'Usuário', type: 'relation', required: true, resource: 'usuarios' },
    { name: 'dataEmprestimo', label: 'Data do empréstimo', type: 'date', required: true },
    { name: 'dataPrevistaDevolucao', label: 'Devolução prevista', type: 'date', required: true },
    { name: 'status', label: 'Status', type: 'enum', required: true, options: STATUS_OPTIONS },
  ],
  title: (e) => e.livro?.titulo ?? `Empréstimo #${e.id}`,
  subtitle: (e) => e.usuario?.nome ?? '',
  details: (e) => [
    { label: 'Emprestado em', value: formatDate(e.dataEmprestimo) },
    { label: 'Devolução', value: formatDate(e.dataPrevistaDevolucao) },
  ],
  badge: (e) => ({
    text: STATUS_OPTIONS.find((o) => o.value === e.status)?.label ?? e.status,
    tone: statusTone[e.status] ?? 'warn',
  }),
  toForm: (e) => ({
    livroId: e?.livro ? String(e.livro.id) : '',
    usuarioId: e?.usuario ? String(e.usuario.id) : '',
    dataEmprestimo: e?.dataEmprestimo ?? today(),
    dataPrevistaDevolucao: e?.dataPrevistaDevolucao ?? today(14),
    status: e?.status ?? 'ATIVO',
  }),
  toPayload: (v) => ({
    livroId: num(v.livroId),
    usuarioId: num(v.usuarioId),
    dataEmprestimo: str(v.dataEmprestimo) || null,
    dataPrevistaDevolucao: str(v.dataPrevistaDevolucao) || null,
    status: str(v.status) || null,
  }),
};

export const RESOURCES: Record<ResourceKey, Resource> = { livros, autores, categorias, usuarios, perfis, emprestimos };

export const RESOURCE_ORDER: ResourceKey[] = ['emprestimos', 'livros', 'autores', 'categorias', 'usuarios', 'perfis'];

export function isResourceKey(key: string | undefined): key is ResourceKey {
  return !!key && key in RESOURCES;
}

/** Validação local antes de enviar (a API também valida e devolve os erros por campo). */
export function validate(resource: Resource, values: FormValues): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const f of resource.fields) {
    const v = values[f.name];
    const empty = Array.isArray(v) ? v.length === 0 : !v || v.trim() === '';
    if (f.required && empty) {
      errors[f.name] = `${f.label} é obrigatório.`;
      continue;
    }
    if (empty || Array.isArray(v)) continue;
    if ('maxLength' in f && f.maxLength && v.length > f.maxLength) errors[f.name] = `Máximo de ${f.maxLength} caracteres.`;
    if (f.type === 'number' && !/^\d+$/.test(v.trim())) errors[f.name] = 'Informe um número válido.';
    if (f.type === 'email' && !/^\S+@\S+\.\S+$/.test(v.trim())) errors[f.name] = 'Informe um e-mail válido.';
    if (f.type === 'date' && !isValidIsoDate(v.trim())) errors[f.name] = 'Informe uma data válida.';
  }
  if (resource.key === 'emprestimos' && !errors.dataPrevistaDevolucao) {
    const ini = str(values.dataEmprestimo);
    const fim = str(values.dataPrevistaDevolucao);
    if (ini && fim && fim < ini) errors.dataPrevistaDevolucao = 'A devolução deve ser depois do empréstimo.';
  }
  return errors;
}

// Descrição de cada recurso da API: campos do formulário, busca e como exibir.
// As telas de listagem e formulário (web e mobile) são montadas a partir daqui.
// O app mobile (repositório REACT-NATIVE) usa um arquivo equivalente.

import { formatDate, isValidIsoDate, today } from '../utils/datas';

export type StatusEmprestimo = 'ATIVO' | 'DEVOLVIDO' | 'ATRASADO';

export type TipoUsuario = 'ALUNO' | 'PROFESSOR' | 'FUNCIONARIO' | 'VISITANTE';

export interface Autor {
  id: number;
  nome: string;
  nacionalidade: string;
  dataNascimento?: string | null;
  generoLiterario?: string | null;
  biografia?: string | null;
}
export interface Categoria {
  id: number;
  nome: string;
  codigo: string;
  descricao?: string | null;
  localizacao?: string | null;
  publicoAlvo?: string | null;
}
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  matricula?: string | null;
  tipo: TipoUsuario;
}
export interface Livro {
  id: number;
  titulo: string;
  isbn: string;
  anoPublicacao: number;
  editora: string;
  numeroPaginas: number;
  categoria: Categoria;
  autores: Autor[];
}
export interface Perfil {
  id: number;
  telefone: string;
  endereco: string;
  cidade: string;
  estado: string;
  cep: string;
  dataNascimento: string;
  usuario: Usuario;
}
export interface Emprestimo {
  id: number;
  dataEmprestimo: string;
  dataPrevistaDevolucao: string;
  dataDevolucao?: string | null;
  status: StatusEmprestimo;
  observacao?: string | null;
  usuario: Usuario;
  livro: Livro;
}

export type ResourceKey = 'livros' | 'autores' | 'categorias' | 'usuarios' | 'perfis' | 'emprestimos';

export type FormValues = Record<string, string | string[]>;

export interface Option { value: string; label: string }

export type Field =
  | { name: string; label: string; type: 'text' | 'email' | 'number' | 'date'; required?: boolean; maxLength?: number; placeholder?: string }
  /** Somente dígitos, com tamanho exato (CPF, CEP). */
  | { name: string; label: string; type: 'digits'; required?: boolean; length: number; placeholder?: string }
  /** Sigla de estado com 2 letras. */
  | { name: string; label: string; type: 'uf'; required?: boolean; placeholder?: string }
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

export const TIPO_USUARIO_OPTIONS: Option[] = [
  { value: 'ALUNO', label: 'Aluno' },
  { value: 'PROFESSOR', label: 'Professor' },
  { value: 'FUNCIONARIO', label: 'Funcionário' },
  { value: 'VISITANTE', label: 'Visitante' },
];

const str = (v: string | string[] | undefined) => (Array.isArray(v) ? v[0] ?? '' : v ?? '').trim();
/** Texto opcional: string vazia vira null. */
const opt = (v: string | string[] | undefined) => str(v) || null;
const num = (v: string | string[] | undefined) => (str(v) === '' ? null : Number(str(v)));
const ids = (v: string | string[] | undefined) => (Array.isArray(v) ? v.map(Number) : []);

// formatDate, isValidIsoDate e today ficam em utils/datas.js (JavaScript puro).
export { formatDate, isValidIsoDate, today };

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
    { name: 'editora', label: 'Editora', type: 'text', required: true, maxLength: 100, placeholder: 'Companhia das Letras' },
    { name: 'numeroPaginas', label: 'Número de páginas', type: 'number', required: true, placeholder: '256' },
    { name: 'categoriaId', label: 'Categoria', type: 'relation', required: true, resource: 'categorias' },
    { name: 'autoresIds', label: 'Autores', type: 'relations', resource: 'autores' },
  ],
  title: (l) => l.titulo,
  subtitle: (l) => l.autores.map((a) => a.nome).join(', ') || 'Sem autor',
  details: (l) => [
    { label: 'ISBN', value: l.isbn },
    { label: 'Ano', value: String(l.anoPublicacao) },
    { label: 'Editora', value: l.editora ?? '—' },
    { label: 'Categoria', value: l.categoria?.nome ?? '—' },
  ],
  toForm: (l) => ({
    titulo: l?.titulo ?? '',
    isbn: l?.isbn ?? '',
    anoPublicacao: l ? String(l.anoPublicacao) : '',
    editora: l?.editora ?? '',
    numeroPaginas: l?.numeroPaginas ? String(l.numeroPaginas) : '',
    categoriaId: l?.categoria ? String(l.categoria.id) : '',
    autoresIds: l?.autores.map((a) => String(a.id)) ?? [],
  }),
  toPayload: (v) => ({
    titulo: str(v.titulo),
    isbn: str(v.isbn),
    anoPublicacao: num(v.anoPublicacao),
    editora: str(v.editora),
    numeroPaginas: num(v.numeroPaginas),
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
  fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 100, placeholder: 'Machado de Assis' },
    { name: 'nacionalidade', label: 'Nacionalidade', type: 'text', required: true, maxLength: 60, placeholder: 'Brasileira' },
    { name: 'dataNascimento', label: 'Data de nascimento', type: 'date' },
    { name: 'generoLiterario', label: 'Gênero literário', type: 'text', maxLength: 60, placeholder: 'Romance' },
    { name: 'biografia', label: 'Biografia', type: 'text', maxLength: 1000, placeholder: 'Resumo da vida e da obra' },
  ],
  title: (a) => a.nome,
  subtitle: (a) => a.nacionalidade ?? '',
  details: (a) => [
    { label: 'Gênero', value: a.generoLiterario || '—' },
    { label: 'Nascimento', value: formatDate(a.dataNascimento ?? undefined) },
  ],
  toForm: (a) => ({
    nome: a?.nome ?? '',
    nacionalidade: a?.nacionalidade ?? '',
    dataNascimento: a?.dataNascimento ?? '',
    generoLiterario: a?.generoLiterario ?? '',
    biografia: a?.biografia ?? '',
  }),
  toPayload: (v) => ({
    nome: str(v.nome),
    nacionalidade: str(v.nacionalidade),
    dataNascimento: opt(v.dataNascimento),
    generoLiterario: opt(v.generoLiterario),
    biografia: opt(v.biografia),
  }),
};

const categorias: Resource<Categoria> = {
  key: 'categorias',
  label: 'Categorias',
  singular: 'Categoria',
  newLabel: 'Nova categoria',
  emptyLabel: 'Nenhuma categoria cadastrada.',
  embeddedKey: 'categoriaList',
  search: { param: 'nome', label: 'Buscar por nome' },
  fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 80, placeholder: 'Romance' },
    { name: 'codigo', label: 'Código', type: 'text', required: true, maxLength: 10, placeholder: 'ROM' },
    { name: 'descricao', label: 'Descrição', type: 'text', maxLength: 255, placeholder: 'Narrativas de ficção em prosa' },
    { name: 'localizacao', label: 'Localização', type: 'text', maxLength: 50, placeholder: 'Estante A' },
    { name: 'publicoAlvo', label: 'Público-alvo', type: 'text', maxLength: 50, placeholder: 'Adulto' },
  ],
  title: (c) => c.nome,
  subtitle: (c) => c.descricao || '',
  details: (c) => [
    { label: 'Código', value: c.codigo ?? '—' },
    { label: 'Localização', value: c.localizacao || '—' },
    { label: 'Público', value: c.publicoAlvo || '—' },
  ],
  toForm: (c) => ({
    nome: c?.nome ?? '',
    codigo: c?.codigo ?? '',
    descricao: c?.descricao ?? '',
    localizacao: c?.localizacao ?? '',
    publicoAlvo: c?.publicoAlvo ?? '',
  }),
  toPayload: (v) => ({
    nome: str(v.nome),
    codigo: str(v.codigo).toUpperCase(),
    descricao: opt(v.descricao),
    localizacao: opt(v.localizacao),
    publicoAlvo: opt(v.publicoAlvo),
  }),
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
    { name: 'cpf', label: 'CPF', type: 'digits', required: true, length: 11, placeholder: '12345678909' },
    { name: 'matricula', label: 'Matrícula', type: 'text', maxLength: 20, placeholder: '2026001' },
    { name: 'tipo', label: 'Tipo', type: 'enum', required: true, options: TIPO_USUARIO_OPTIONS },
  ],
  title: (u) => u.nome,
  subtitle: (u) => u.email,
  details: (u) => [
    { label: 'Tipo', value: TIPO_USUARIO_OPTIONS.find((o) => o.value === u.tipo)?.label ?? '—' },
    { label: 'CPF', value: u.cpf ?? '—' },
    { label: 'Matrícula', value: u.matricula || '—' },
  ],
  toForm: (u) => ({
    nome: u?.nome ?? '',
    email: u?.email ?? '',
    cpf: u?.cpf ?? '',
    matricula: u?.matricula ?? '',
    tipo: u?.tipo ?? 'ALUNO',
  }),
  toPayload: (v) => ({
    nome: str(v.nome),
    email: str(v.email),
    cpf: str(v.cpf),
    matricula: opt(v.matricula),
    tipo: str(v.tipo) || null,
  }),
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
    { name: 'cidade', label: 'Cidade', type: 'text', required: true, maxLength: 80, placeholder: 'São Paulo' },
    { name: 'estado', label: 'Estado (UF)', type: 'uf', required: true, placeholder: 'SP' },
    { name: 'cep', label: 'CEP', type: 'digits', required: true, length: 8, placeholder: '01001000' },
    { name: 'dataNascimento', label: 'Data de nascimento', type: 'date', required: true },
  ],
  title: (p) => p.usuario?.nome ?? `Perfil #${p.id}`,
  subtitle: (p) => p.telefone,
  details: (p) => [
    { label: 'Endereço', value: p.endereco },
    { label: 'Cidade', value: p.cidade ? `${p.cidade}/${p.estado}` : '—' },
    { label: 'Nascimento', value: formatDate(p.dataNascimento) },
  ],
  toForm: (p) => ({
    usuarioId: p?.usuario ? String(p.usuario.id) : '',
    telefone: p?.telefone ?? '',
    endereco: p?.endereco ?? '',
    cidade: p?.cidade ?? '',
    estado: p?.estado ?? '',
    cep: p?.cep ?? '',
    dataNascimento: p?.dataNascimento ?? '',
  }),
  toPayload: (v) => ({
    usuarioId: num(v.usuarioId),
    telefone: str(v.telefone),
    endereco: str(v.endereco),
    cidade: str(v.cidade),
    estado: str(v.estado).toUpperCase(),
    cep: str(v.cep),
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
    { name: 'dataDevolucao', label: 'Devolvido em', type: 'date' },
    { name: 'observacao', label: 'Observação', type: 'text', maxLength: 255, placeholder: 'Ex.: capa danificada' },
  ],
  title: (e) => e.livro?.titulo ?? `Empréstimo #${e.id}`,
  subtitle: (e) => e.usuario?.nome ?? '',
  details: (e) => [
    { label: 'Emprestado em', value: formatDate(e.dataEmprestimo) },
    { label: 'Devolução', value: formatDate(e.dataPrevistaDevolucao) },
    { label: 'Devolvido em', value: formatDate(e.dataDevolucao ?? undefined) },
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
    dataDevolucao: e?.dataDevolucao ?? '',
    observacao: e?.observacao ?? '',
  }),
  toPayload: (v) => ({
    livroId: num(v.livroId),
    usuarioId: num(v.usuarioId),
    dataEmprestimo: str(v.dataEmprestimo) || null,
    dataPrevistaDevolucao: str(v.dataPrevistaDevolucao) || null,
    status: str(v.status) || null,
    dataDevolucao: opt(v.dataDevolucao),
    observacao: opt(v.observacao),
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
    if (f.type === 'digits' && !new RegExp(`^\\d{${f.length}}$`).test(v.trim())) errors[f.name] = `Informe ${f.length} dígitos, sem pontos ou traço.`;
    if (f.type === 'uf' && !/^[A-Za-z]{2}$/.test(v.trim())) errors[f.name] = 'Informe a sigla com 2 letras (ex.: SP).';
  }
  if (resource.key === 'emprestimos' && !errors.dataPrevistaDevolucao) {
    const ini = str(values.dataEmprestimo);
    const fim = str(values.dataPrevistaDevolucao);
    if (ini && fim && fim < ini) errors.dataPrevistaDevolucao = 'A devolução deve ser depois do empréstimo.';
  }
  if (resource.key === 'emprestimos' && !errors.dataDevolucao) {
    const ini = str(values.dataEmprestimo);
    const dev = str(values.dataDevolucao);
    if (ini && dev && dev < ini) errors.dataDevolucao = 'A devolução deve ser depois do empréstimo.';
  }
  return errors;
}

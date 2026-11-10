import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError, Nivel, gerarChave, revogarChave, setApiKey, setOnUnauthorized } from '../api/client';

// Login do site: e-mail e senha viram uma chave de API (POST /chaves-api), guardada só nesta aba do navegador.

const ORDEM: Nivel[] = ['LEITURA', 'ESCRITA', 'ADMIN'];
const ARMAZEM = 'biblioteca.sessao';

export interface Sessao {
  chave: string;
  /** ID da chave, usado para revogá-la ao sair. */
  id: number;
  nivel: Nivel;
  email: string;
  nome: string;
  tipo?: string;
}

interface Auth {
  sessao?: Sessao;
  /** Mensagem sobre a sessão (ex.: chave revogada), mostrada na tela de entrada. */
  aviso?: string;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => Promise<void>;
  /** A sessão tem pelo menos este nível de acesso? */
  pode: (nivel: Nivel) => boolean;
  /** É da equipe/dono da biblioteca (tipo FUNCIONARIO)? Os demais são clientes. */
  ehFuncionario: boolean;
}

/** Recursos com dados pessoais: a API só responde a quem tem chave. */
const PROTEGIDOS = ['usuarios', 'carteirinhas', 'emprestimos'];
export const exigeLogin = (recurso?: string) => !!recurso && PROTEGIDOS.includes(recurso);

function ler(): Sessao | undefined {
  try {
    const texto = sessionStorage.getItem(ARMAZEM);
    return texto ? (JSON.parse(texto) as Sessao) : undefined;
  } catch {
    return undefined;
  }
}

function gravar(sessao?: Sessao) {
  try {
    if (sessao) sessionStorage.setItem(ARMAZEM, JSON.stringify(sessao));
    else sessionStorage.removeItem(ARMAZEM);
  } catch {
    // Sem armazenamento (aba privada, por exemplo): a sessão vale só até recarregar a página.
  }
}

const Contexto = createContext<Auth | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | undefined>(() => {
    const salva = ler();
    if (salva) setApiKey(salva.chave);
    return salva;
  });
  const [aviso, setAviso] = useState<string>();

  // A API recusou a chave (revogada, por exemplo): volta ao estado de visitante.
  useEffect(() => {
    setOnUnauthorized(() => {
      gravar(undefined);
      setApiKey(import.meta.env.VITE_API_KEY);
      setSessao(undefined);
      setAviso('Sua sessão terminou. Entre novamente.');
    });
    return () => setOnUnauthorized(undefined);
  }, []);

  const entrar = useCallback(async (email: string, senha: string) => {
    // Funcionários (equipe da biblioteca) recebem uma chave ADMIN; clientes só podem ter LEITURA (a API responde 403 ao ADMIN).
    let criada;
    try {
      criada = await gerarChave(email, senha, 'ADMIN');
    } catch (e) {
      if ((e as ApiError).status !== 403) throw e;
      criada = await gerarChave(email, senha, 'LEITURA');
    }

    setApiKey(criada.chave);
    const nova: Sessao = {
      chave: criada.chave,
      id: criada.id,
      nivel: criada.nivel,
      email: email.trim(),
      nome: criada.usuarioNome ?? email.trim(),
      tipo: criada.usuarioTipo,
    };
    gravar(nova);
    setAviso(undefined);
    setSessao(nova);
  }, []);

  const sair = useCallback(async () => {
    const atual = sessao;
    // Revoga a chave no servidor para ela não ficar válida depois de sair. Se falhar, sai mesmo assim.
    if (atual) {
      try {
        await revogarChave(atual.id);
      } catch {
        // Ignorado: a sessão local termina de qualquer forma.
      }
    }
    gravar(undefined);
    setApiKey(import.meta.env.VITE_API_KEY);
    setAviso(undefined);
    setSessao(undefined);
  }, [sessao]);

  const valor = useMemo<Auth>(
    () => ({
      sessao,
      aviso,
      entrar,
      sair,
      pode: (nivel) => !!sessao && ORDEM.indexOf(sessao.nivel) >= ORDEM.indexOf(nivel),
      ehFuncionario: sessao?.tipo === 'FUNCIONARIO',
    }),
    [sessao, aviso, entrar, sair],
  );

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useAuth(): Auth {
  const auth = useContext(Contexto);
  if (!auth) throw new Error('useAuth precisa estar dentro de <AuthProvider>.');
  return auth;
}

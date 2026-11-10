import { FormEvent, useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';
import { Icon } from '../components/Icon';
import { PilhaDeLivros } from '../components/ilustracoes';

// Só aceita voltar para caminhos do próprio site (evita redirecionar para outro endereço).
const destinoSeguro = (de: string | null) => (de && de.startsWith('/') && !de.startsWith('//') ? de : '/');

export default function Entrar() {
  const { sessao, aviso, entrar } = useAuth();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const destino = destinoSeguro(params.get('de'));

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string>();
  const [enviando, setEnviando] = useState(false);

  if (sessao) return <Navigate to={destino} replace />;

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.');
      return;
    }
    setEnviando(true);
    setErro(undefined);
    try {
      await entrar(email, senha);
      navigate(destino, { replace: true });
    } catch (err) {
      const status = (err as ApiError).status;
      setErro(status === 401 ? 'E-mail ou senha incorretos.' : (err as Error).message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-wrap">
      <form className="card form login-card" onSubmit={enviar} noValidate>
        <div className="login-art"><PilhaDeLivros width={120} /></div>
        <h1>Entrar</h1>
        <p className="muted">Entre para reservar livros e acompanhar suas reservas. A equipe da biblioteca entra por aqui também.</p>
        {aviso && <div className="alert">{aviso}</div>}
        {erro && <div className="alert" role="alert">{erro}</div>}
        <div className="field">
          <label htmlFor="login-email">E-mail</label>
          <input id="login-email" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="login-senha">Senha</label>
          <input id="login-senha" type="password" autoComplete="current-password" value={senha} onChange={(e) => setSenha(e.target.value)} />
        </div>
        <div className="form-actions">
          <Link className="btn" to="/cadastro">Criar conta</Link>
          <button className="btn primary" type="submit" disabled={enviando}>
            <Icon name="usuarios" /> {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </div>
      </form>
    </div>
  );
}

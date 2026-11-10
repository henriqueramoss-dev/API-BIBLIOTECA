import { FormEvent, useMemo, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { ApiError, create } from '../api/client';
import { Field, FormValues, RESOURCES, validate } from '../api/resources';
import { useAuth } from '../auth/AuthContext';
import { Icon } from '../components/Icon';
import { FieldInput, mapApiErrors } from './ResourceForm';

// Autocadastro: a API só aceita os tipos Aluno e Visitante sem chave de API.
// Funcionários e professores são cadastrados pela biblioteca.
const TIPOS_AUTOCADASTRO = ['ALUNO', 'VISITANTE'];

export default function Cadastro() {
  const { sessao, entrar } = useAuth();
  const navigate = useNavigate();

  // Os mesmos campos do cadastro de usuários, com o tipo limitado.
  const usuarios = RESOURCES.usuarios;
  const recurso = useMemo(
    () => ({
      ...usuarios,
      fields: usuarios.fields.map((f: Field) =>
        f.type === 'enum' && f.name === 'tipo' ? { ...f, options: f.options.filter((o) => TIPOS_AUTOCADASTRO.includes(o.value)) } : f,
      ),
    }),
    [usuarios],
  );

  const [values, setValues] = useState<FormValues>(() => usuarios.toForm());
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<string>();
  const [saving, setSaving] = useState(false);

  if (sessao) return <Navigate to="/" replace />;

  const setField = (name: string, value: string | string[]) => {
    setValues((v) => ({ ...v, [name]: value }));
    setErrors(({ [name]: _, ...rest }) => rest);
  };

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const local = validate(recurso, values);
    if (senha.length < 8 || senha.length > 72) local.senha = 'A senha deve ter de 8 a 72 caracteres.';
    if (confirmacao !== senha) local.confirmacao = 'As senhas não conferem.';
    setErrors(local);
    if (Object.keys(local).length) return;

    setSaving(true);
    setMessage(undefined);
    try {
      await create('usuarios', { ...(usuarios.toPayload(values) as object), senha });
    } catch (err) {
      const apiErr = err as ApiError;
      setMessage(apiErr.message);
      setErrors(mapApiErrors(apiErr.campos ?? {}));
      setSaving(false);
      return;
    }
    // Conta criada: já entra com o e-mail e a senha informados.
    try {
      await entrar(String(values.email ?? ''), senha);
      navigate('/', { replace: true });
    } catch {
      navigate('/entrar', { replace: true });
    }
  };

  return (
    <div className="login-wrap">
      <form className="card form cadastro-card" onSubmit={enviar} noValidate>
        <h1>Criar conta</h1>
        <p className="muted">Crie sua conta de cliente para reservar livros e retirá-los na biblioteca.</p>
        {message && <div className="alert" role="alert">{message}</div>}
        {recurso.fields.map((f: Field) => (
          <FieldInput key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={(v) => setField(f.name, v)} />
        ))}
        <div className={`field ${errors.senha ? 'has-error' : ''}`}>
          <label htmlFor="cad-senha">Senha<span className="req"> *</span></label>
          <input id="cad-senha" type="password" autoComplete="new-password" maxLength={72} value={senha}
            onChange={(e) => { setSenha(e.target.value); setErrors(({ senha: _, ...r }) => r); }} />
          <span className="muted small">De 8 a 72 caracteres.</span>
          {errors.senha && <span className="error">{errors.senha}</span>}
        </div>
        <div className={`field ${errors.confirmacao ? 'has-error' : ''}`}>
          <label htmlFor="cad-confirmacao">Confirmar senha<span className="req"> *</span></label>
          <input id="cad-confirmacao" type="password" autoComplete="new-password" maxLength={72} value={confirmacao}
            onChange={(e) => { setConfirmacao(e.target.value); setErrors(({ confirmacao: _, ...r }) => r); }} />
          {errors.confirmacao && <span className="error">{errors.confirmacao}</span>}
        </div>
        <div className="form-actions">
          <Link className="btn" to="/entrar">Já tenho conta</Link>
          <button className="btn primary" type="submit" disabled={saving}>
            <Icon name="plus" /> {saving ? 'Criando…' : 'Criar conta'}
          </button>
        </div>
      </form>
    </div>
  );
}

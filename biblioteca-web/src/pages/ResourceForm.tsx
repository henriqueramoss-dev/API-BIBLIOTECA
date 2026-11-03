import { FormEvent, useEffect, useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { ApiError, create, get, update } from '../api/client';
import { Field, FormValues, isResourceKey, RESOURCES, validate } from '../api/resources';
import { Icon } from '../components/Icon';
import { useOptions } from '../components/useOptions';

export default function ResourceForm() {
  const { resource: key, id } = useParams();
  const navigate = useNavigate();
  const resource = isResourceKey(key) ? RESOURCES[key] : undefined;
  const editing = id !== undefined;

  const [values, setValues] = useState<FormValues>(() => resource?.toForm() ?? {});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<string>();
  const [loading, setLoading] = useState(editing);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!resource || !editing) return;
    get(resource.key, Number(id))
      .then((item) => setValues(resource.toForm(item)))
      .catch((e: Error) => setMessage(e.message))
      .finally(() => setLoading(false));
  }, [resource, editing, id]);

  if (!resource) return <Navigate to="/" replace />;

  const setField = (name: string, value: string | string[]) => {
    setValues((v) => ({ ...v, [name]: value }));
    setErrors(({ [name]: _, ...rest }) => rest);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const local = validate(resource, values);
    setErrors(local);
    if (Object.keys(local).length) return;

    setSaving(true);
    setMessage(undefined);
    try {
      const payload = resource.toPayload(values);
      if (editing) await update(resource.key, Number(id), payload);
      else await create(resource.key, payload);
      navigate(`/${resource.key}`);
    } catch (err) {
      const apiErr = err as ApiError;
      setMessage(apiErr.message);
      setErrors(mapApiErrors(apiErr.campos ?? {}));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <Link to={`/${resource.key}`} className="back"><Icon name="back" /> {resource.label}</Link>
          <h1>{editing ? `Editar ${resource.singular.toLowerCase()}` : resource.newLabel}</h1>
        </div>
      </div>

      <form className="card form" onSubmit={handleSubmit} noValidate>
        {message && <div className="alert">{message}</div>}
        {loading ? (
          <p className="empty">Carregando…</p>
        ) : (
          resource.fields.map((f) => (
            <FieldInput key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={(v) => setField(f.name, v)} />
          ))
        )}
        <div className="form-actions">
          <Link className="btn" to={`/${resource.key}`}>Cancelar</Link>
          <button className="btn primary" type="submit" disabled={saving || loading}>
            {saving ? 'Salvando…' : 'Salvar'}
          </button>
        </div>
      </form>
    </>
  );
}

// A API devolve erros pelo nome do campo do DTO (ex.: "usuarioId") — mesmo nome usado nos formulários.
function mapApiErrors(campos: Record<string, string>) {
  const map: Record<string, string> = { usuario: 'usuarioId', livro: 'livroId', categoria: 'categoriaId' };
  return Object.fromEntries(Object.entries(campos).map(([k, v]) => [map[k] ?? k, v]));
}

function FieldInput({ field, value, error, onChange }: {
  field: Field;
  value: string | string[] | undefined;
  error?: string;
  onChange: (v: string | string[]) => void;
}) {
  const relationKey = field.type === 'relation' || field.type === 'relations' ? field.resource : undefined;
  const { options, error: optionsError, loaded } = useOptions(relationKey);
  const id = `f-${field.name}`;
  const str = typeof value === 'string' ? value : '';

  let input;
  switch (field.type) {
    case 'enum':
      input = (
        <select id={id} value={str} onChange={(e) => onChange(e.target.value)}>
          {field.options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      );
      break;
    case 'relation':
      input = (
        <select id={id} value={str} onChange={(e) => onChange(e.target.value)}>
          <option value="">Selecione…</option>
          {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      );
      break;
    case 'relations': {
      const selected = Array.isArray(value) ? value : [];
      input = loaded && options.length === 0 ? (
        <p className="muted small">Nenhum registro em {RESOURCES[field.resource].label.toLowerCase()}. <Link to={`/${field.resource}/novo`}>Cadastrar</Link></p>
      ) : (
        <div className="checks">
          {options.map((o) => (
            <label key={o.value} className={`check ${selected.includes(o.value) ? 'on' : ''}`}>
              <input type="checkbox" checked={selected.includes(o.value)}
                onChange={(e) => onChange(e.target.checked ? [...selected, o.value] : selected.filter((s) => s !== o.value))} />
              {o.label}
            </label>
          ))}
        </div>
      );
      break;
    }
    case 'digits':
      input = (
        <input id={id} type="text" inputMode="numeric" value={str} placeholder={field.placeholder}
          onChange={(e) => onChange(e.target.value.replace(/\D/g, '').slice(0, field.length))} />
      );
      break;
    case 'uf':
      input = (
        <input id={id} type="text" value={str} maxLength={2} placeholder={field.placeholder}
          onChange={(e) => onChange(e.target.value.replace(/[^A-Za-z]/g, '').toUpperCase())} />
      );
      break;
    default:
      input = (
        <input id={id} type={field.type === 'number' ? 'text' : field.type} inputMode={field.type === 'number' ? 'numeric' : undefined}
          value={str} maxLength={field.maxLength} placeholder={field.placeholder} onChange={(e) => onChange(e.target.value)} />
      );
  }

  return (
    <div className={`field ${error ? 'has-error' : ''}`}>
      <label htmlFor={id}>{field.label}{field.required && <span className="req"> *</span>}</label>
      {input}
      {field.type === 'relation' && loaded && options.length === 0 && !optionsError && (
        <span className="muted small">Nenhum registro disponível. <Link to={`/${field.resource}/novo`}>Cadastrar {RESOURCES[field.resource].singular.toLowerCase()}</Link></span>
      )}
      {optionsError && <span className="error">{optionsError}</span>}
      {error && <span className="error">{error}</span>}
    </div>
  );
}

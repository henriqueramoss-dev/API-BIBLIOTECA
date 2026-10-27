import { useCallback, useEffect, useState } from 'react';
import { Link, Navigate, useParams, useSearchParams } from 'react-router-dom';
import { ApiError, list, Page, remove } from '../api/client';
import { isResourceKey, RESOURCES } from '../api/resources';
import { Icon } from '../components/Icon';

const PAGE_SIZE = 10;

export default function ResourceList() {
  const { resource: key } = useParams();
  const [params, setParams] = useSearchParams();
  const resource = isResourceKey(key) ? RESOURCES[key] : undefined;

  const page = Number(params.get('page') ?? 0);
  const term = resource ? params.get(resource.search.param) ?? '' : '';
  const [draft, setDraft] = useState(term);
  const [data, setData] = useState<Page<any>>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>();

  const load = useCallback(() => {
    if (!resource) return;
    setLoading(true);
    setError(undefined);
    list(resource.key, resource.embeddedKey, { page, size: PAGE_SIZE, search: { [resource.search.param]: term } })
      .then(setData)
      .catch((e: Error) => {
        // Página que não existe mais (endereço antigo, ou itens excluídos em outra aba): volta para a primeira.
        if ((e as ApiError).status === 404 && page > 0) {
          const next = new URLSearchParams(params);
          next.delete('page');
          setParams(next, { replace: true });
          return;
        }
        setError(e.message);
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resource, page, term]);

  useEffect(load, [load]);

  if (!resource) return <Navigate to="/" replace />;

  const applySearch = (value: string) => {
    const next = new URLSearchParams();
    if (value.trim()) next.set(resource.search.param, value.trim());
    setParams(next);
  };

  const goTo = (p: number) => {
    const next = new URLSearchParams(params);
    next.set('page', String(p));
    setParams(next);
  };

  const handleDelete = async (item: any) => {
    if (!confirm(`Excluir ${resource.singular.toLowerCase()} "${resource.title(item)}"?`)) return;
    try {
      await remove(resource.key, item.id);
      // Se era o último item da página, volta uma página (a API responde 404 para página inexistente).
      if (data && data.items.length === 1 && page > 0) goTo(page - 1);
      else load();
    } catch (e) {
      alert((e as Error).message);
    }
  };

  const columns = data?.items[0] ? resource.details(data.items[0]).map((d) => d.label) : [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{resource.label}</h1>
          <p className="muted">{data ? (data.totalElements === 1 ? '1 registro' : `${data.totalElements} registros`) : 'Carregando…'}</p>
        </div>
        <Link className="btn primary" to={`/${resource.key}/novo`}>
          <Icon name="plus" /> {resource.newLabel}
        </Link>
      </div>

      <div className="toolbar">
        {resource.search.options ? (
          <div className="chips">
            <button className={`chip ${term === '' ? 'active' : ''}`} onClick={() => applySearch('')}>Todos</button>
            {resource.search.options.map((o) => (
              <button key={o.value} className={`chip ${term === o.value ? 'active' : ''}`} onClick={() => applySearch(o.value)}>
                {o.label}
              </button>
            ))}
          </div>
        ) : (
          <form className="search" onSubmit={(e) => { e.preventDefault(); applySearch(draft); }}>
            <Icon name="search" />
            <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder={resource.search.label} />
            {term && (
              <button type="button" className="icon-btn" onClick={() => { setDraft(''); applySearch(''); }} aria-label="Limpar busca">
                <Icon name="x" />
              </button>
            )}
          </form>
        )}
      </div>

      {error && <div className="alert">{error}</div>}

      <div className="card table-card">
        {loading && !data ? (
          <p className="empty">Carregando…</p>
        ) : data && data.items.length === 0 ? (
          <p className="empty">{term ? 'Nenhum resultado para a busca.' : resource.emptyLabel}</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>{resource.singular}</th>
                {columns.map((c) => <th key={c}>{c}</th>)}
                {resource.badge && <th>Status</th>}
                <th className="actions-col" aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {data?.items.map((item) => {
                const badge = resource.badge?.(item);
                return (
                  <tr key={item.id}>
                    <td className="muted">{item.id}</td>
                    <td>
                      <Link to={`/${resource.key}/${item.id}`} className="row-title">{resource.title(item)}</Link>
                      <div className="muted small">{resource.subtitle(item)}</div>
                    </td>
                    {resource.details(item).map((d) => <td key={d.label} data-label={d.label}>{d.value}</td>)}
                    {badge && <td><span className={`badge ${badge.tone}`}>{badge.text}</span></td>}
                    <td className="actions">
                      <Link className="icon-btn" to={`/${resource.key}/${item.id}`} aria-label="Editar"><Icon name="edit" /></Link>
                      <button className="icon-btn danger" onClick={() => handleDelete(item)} aria-label="Excluir"><Icon name="trash" /></button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {data && data.totalPages > 1 && (
        <div className="pagination">
          <button className="btn" disabled={page <= 0} onClick={() => goTo(page - 1)}>Anterior</button>
          <span className="muted">Página {page + 1} de {data.totalPages}</span>
          <button className="btn" disabled={page >= data.totalPages - 1} onClick={() => goTo(page + 1)}>Próxima</button>
        </div>
      )}
    </>
  );
}

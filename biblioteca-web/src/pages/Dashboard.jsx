import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { list } from '../api/client';
import { RESOURCE_ORDER, RESOURCES, STATUS_OPTIONS } from '../api/resources';
import { Icon } from '../components/Icon';

// Página inicial: contadores, atalhos e os empréstimos mais recentes.
// Arquivo em JavaScript (.jsx): usa só o básico do React (useState e useEffect).
export default function Dashboard() {
  const [counts, setCounts] = useState({});
  const [status, setStatus] = useState({});
  const [recentes, setRecentes] = useState([]);
  const [error, setError] = useState();

  useEffect(() => {
    const emp = RESOURCES.emprestimos;
    Promise.all([
      ...RESOURCE_ORDER.map((k) =>
        list(k, RESOURCES[k].embeddedKey, { size: 1 }).then((p) => [k, p.totalElements]),
      ),
    ])
      .then((entries) => setCounts(Object.fromEntries(entries)))
      .catch((e) => setError(e.message));

    Promise.all(
      STATUS_OPTIONS.map((o) =>
        list(emp.key, emp.embeddedKey, { size: 1, search: { status: o.value } }).then((p) => [o.value, p.totalElements]),
      ),
    ).then((entries) => setStatus(Object.fromEntries(entries))).catch(() => {});

    list(emp.key, emp.embeddedKey, { size: 5, sort: 'id,desc' })
      .then((p) => setRecentes(p.items))
      .catch(() => {});
  }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Início</h1>
          <p className="muted">Visão geral do acervo e dos empréstimos.</p>
        </div>
        <Link className="btn primary" to="/emprestimos/novo">
          <Icon name="plus" /> Novo empréstimo
        </Link>
      </div>

      {error && <div className="alert">{error}</div>}

      <section className="stats">
        {STATUS_OPTIONS.map((o) => (
          <Link key={o.value} to={`/emprestimos?status=${o.value}`} className={`stat tone-${o.value.toLowerCase()}`}>
            <span className="stat-label">Empréstimos {o.label.toLowerCase()}s</span>
            <span className="stat-value">{status[o.value] ?? '–'}</span>
          </Link>
        ))}
      </section>

      <section className="tiles">
        {RESOURCE_ORDER.map((k) => (
          <Link key={k} to={`/${k}`} className="tile">
            <span className="tile-icon"><Icon name={k} size={22} /></span>
            <span>
              <span className="tile-value">{counts[k] ?? '–'}</span>
              <span className="tile-label">{RESOURCES[k].label}</span>
            </span>
          </Link>
        ))}
      </section>

      <section className="card">
        <div className="card-header">
          <h2>Empréstimos recentes</h2>
          <Link to="/emprestimos">Ver todos</Link>
        </div>
        {recentes.length === 0 ? (
          <p className="empty">Nenhum empréstimo registrado ainda.</p>
        ) : (
          <ul className="recent">
            {recentes.map((e) => {
              const badge = RESOURCES.emprestimos.badge(e);
              return (
                <li key={e.id}>
                  <Link to={`/emprestimos/${e.id}`}>
                    <span>
                      <strong>{e.livro?.titulo}</strong>
                      <span className="muted"> · {e.usuario?.nome}</span>
                    </span>
                    <span className={`badge ${badge.tone}`}>{badge.text}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        )}
      </section>
    </>
  );
}

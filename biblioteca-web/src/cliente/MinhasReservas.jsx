import { useCallback, useEffect, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { cancelarReserva, list } from '../api/client';
import { RESOURCES } from '../api/resources';
import { useAuth } from '../auth/AuthContext';
import CapaDoLivro from '../components/CapaDoLivro';
import { PilhaDeLivros } from '../components/ilustracoes';
import { formatDate, today } from '../utils/datas';
import { autoresDe } from './useReservar';

// Reservas e empréstimos de quem está logado.
function situacao(e) {
  if (e.status === 'RESERVADO') {
    if (e.retirarAte && e.retirarAte < today()) return { texto: 'Prazo de retirada vencido', tom: 'danger', detalhe: `Era até ${formatDate(e.retirarAte)}` };
    return { texto: 'Aguardando retirada', tom: 'info', detalhe: `Retire na biblioteca até ${formatDate(e.retirarAte)}` };
  }
  if (e.status === 'ATIVO') return { texto: 'Emprestado', tom: 'ok', detalhe: `Devolver até ${formatDate(e.dataPrevistaDevolucao)}` };
  if (e.status === 'ATRASADO') return { texto: 'Devolução atrasada', tom: 'danger', detalhe: `Era até ${formatDate(e.dataPrevistaDevolucao)}` };
  return { texto: 'Devolvido', tom: 'warn', detalhe: `Em ${formatDate(e.dataDevolucao)}` };
}

export default function MinhasReservas() {
  const { sessao } = useAuth();
  const [page, setPage] = useState(0);
  const [dados, setDados] = useState();
  const [erro, setErro] = useState();
  const [carregando, setCarregando] = useState(true);

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(undefined);
    list('reservas/minhas', RESOURCES.emprestimos.embeddedKey, { page, size: 10, sort: 'id,desc' })
      .then(setDados)
      .catch((e) => {
        if (e.status === 404 && page > 0) setPage(page - 1);
        else setErro(e.message);
      })
      .finally(() => setCarregando(false));
  }, [page]);

  useEffect(() => { if (sessao) carregar(); }, [sessao, carregar]);

  if (!sessao) return <Navigate to="/entrar?de=%2Fminhas-reservas" replace />;

  const cancelar = async (e) => {
    if (!confirm(`Cancelar a reserva de "${e.livro?.titulo}"?`)) return;
    try {
      await cancelarReserva(e.id);
      carregar();
    } catch (err) {
      setErro(err.message);
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Minhas reservas</h1>
          <p className="muted">Livros que você reservou ou pegou emprestados.</p>
        </div>
        <Link className="btn primary" to="/">Escolher um livro</Link>
      </div>

      {erro && <div className="alert" role="alert">{erro}</div>}

      {carregando && !dados ? (
        <p className="empty">Carregando…</p>
      ) : dados && dados.items.length === 0 ? (
        <div className="card empty-state">
          <PilhaDeLivros width={170} />
          <p>Você ainda não reservou nenhum livro.</p>
          <Link className="btn primary" to="/">Ver o catálogo</Link>
        </div>
      ) : (
        <div className="reservas-lista">
          {dados?.items.map((e) => {
            const s = situacao(e);
            return (
              <article key={e.id} className="card reserva-card">
                <Link to={`/livro/${e.livro?.id}`}><CapaDoLivro livro={e.livro} width={64} /></Link>
                <div className="reserva-info">
                  <Link to={`/livro/${e.livro?.id}`} className="livro-card-titulo">{e.livro?.titulo}</Link>
                  <span className="muted small">{autoresDe(e.livro)}</span>
                  <span className={`badge ${s.tom}`}>{s.texto}</span>
                  <span className="muted small">{s.detalhe}</span>
                </div>
                {e.status === 'RESERVADO' && (
                  <button className="btn" onClick={() => cancelar(e)}>Cancelar reserva</button>
                )}
              </article>
            );
          })}
        </div>
      )}

      {dados && dados.totalPages > 1 && (
        <div className="pagination">
          <button className="btn" disabled={page <= 0} onClick={() => setPage(page - 1)}>Anterior</button>
          <span className="muted">Página {page + 1} de {dados.totalPages}</span>
          <button className="btn" disabled={page >= dados.totalPages - 1} onClick={() => setPage(page + 1)}>Próxima</button>
        </div>
      )}
    </>
  );
}

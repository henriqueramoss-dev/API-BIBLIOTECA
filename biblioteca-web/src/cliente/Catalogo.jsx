import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { list } from '../api/client';
import { RESOURCES } from '../api/resources';
import CapaDoLivro from '../components/CapaDoLivro';
import { Icon } from '../components/Icon';
import { PilhaDeLivros } from '../components/ilustracoes';
import { autoresDe, useReservar } from './useReservar';

const TAMANHO = 12;

// Página inicial do cliente: escolher um livro do acervo e reservá-lo para retirar na biblioteca.
export default function Catalogo() {
  const [params, setParams] = useSearchParams();
  const page = Number(params.get('page') ?? 0);
  const termo = params.get('titulo') ?? '';
  const categoria = params.get('categoria') ?? '';
  const [rascunho, setRascunho] = useState(termo);
  const [categorias, setCategorias] = useState([]);
  const [dados, setDados] = useState();
  const [indisponiveis, setIndisponiveis] = useState(new Set());
  const [erro, setErro] = useState();
  const [carregando, setCarregando] = useState(true);

  // Cada livro já vem com a "situacao" calculada pela API. Ao reservar, marca o livro na hora;
  // se a reserva falhar (alguém reservou antes), recarrega a página para mostrar a situação atual.
  const { reservar, reservando, aviso, erro: erroReserva } = useReservar((id) => {
    if (id) setIndisponiveis((atual) => new Set(atual).add(id));
    else carregar();
  });

  useEffect(() => {
    list('categorias', RESOURCES.categorias.embeddedKey, { size: 100 }).then((p) => setCategorias(p.items)).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(undefined);
    const embedded = RESOURCES.livros.embeddedKey;
    const consulta = categoria
      ? list(`categorias/${categoria}/livros`, embedded, { page, size: TAMANHO, sort: 'titulo' })
      : list('livros', embedded, { page, size: TAMANHO, sort: 'titulo', search: { titulo: termo } });
    consulta
      .then((pagina) => {
        setDados(pagina);
        setIndisponiveis(new Set(pagina.items.filter((l) => l.situacao && l.situacao !== 'DISPONIVEL').map((l) => l.id)));
      })
      .catch((e) => {
        // Página que não existe mais: volta para a primeira.
        if (e.status === 404 && page > 0) {
          const proximo = new URLSearchParams(params);
          proximo.delete('page');
          setParams(proximo, { replace: true });
          return;
        }
        setErro(e.message);
      })
      .finally(() => setCarregando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, termo, categoria]);

  useEffect(carregar, [carregar]);

  const buscar = (valor) => {
    const proximo = new URLSearchParams();
    if (valor.trim()) proximo.set('titulo', valor.trim());
    setParams(proximo);
  };
  const escolherCategoria = (id) => {
    setRascunho('');
    const proximo = new URLSearchParams();
    if (id) proximo.set('categoria', id);
    setParams(proximo);
  };
  const irParaPagina = (p) => {
    const proximo = new URLSearchParams(params);
    proximo.set('page', String(p));
    setParams(proximo);
  };

  return (
    <>
      <section className="cliente-hero">
        <div>
          <p className="hero-eyebrow">BIBLIOTECA</p>
          <h1>Escolha seu próximo livro</h1>
          <p className="muted">Reserve pelo site e retire na biblioteca em até 3 dias.</p>
        </div>
        <form className="search cliente-busca" onSubmit={(e) => { e.preventDefault(); buscar(rascunho); }}>
          <Icon name="search" />
          <input value={rascunho} onChange={(e) => setRascunho(e.target.value)} placeholder="Buscar por título" aria-label="Buscar por título" />
          {termo && (
            <button type="button" className="icon-btn" onClick={() => { setRascunho(''); buscar(''); }} aria-label="Limpar busca">
              <Icon name="x" />
            </button>
          )}
        </form>
      </section>

      {categorias.length > 0 && (
        <div className="chips cliente-categorias">
          <button className={`chip ${!categoria ? 'active' : ''}`} onClick={() => escolherCategoria('')}>Todos</button>
          {categorias.map((c) => (
            <button key={c.id} className={`chip ${String(c.id) === categoria ? 'active' : ''}`} onClick={() => escolherCategoria(String(c.id))}>
              {c.nome}
            </button>
          ))}
        </div>
      )}

      {aviso && (
        <div className="alert ok-alert" role="status">
          {aviso} <Link to="/minhas-reservas">Ver minhas reservas</Link>
        </div>
      )}
      {(erro || erroReserva) && <div className="alert" role="alert">{erro || erroReserva}</div>}

      {carregando && !dados ? (
        <p className="empty">Carregando…</p>
      ) : dados && dados.items.length === 0 ? (
        <div className="card empty-state">
          <PilhaDeLivros width={170} />
          <p>{termo || categoria ? 'Nenhum livro encontrado.' : 'O acervo ainda não tem livros.'}</p>
        </div>
      ) : (
        <div className="livros-grade">
          {dados?.items.map((livro) => {
            const fora = indisponiveis.has(livro.id);
            return (
              <article key={livro.id} className="livro-card">
                <Link to={`/livro/${livro.id}`} className="livro-card-capa" aria-label={`Ver ${livro.titulo}`}>
                  <CapaDoLivro livro={livro} width={110} />
                </Link>
                <div className="livro-card-info">
                  <Link to={`/livro/${livro.id}`} className="livro-card-titulo">{livro.titulo}</Link>
                  <span className="muted small">{autoresDe(livro)}</span>
                  <span className="muted small">{livro.categoria?.nome}</span>
                </div>
                <span className={`badge ${fora ? 'warn' : 'ok'}`}>{fora ? 'Indisponível' : 'Disponível'}</span>
                <button className="btn primary" disabled={fora || reservando === livro.id} onClick={() => reservar(livro)}>
                  {reservando === livro.id ? 'Reservando…' : 'Reservar'}
                </button>
              </article>
            );
          })}
        </div>
      )}

      {dados && dados.totalPages > 1 && (
        <div className="pagination">
          <button className="btn" disabled={page <= 0} onClick={() => irParaPagina(page - 1)}>Anterior</button>
          <span className="muted">Página {page + 1} de {dados.totalPages}</span>
          <button className="btn" disabled={page >= dados.totalPages - 1} onClick={() => irParaPagina(page + 1)}>Próxima</button>
        </div>
      )}
    </>
  );
}

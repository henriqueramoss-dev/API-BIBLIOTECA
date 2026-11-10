import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { get } from '../api/client';
import CapaDoLivro from '../components/CapaDoLivro';
import { Icon } from '../components/Icon';
import { autoresDe, useReservar } from './useReservar';

// Detalhes de um livro, com o botão de reservar.
export default function LivroDetalhe() {
  const { id } = useParams();
  const [livro, setLivro] = useState();
  const [fora, setFora] = useState(false);
  const [erro, setErro] = useState();
  const carregar = useCallback(() => {
    get('livros', Number(id))
      .then((l) => {
        setLivro(l);
        setFora(!!l.situacao && l.situacao !== 'DISPONIVEL');
      })
      .catch((e) => setErro(e.message));
  }, [id]);

  // A situação vem no próprio livro. Se a reserva falhar (alguém reservou antes), recarrega para mostrar a atual.
  const { reservar, reservando, aviso, erro: erroReserva } = useReservar((livroId) => {
    if (livroId) setFora(true);
    else carregar();
  });

  useEffect(carregar, [carregar]);

  return (
    <>
      <Link to="/" className="back"><Icon name="back" /> Catálogo</Link>
      {erro && <div className="alert" role="alert">{erro}</div>}
      {!livro && !erro && <p className="empty">Carregando…</p>}
      {livro && (
        <article className="card livro-detalhe">
          <div className="livro-detalhe-capa"><CapaDoLivro livro={livro} width={180} /></div>
          <div className="livro-detalhe-info">
            <h1>{livro.titulo}</h1>
            <p className="muted">{autoresDe(livro)}</p>
            <span className={`badge ${fora ? 'warn' : 'ok'}`}>{fora ? 'Indisponível no momento' : 'Disponível para reserva'}</span>
            <dl className="ficha">
              <div><dt>Categoria</dt><dd>{livro.categoria?.nome ?? '—'}</dd></div>
              {livro.categoria?.localizacao && <div><dt>Onde encontrar</dt><dd>{livro.categoria.localizacao}</dd></div>}
              <div><dt>Editora</dt><dd>{livro.editora}</dd></div>
              <div><dt>Ano</dt><dd>{livro.anoPublicacao}</dd></div>
              <div><dt>Páginas</dt><dd>{livro.numeroPaginas}</dd></div>
              <div><dt>ISBN</dt><dd>{livro.isbn}</dd></div>
            </dl>
            {aviso && <div className="alert ok-alert" role="status">{aviso} <Link to="/minhas-reservas">Ver minhas reservas</Link></div>}
            {erroReserva && <div className="alert" role="alert">{erroReserva}</div>}
            <div className="livro-detalhe-acoes">
              <button className="btn primary" disabled={fora || reservando === livro.id} onClick={() => reservar(livro)}>
                {reservando === livro.id ? 'Reservando…' : fora ? 'Indisponível' : 'Reservar para retirar na biblioteca'}
              </button>
              <span className="muted small">Você terá 3 dias para retirar. Cada pessoa pode ter até 3 reservas.</span>
            </div>
          </div>
        </article>
      )}
    </>
  );
}

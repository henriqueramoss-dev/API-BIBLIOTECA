import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { reservarLivro } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { formatDate } from '../utils/datas';

// Reservar um livro: sem login leva para a tela de entrada e volta depois.
// onFeito(livroId) avisa a tela que o livro acabou de ficar indisponível (ou, sem id, que deu erro e vale recarregar).
export function useReservar(onFeito) {
  const { sessao } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [reservando, setReservando] = useState(null);
  const [aviso, setAviso] = useState();
  const [erro, setErro] = useState();

  const reservar = async (livro) => {
    if (!sessao) {
      navigate(`/entrar?de=${encodeURIComponent(location.pathname + location.search)}`);
      return;
    }
    setReservando(livro.id);
    setAviso(undefined);
    setErro(undefined);
    try {
      const reserva = await reservarLivro(livro.id);
      setAviso(`Reserva feita! Retire "${livro.titulo}" na biblioteca até ${formatDate(reserva.retirarAte)}.`);
      onFeito?.(livro.id);
    } catch (e) {
      setErro(e.message);
      onFeito?.();
    } finally {
      setReservando(null);
    }
  };

  return { reservar, reservando, aviso, erro };
}

export const autoresDe = (livro) => (livro.autores ?? []).map((a) => a.nome).join(', ') || 'Autor não informado';

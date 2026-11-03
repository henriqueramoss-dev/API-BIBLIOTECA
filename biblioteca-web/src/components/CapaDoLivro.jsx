import { useState } from 'react';
import { CapaDeLivro, corDaCapa } from './ilustracoes';
import { urlDaCapa } from '../utils/capas';

// Para desligar as capas reais (nenhuma consulta ao Open Library), crie o arquivo .env com VITE_CAPAS_REAIS=false
const CAPAS_REAIS = import.meta.env.VITE_CAPAS_REAIS !== 'false';

// Capa do livro: a imagem real (pelo ISBN) ou, se não houver, a capa ilustrada.
export default function CapaDoLivro({ livro, width = 30 }) {
  const [falhou, setFalhou] = useState(false);
  const url = CAPAS_REAIS ? urlDaCapa(livro.isbn) : null;

  if (!url || falhou) return <CapaDeLivro cor={corDaCapa(livro.id)} width={width} />;

  return (
    <img
      className="capa-real"
      src={url}
      alt=""
      width={width}
      height={Math.round((width * 84) / 60)}
      loading="lazy"
      referrerPolicy="no-referrer"
      onLoad={(e) => { if (e.currentTarget.naturalWidth <= 1) setFalhou(true); }}
      onError={() => setFalhou(true)}
    />
  );
}

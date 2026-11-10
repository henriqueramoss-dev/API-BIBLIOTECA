import { Link, NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Icon } from '../components/Icon';
import { FaixaDeLombadas } from '../components/ilustracoes';
import Cadastro from '../pages/Cadastro';
import Entrar from '../pages/Entrar';
import Catalogo from './Catalogo';
import LivroDetalhe from './LivroDetalhe';
import MinhasReservas from './MinhasReservas';

// Telas de quem usa a biblioteca (cliente ou visitante): catálogo, detalhes do livro e minhas reservas.
// A equipe da biblioteca (tipo FUNCIONARIO) vê o painel de gestão no lugar (ver App.jsx).
export default function AreaDoCliente() {
  const { sessao, sair } = useAuth();

  return (
    <div className="cliente">
      <header className="cliente-topo">
        <Link to="/" className="cliente-marca"><Icon name="book" /> Biblioteca</Link>
        <nav className="cliente-nav" aria-label="Principal">
          <NavLink to="/" end>Catálogo</NavLink>
          <NavLink to="/minhas-reservas">Minhas reservas</NavLink>
        </nav>
        <div className="cliente-conta">
          {sessao ? (
            <>
              <span className="cliente-nome">Olá, {sessao.nome.split(' ')[0]}</span>
              <button type="button" className="btn" onClick={sair}>Sair</button>
            </>
          ) : (
            <>
              <Link className="btn" to="/cadastro">Criar conta</Link>
              <Link className="btn primary" to="/entrar">Entrar</Link>
            </>
          )}
        </div>
      </header>
      <div className="shelf-banner"><FaixaDeLombadas height={44} /></div>
      <main className="cliente-conteudo">
        <Routes>
          <Route path="/" element={<Catalogo />} />
          <Route path="/livro/:id" element={<LivroDetalhe />} />
          <Route path="/minhas-reservas" element={<MinhasReservas />} />
          <Route path="/entrar" element={<Entrar />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}

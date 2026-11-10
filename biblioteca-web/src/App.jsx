import { useState } from 'react';
import { Link, NavLink, Navigate, Route, Routes, useLocation, useParams } from 'react-router-dom';
import { RESOURCE_ORDER, RESOURCES } from './api/resources';
import { exigeLogin, useAuth } from './auth/AuthContext';
import { Icon } from './components/Icon';
import { Estante, FaixaDeLombadas } from './components/ilustracoes';
import Cadastro from './pages/Cadastro';
import Dashboard from './pages/Dashboard';
import Entrar from './pages/Entrar';
import ResourceForm from './pages/ResourceForm';
import ResourceList from './pages/ResourceList';

// Exige login: as listas de dados pessoais e todos os formulários (cadastrar, editar) só abrem com sessão.
function Guarda({ modo, children }) {
  const { sessao } = useAuth();
  const { resource } = useParams();
  const location = useLocation();
  const precisaLogin = modo === 'form' || exigeLogin(resource);
  if (precisaLogin && !sessao) {
    return <Navigate to={`/entrar?de=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  }
  return children;
}

const NIVEIS = { LEITURA: 'Leitura', ESCRITA: 'Escrita', ADMIN: 'Administrador' };

// Estrutura da página: menu lateral, barra de topo e as rotas.
// Arquivo em JavaScript (.jsx).
export default function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const { sessao, sair } = useAuth();

  return (
    <div className="layout">
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`} onClick={() => setMenuOpen(false)}>
        <div className="brand">
          <Icon name="book" /> Biblioteca
        </div>
        <p className="brand-sub">Acervo e empréstimos</p>
        <nav>
          <NavLink to="/" end>
            <Icon name="home" /> Início
          </NavLink>
          {RESOURCE_ORDER.map((key) => (
            <NavLink key={key} to={`/${key}`}>
              <Icon name={key} /> {RESOURCES[key].label}
            </NavLink>
          ))}
        </nav>
        <div className="sessao" onClick={(e) => e.stopPropagation()}>
          {sessao ? (
            <>
              <span className="sessao-nome">{sessao.nome}</span>
              <span className="sessao-nivel">Acesso: {NIVEIS[sessao.nivel] ?? sessao.nivel}</span>
              <button type="button" className="btn-sair" onClick={sair}>Sair</button>
            </>
          ) : (
            <>
              <Link className="btn-entrar" to="/entrar" onClick={() => setMenuOpen(false)}>Entrar</Link>
              <Link className="sessao-link" to="/cadastro" onClick={() => setMenuOpen(false)}>Criar conta</Link>
            </>
          )}
        </div>
        <div className="sidebar-art">
          <Estante width={212} />
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setMenuOpen((o) => !o)} aria-label="Abrir menu">
            <Icon name="menu" />
          </button>
          <span className="topbar-title">Biblioteca</span>
        </header>
        <div className="shelf-banner">
          <FaixaDeLombadas height={44} />
        </div>
        <main className="content" key={location.pathname}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/entrar" element={<Entrar />} />
            <Route path="/cadastro" element={<Cadastro />} />
            <Route path="/:resource" element={<Guarda modo="lista"><ResourceList /></Guarda>} />
            <Route path="/:resource/novo" element={<Guarda modo="form"><ResourceForm /></Guarda>} />
            <Route path="/:resource/:id" element={<Guarda modo="form"><ResourceForm /></Guarda>} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

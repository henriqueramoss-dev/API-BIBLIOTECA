import { useState } from 'react';
import { NavLink, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { RESOURCE_ORDER, RESOURCES } from './api/resources';
import { Icon } from './components/Icon';
import Dashboard from './pages/Dashboard';
import ResourceForm from './pages/ResourceForm';
import ResourceList from './pages/ResourceList';

// Estrutura da página: menu lateral, barra de topo e as rotas.
// Arquivo em JavaScript (.jsx).
export default function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();

  return (
    <div className="layout">
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`} onClick={() => setMenuOpen(false)}>
        <div className="brand">
          <Icon name="book" /> Biblioteca
        </div>
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
      </aside>

      <div className="main">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setMenuOpen((o) => !o)} aria-label="Abrir menu">
            <Icon name="menu" />
          </button>
          <span className="topbar-title">Biblioteca</span>
        </header>
        <main className="content" key={location.pathname}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/:resource" element={<ResourceList />} />
            <Route path="/:resource/novo" element={<ResourceForm />} />
            <Route path="/:resource/:id" element={<ResourceForm />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

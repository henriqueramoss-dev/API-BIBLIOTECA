import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { setBaseUrl } from './api/client';
import App from './App';
import './styles.css';

setBaseUrl(import.meta.env.VITE_API_URL ?? 'http://localhost:8080');

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);

import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { setApiKey, setBaseUrl } from './api/client';
import { AuthProvider } from './auth/AuthContext';
import App from './App';
import './styles.css';

setBaseUrl(import.meta.env.VITE_API_URL ?? 'https://api-biblioteca-xgsa.onrender.com');
setApiKey(import.meta.env.VITE_API_KEY);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
);

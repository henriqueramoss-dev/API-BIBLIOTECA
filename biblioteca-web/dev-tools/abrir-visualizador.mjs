// Sobe o site em modo de desenvolvimento e abre o visualizador de dispositivos no navegador.
import { spawn } from 'node:child_process';

const porta = process.env.PORT ?? '5173';
const endereco = `http://localhost:${porta}/dev-tools/dispositivos.html`;

const vite = spawn('npx', ['vite', '--port', porta, '--strictPort'], { stdio: 'inherit', shell: true });
vite.on('exit', (codigo) => process.exit(codigo ?? 0));

async function esperar() {
  for (let i = 0; i < 60; i++) {
    try { if ((await fetch(endereco)).ok) return true; } catch { /* ainda subindo */ }
    await new Promise((r) => setTimeout(r, 500));
  }
  return false;
}

if (await esperar()) {
  console.log(`\nVisualizador de dispositivos: ${endereco}\n`);
  const abrir = process.platform === 'win32' ? ['cmd', ['/c', 'start', '', endereco]] : process.platform === 'darwin' ? ['open', [endereco]] : ['xdg-open', [endereco]];
  try { spawn(abrir[0], abrir[1], { stdio: 'ignore', detached: true }).on('error', () => {}).unref(); } catch { /* sem navegador padrão */ }
} else {
  console.error('O site não subiu a tempo.');
}

import { defineConfig, devices } from '@playwright/test';

/**
 * Testes de navegador do site (Playwright).
 *
 * A API é SIMULADA (e2e/api-simulada.ts): os testes são rápidos, não dependem da Render
 * (que dorme no plano gratuito) e não mexem em dados reais.
 */
const porta = 5174;

// Os aparelhos simulados (iPhone, iPad) pedem o WebKit; aqui todos rodam no Chromium, que já é o único navegador
// que precisa estar instalado. Para testar no WebKit de verdade: E2E_NAVEGADOR=webkit (e npx playwright install webkit).
const navegador = { defaultBrowserType: (process.env.E2E_NAVEGADOR ?? 'chromium') as 'chromium' | 'webkit' | 'firefox' };

export default defineConfig({
  testDir: './e2e',
  outputDir: './e2e/resultados',
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'e2e/relatorio' }]],
  timeout: 45_000,
  expect: { timeout: 10_000 },
  fullyParallel: true,
  retries: 0,
  use: {
    baseURL: `http://localhost:${porta}`,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off',
    locale: 'pt-BR',
  },
  // Cada "projeto" é um tamanho de tela: o mesmo teste roda em todos.
  projects: [
    { name: 'celular-pequeno', use: { ...devices['iPhone SE'], ...navegador } },
    { name: 'celular', use: { ...devices['Pixel 7'], ...navegador } },
    { name: 'tablet', use: { ...devices['iPad (gen 7)'], ...navegador } },
    { name: 'notebook', use: { viewport: { width: 1280, height: 720 } } },
    { name: 'desktop', use: { viewport: { width: 1920, height: 1080 } } },
  ],
  webServer: {
    command: `npx vite --port ${porta} --strictPort`,
    url: `http://localhost:${porta}`,
    reuseExistingServer: true,
    timeout: 60_000,
    env: { VITE_API_URL: process.env.E2E_API_URL ?? 'https://api-biblioteca-xgsa.onrender.com' },
  },
});

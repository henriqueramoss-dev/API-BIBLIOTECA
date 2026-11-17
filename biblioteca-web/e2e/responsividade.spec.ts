import { expect, test } from '@playwright/test';
import { entrarNoSite, simularApi } from './api-simulada';

// Roda em todos os tamanhos de tela definidos em playwright.config.ts (celular pequeno até desktop).
// Confere o que costuma quebrar: rolagem horizontal, botões fora da tela e o menu do painel.

const semRolagemHorizontal = async (page) => {
  const sobra = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(sobra, 'a página não pode rolar para o lado').toBeLessThanOrEqual(1);
};

test.describe('Responsividade', () => {
  test.beforeEach(async ({ page }) => { await simularApi(page); });

  for (const [nome, rota] of [['catálogo', '/'], ['entrar', '/entrar'], ['criar conta', '/cadastro'], ['ficha do livro', '/livro/1']] as const) {
    test(`${nome}: cabe na tela`, async ({ page }, info) => {
      await page.goto(rota);
      await page.waitForLoadState('networkidle');
      await semRolagemHorizontal(page);
      await page.screenshot({ path: `e2e/capturas/${info.project.name}-${nome.replace(/ /g, '-')}.png`, fullPage: true });
    });
  }

  test('painel do dono: cabe na tela e o menu funciona', async ({ page }, info) => {
    await entrarNoSite(page, 'dono@teste.com', 'Senha12345');
    await expect(page.getByText('Acesso: Administrador')).toBeAttached();
    await page.waitForLoadState('networkidle');
    await semRolagemHorizontal(page);
    const botaoMenu = page.getByRole('button', { name: 'Abrir menu' });
    if (await botaoMenu.isVisible()) {
      await expect(page.locator('aside.sidebar')).not.toHaveClass(/open/);
      await botaoMenu.click();
      await expect(page.locator('aside.sidebar')).toHaveClass(/open/);
    } else {
      await expect(page.locator('aside nav')).toBeVisible();
    }
    await page.screenshot({ path: `e2e/capturas/${info.project.name}-painel.png`, fullPage: true });
  });
});

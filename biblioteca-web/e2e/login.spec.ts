import { expect, test } from '@playwright/test';
import { entrarNoSite, simularApi } from './api-simulada';

test.describe('Login e cadastro', () => {
  test.beforeEach(async ({ page }) => { await simularApi(page); });

  test('campos vazios mostram aviso e não chamam a API', async ({ page }) => {
    const chamadas = await simularApi(page);
    await page.goto('/entrar');
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page.getByRole('alert')).toHaveText('Informe o e-mail e a senha.');
    expect(chamadas.filter((c) => c.caminho === '/chaves-api')).toHaveLength(0);
  });

  test('senha errada mostra a mensagem da API', async ({ page }) => {
    await entrarNoSite(page, 'cliente@teste.com', 'errada');
    await expect(page.getByRole('alert')).toHaveText('E-mail ou senha incorretos.');
    await expect(page).toHaveURL(/\/entrar/);
  });

  test('cliente entra, vê o catálogo e sai', async ({ page }) => {
    await entrarNoSite(page, 'cliente@teste.com', 'Senha12345');
    await expect(page.getByRole('heading', { name: 'Escolha seu próximo livro' })).toBeVisible();
    const sair = page.getByRole('button', { name: 'Sair' });
    await expect(sair).toBeVisible();
    await sair.click();
    await expect(page.getByRole('button', { name: 'Sair' })).toHaveCount(0);
  });

  test('cliente reserva um livro', async ({ page }) => {
    const chamadas = await simularApi(page);
    await entrarNoSite(page, 'cliente@teste.com', 'Senha12345');
    await page.getByRole('button', { name: 'Reservar' }).first().click();
    await expect.poll(() => chamadas.some((c) => c.metodo === 'POST' && c.caminho === '/reservas')).toBeTruthy();
    const reserva = chamadas.find((c) => c.caminho === '/reservas');
    expect(reserva?.chave).toBe('bib_simulada_cliente'); // usa a chave da pessoa, não a do catálogo
  });

  test('dono entra e cai no painel de gestão', async ({ page }) => {
    await entrarNoSite(page, 'dono@teste.com', 'Senha12345');
    await expect(page.getByText('Acesso: Administrador')).toBeAttached();
    await expect(page.getByRole('link', { name: /Livros/ }).first()).toBeAttached();
  });

  test('cadastro valida a confirmação de senha', async ({ page }) => {
    await page.goto('/cadastro');
    await page.locator('#cad-senha').fill('Senha12345');
    await page.locator('#cad-confirmacao').fill('outra-coisa');
    await page.getByRole('button', { name: /Criar|Cadastrar/ }).click();
    await expect(page.getByRole('alert').or(page.locator('.field-error, .error, .alert')).first()).toBeVisible();
  });
});

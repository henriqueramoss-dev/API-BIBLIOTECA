import { expect, test } from '@playwright/test';
import { simularApi } from './api-simulada';

test.describe('Visitante (sem login)', () => {
  test.beforeEach(async ({ page }) => { await simularApi(page); });

  test('vê o catálogo, as categorias e os livros', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Escolha seu próximo livro' })).toBeVisible();
    await expect(page.getByText('Dom Casmurro')).toBeVisible();
    await expect(page.getByText('Clean Code na prática')).toBeVisible();
    await expect(page.getByText('Indisponível').first()).toBeVisible();
  });

  test('envia a chave do catálogo na leitura', async ({ page }) => {
    const chamadas = await simularApi(page);
    await page.goto('/');
    await expect(page.getByText('Dom Casmurro')).toBeVisible();
    const leitura = chamadas.find((c) => c.caminho === '/livros');
    expect(leitura?.chave).toMatch(/^bib_/);
  });

  test('busca por título filtra a lista', async ({ page }) => {
    await page.goto('/');
    await page.getByLabel('Buscar por título').fill('clean');
    await page.keyboard.press('Enter');
    await expect(page.getByText('Clean Code na prática')).toBeVisible();
    await expect(page.getByText('Dom Casmurro')).toHaveCount(0);
  });

  test('abre a ficha de um livro e volta', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'Ver Dom Casmurro' }).click();
    await expect(page).toHaveURL(/\/livro\/1$/);
    await expect(page.getByRole('heading', { name: 'Dom Casmurro' })).toBeVisible();
    await page.goBack();
    await expect(page.getByRole('heading', { name: 'Escolha seu próximo livro' })).toBeVisible();
  });

  test('reservar sem estar logado leva ao login', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Reservar' }).first().click();
    await expect(page).toHaveURL(/\/entrar\?de=/);
    await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
  });

  test('navega entre catálogo, entrar e criar conta', async ({ page }) => {
    await page.goto('/entrar');
    await page.getByRole('main').getByRole('link', { name: 'Criar conta' }).click();
    await expect(page).toHaveURL(/\/cadastro$/);
    await expect(page.getByRole('heading', { name: 'Criar conta' })).toBeVisible();
  });
});

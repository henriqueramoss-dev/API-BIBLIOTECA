import { expect, test } from '@playwright/test';
import { entrarNoSite, simularApi } from './api-simulada';

// No celular o menu lateral fica escondido atrás do botão "Abrir menu".
async function abrirMenuSePreciso(page) {
  const botao = page.getByRole('button', { name: 'Abrir menu' });
  if (await botao.isVisible()) await botao.click();
}

test.describe('Dono da biblioteca (painel)', () => {
  test.beforeEach(async ({ page }) => {
    await simularApi(page);
    await entrarNoSite(page, 'dono@teste.com', 'Senha12345');
    await expect(page.getByText('Acesso: Administrador')).toBeAttached();
  });

  test('navega pelas listagens do menu', async ({ page }) => {
    for (const [nome, rota] of [['Livros', '/livros'], ['Autores', '/autores'], ['Categorias', '/categorias'], ['Usuários', '/usuarios'], ['Carteirinhas', '/carteirinhas'], ['Empréstimos', '/emprestimos']] as const) {
      await abrirMenuSePreciso(page);
      await page.locator('aside nav').getByRole('link', { name: new RegExp(nome) }).click();
      await expect(page).toHaveURL(new RegExp(`${rota}$`));
    }
  });

  test('abre o formulário de novo livro e valida os campos obrigatórios', async ({ page }) => {
    await page.goto('/livros/novo');
    await expect(page.getByRole('heading', { name: 'Novo livro' })).toBeVisible();
    await page.getByRole('button', { name: /Salvar|Cadastrar|Criar/ }).click();
    // Sem preencher nada, o formulário não pode ter sido enviado.
    await expect(page).toHaveURL(/\/livros\/novo$/);
  });

  test('preenche o título do formulário de livro', async ({ page }) => {
    await page.goto('/livros/novo');
    await page.getByLabel(/Título/).fill('Livro de teste');
    await expect(page.getByLabel(/Título/)).toHaveValue('Livro de teste');
  });

  test('sair volta para a área pública', async ({ page }) => {
    await abrirMenuSePreciso(page);
    await page.getByRole('button', { name: 'Sair' }).click();
    await expect(page.getByRole('heading', { name: 'Escolha seu próximo livro' })).toBeVisible();
  });
});

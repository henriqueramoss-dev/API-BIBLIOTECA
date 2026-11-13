# Ambiente de visualização e testes das telas

Dois projetos, duas formas de testar. Nada aqui altera telas ou regras de negócio.

| Projeto | Onde | Como visualizar | Como testar |
|---|---|---|---|
| Site (React + Vite) | `biblioteca-web/` | Visualizador de dispositivos | Playwright (5 tamanhos de tela) |
| App (Expo / React Native) | repositório `REACT-NATIVE` | Emulador Android existente | Roteiros Maestro |

## 1. Site

Uma vez, dentro de `biblioteca-web/`:

```bash
npm install
npm run e2e:instalar
```

### Ver o site em vários tamanhos (interativo)

```bash
npm run preview:dispositivos
```

Sobe o site e abre `http://localhost:5173/dev-tools/dispositivos.html`: iPhone SE, Pixel 7, iPad Mini e notebook lado a lado. Dá para trocar o endereço (ou usar os atalhos), girar o aparelho, mudar o zoom, adicionar mais aparelhos e usar cada tela de verdade (entrar, reservar, preencher formulários). Esta página só existe no desenvolvimento: não vai para o site publicado.

### Rodar os testes de navegador

```bash
npm run test:e2e            # todos, nos 5 tamanhos (cerca de 1 minuto)
npm run test:e2e:ui         # painel interativo: escolha o teste, veja passo a passo e refaça
npm run test:e2e:visivel    # abre o navegador de celular e mostra os cliques acontecendo
npm run test:e2e:celular    # só os dois celulares
npm run test:e2e:relatorio  # abre o relatório HTML da última execução
```

Tamanhos (em `playwright.config.ts`): celular pequeno (iPhone SE), celular (Pixel 7), tablet (iPad), notebook 1280x720 e desktop 1920x1080. O mesmo teste roda em todos.

O que é testado (`e2e/`):
- `visitante.spec.ts`: catálogo, chave do catálogo no cabeçalho, busca, ficha do livro, reservar sem login leva ao login, navegação para criar conta.
- `login.spec.ts`: campos vazios, senha errada, cliente entra/reserva/sai, dono cai no painel, validação do cadastro.
- `dono.spec.ts`: navegação pelas listagens do menu, formulário de novo livro, sair.
- `responsividade.spec.ts`: nenhuma página rola para o lado em nenhum tamanho, e o menu do painel abre no celular. Salva capturas em `e2e/capturas/`.

A API é **simulada** (`e2e/api-simulada.ts`): os testes não dependem da Render (que dorme no plano gratuito) e não criam dados reais. Contas de teste: `dono@teste.com` e `cliente@teste.com`, senha `Senha12345`.

Quando um teste falha, a captura e o trace ficam em `e2e/resultados/`; abra o trace com `npx playwright show-trace <arquivo>`.

## 2. App no emulador Android

Pré-requisito: Android Studio com um emulador criado (More Actions > Virtual Device Manager).

No repositório do app:

```bash
npm install
npm run android:emuladores   # lista os emuladores encontrados
npm run android:emulador     # liga o emulador (se preciso) e abre o app
```

O script acha o SDK sozinho (`ANDROID_HOME`, `ANDROID_SDK_ROOT` ou a pasta padrão do Windows, Mac e Linux). Para escolher o emulador: `AVD=Nome_do_emulador npm run android:emulador`. Sem SDK ou sem emulador, ele explica o que falta.

Roteiros automáticos (Maestro, https://docs.maestro.dev/getting-started/installing-maestro), com o app aberto no emulador:

```bash
npm run teste:emulador
```

Rodam visitante e cliente contra a API publicada. O do dono pede a senha: `maestro test -e SENHA_DONO=... .maestro/03-dono.yaml`.

Para só ver o app em tamanhos de tela no navegador: `npm run navegador` e o modo dispositivo do DevTools (F12).

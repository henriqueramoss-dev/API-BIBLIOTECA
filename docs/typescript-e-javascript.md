# TypeScript e JavaScript no site

O site (`biblioteca-web`) mistura os dois de propósito, para ficar mais fácil de ler:

| Linguagem | Extensão | Onde | Por quê |
|---|---|---|---|
| **JavaScript** | `.js` e `.jsx` | Partes simples e fáceis de acompanhar | Sem tipos, só o básico do React e funções pequenas |
| **TypeScript** | `.ts` e `.tsx` | Comunicação com a API, descrição dos recursos, formulário e lista | Os tipos evitam erros em código mais complexo |

## Arquivos em JavaScript

| Arquivo | O que faz |
|---|---|
| `src/utils/datas.js` | Funções de data: `formatDate`, `isValidIsoDate` e `today` |
| `src/components/Icon.jsx` | A "tag" `<Icon name="livros" />` que desenha os ícones |
| `src/pages/Dashboard.jsx` | Página inicial (contadores e empréstimos recentes) |
| `src/App.jsx` | Menu lateral, barra de topo e rotas |

## Arquivos em TypeScript

`src/main.tsx`, `src/api/client.ts`, `src/api/resources.ts`, `src/components/useOptions.ts`, `src/pages/ResourceList.tsx` e `src/pages/ResourceForm.tsx`.

## Como funciona a mistura

- Os dois tipos de arquivo se importam normalmente. Escreva `import Dashboard from './pages/Dashboard'`, sem a extensão.
- O `tsconfig.json` tem `"allowJs": true`, que permite ao TypeScript ler arquivos `.js` e `.jsx`.
- Os arquivos JavaScript **não são checados** pelo TypeScript (não há `checkJs`). Um erro de tipo neles só aparece quando o site roda.
- Para converter um arquivo de JavaScript para TypeScript, troque a extensão (`.jsx` → `.tsx`) e acrescente os tipos que o `npx tsc -b` pedir.

## Como rodar

```
cd biblioteca-web
npm install
npm run dev
```

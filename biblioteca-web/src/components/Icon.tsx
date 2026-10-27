// Ícones SVG simples (traço), sem dependências externas.
const paths: Record<string, string> = {
  book: 'M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2V5Zm0 16a2 2 0 0 1 2-2h13v2H6',
  home: 'M3 11 12 4l9 7M5 10v10h5v-6h4v6h5V10',
  livros: 'M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2V5Zm0 16a2 2 0 0 1 2-2h13v2H6',
  autores: 'M15 4l5 5-10 10H5v-5L15 4Z',
  categorias: 'M3 12V4h8l10 10-8 8L3 12Zm5-4h.01',
  usuarios: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-8 9a8 8 0 0 1 16 0',
  perfis: 'M4 5h16v14H4zM9 11a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm-3 5a3 3 0 0 1 6 0m3-7h3m-3 4h3',
  emprestimos: 'M7 7h13l-4-4M17 17H4l4 4',
  menu: 'M4 6h16M4 12h16M4 18h16',
  plus: 'M12 5v14M5 12h14',
  edit: 'M15 4l5 5-10 10H5v-5L15 4Z',
  trash: 'M4 7h16M9 7V4h6v3m-8 0 1 13h8l1-13',
  search: 'M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14Zm5-2 4 4',
  back: 'M15 5l-7 7 7 7',
  x: 'M6 6l12 12M18 6 6 18',
};

export function Icon({ name, size = 18 }: { name: string; size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}
      strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={paths[name] ?? paths.book} />
    </svg>
  );
}

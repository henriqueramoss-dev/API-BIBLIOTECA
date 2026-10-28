// Funções simples para trabalhar com datas.
// Este arquivo é JavaScript puro (sem tipos) de propósito: são funções pequenas,
// fáceis de ler e de testar. O resto do projeto (TypeScript) importa daqui.

// Converte "2026-10-09" (formato da API) em "09/10/2026" (formato brasileiro).
export function formatDate(iso) {
  if (!iso) return '—';
  const [ano, mes, dia] = iso.split('-');
  return dia && mes && ano ? `${dia}/${mes}/${ano}` : iso;
}

// Confere o formato AAAA-MM-DD e se a data existe (31/02, por exemplo, é inválida).
export function isValidIsoDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const [ano, mes, dia] = value.split('-').map(Number);
  const data = new Date(Date.UTC(ano, mes - 1, dia));
  return data.getUTCFullYear() === ano && data.getUTCMonth() === mes - 1 && data.getUTCDate() === dia;
}

// Devolve a data de hoje em AAAA-MM-DD. Com offsetDays dá para pedir outro dia
// (today(7) = daqui a 7 dias, today(-1) = ontem).
export function today(offsetDays = 0) {
  const data = new Date();
  data.setDate(data.getDate() + offsetDays);
  const doisDigitos = (n) => String(n).padStart(2, '0');
  return `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`;
}

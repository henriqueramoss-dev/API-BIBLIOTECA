import { useEffect, useState } from 'react';
import { list } from '../api/client';
import { Option, RESOURCES, ResourceKey } from '../api/resources';

const PAGE_SIZE = 100; // máximo da API por página
const MAX_PAGES = 20; // trava de segurança: até 2000 opções

/** Carrega os registros de outro recurso para preencher selects, percorrendo todas as páginas. */
export function useOptions(key: ResourceKey | undefined) {
  const [options, setOptions] = useState<Option[]>([]);
  const [error, setError] = useState<string>();
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    if (!key) return;
    const r = RESOURCES[key];
    let cancelled = false;

    (async () => {
      try {
        const all: Option[] = [];
        for (let page = 0; page < MAX_PAGES; page++) {
          const data = await list<any>(r.key, r.embeddedKey, { page, size: PAGE_SIZE });
          all.push(...data.items.map((i) => ({ value: String(i.id), label: r.title(i) })));
          if (page >= data.totalPages - 1) break;
        }
        if (!cancelled) setOptions(all);
      } catch (e) {
        if (!cancelled) setError((e as Error).message);
      } finally {
        if (!cancelled) setLoaded(true);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [key]);

  return { options, error, loaded };
}

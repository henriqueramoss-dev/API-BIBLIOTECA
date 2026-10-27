import { useEffect, useState } from 'react';
import { list } from '../api/client';
import { Option, RESOURCES, ResourceKey } from '../api/resources';

/** Carrega os registros de outro recurso para preencher selects (até 200 itens). */
export function useOptions(key: ResourceKey | undefined) {
  const [options, setOptions] = useState<Option[]>([]);
  const [error, setError] = useState<string>();
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    if (!key) return;
    const r = RESOURCES[key];
    list<any>(r.key, r.embeddedKey, { size: 200 })
      .then((page) => setOptions(page.items.map((i) => ({ value: String(i.id), label: r.title(i) }))))
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoaded(true));
  }, [key]);

  return { options, error, loaded };
}

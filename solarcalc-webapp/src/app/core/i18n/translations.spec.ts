import de from '../../../../public/assets/i18n/de.json';
import en from '../../../../public/assets/i18n/en.json';

type Bundle = { [key: string]: string | Bundle };

/** Flattens a nested bundle to dotted keys, e.g. `solar.ks.heating.oil`. */
function flatten(bundle: Bundle, prefix = ''): Map<string, string> {
  const out = new Map<string, string>();
  for (const [key, value] of Object.entries(bundle)) {
    const path = prefix ? `${prefix}.${key}` : key;
    if (typeof value === 'string') {
      out.set(path, value);
    } else {
      for (const [k, v] of flatten(value, path)) out.set(k, v);
    }
  }
  return out;
}

const deKeys = flatten(de as Bundle);
const enKeys = flatten(en as Bundle);

describe('translation bundles', () => {
  it('define the same keys in every language', () => {
    expect([...deKeys.keys()].filter(k => !enKeys.has(k)).sort()).toEqual([]);
    expect([...enKeys.keys()].filter(k => !deKeys.has(k)).sort()).toEqual([]);
  });

  it('has no blank translation for any key', () => {
    const blank = [...deKeys, ...enKeys].filter(([, v]) => v.trim() === '').map(([k]) => k).sort();
    expect(blank).toEqual([]);
  });

  it('covers every enum value rendered through a runtime-built key', () => {
    const dynamic: Record<string, string[]> = {
      'energyClass.': ['A_PLUS', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'],
      'allocation.': ['HOUSEHOLD', 'HEAT_PUMP', 'WALLBOX'],
      'profile.heatingReferenceType_': ['NONE', 'OIL', 'GAS'],
      'solar.filter.': ['ytd', '12m', 'all', 'custom'],
    };
    const missing = Object.entries(dynamic).flatMap(([prefix, values]) =>
      values.map(value => prefix + value).filter(key => !deKeys.has(key)),
    );
    expect(missing).toEqual([]);
  });

  it('names every tab in the tab bar, page title and subtitle', () => {
    const tabs = ['overview', 'production', 'heating', 'household', 'wallbox', 'total', 'grid', 'prices', 'data', 'settings'];
    const missing = tabs.flatMap(tab =>
      ['solar.tab.', 'solar.title.', 'solar.subtitle.']
        .map(prefix => prefix + tab)
        .filter(key => !deKeys.has(key)),
    );
    expect(missing).toEqual([]);
  });
});

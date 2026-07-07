const eur = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR', maximumFractionDigits: 2 });
const int0 = new Intl.NumberFormat('de-DE', { maximumFractionDigits: 0 });

export const MONTH_NAMES_DE = [
  'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
  'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember',
];

export const fmtEUR = (n: number | null | undefined): string => eur.format(n ?? 0);
export const fmtKWh = (n: number | null | undefined): string => `${int0.format(n ?? 0)} kWh`;
export const fmtPct = (n: number | null | undefined): string => `${int0.format(n ?? 0)} %`;
export const fmtKm = (n: number | null | undefined): string => `${int0.format(n ?? 0)} km`;

/** "2026-06" -> "Juni 2026" */
export function monthLongLabel(period: string): string {
  const [y, m] = period.split('-').map(Number);
  return `${MONTH_NAMES_DE[m - 1]} ${y}`;
}

/** "2026-06" -> "Jun '26" */
export function monthShortLabel(period: string): string {
  const [y, m] = period.split('-').map(Number);
  return `${MONTH_NAMES_DE[m - 1].slice(0, 3)} '${String(y).slice(2)}`;
}

import { signal } from '@angular/core';

const eur = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR', maximumFractionDigits: 2 });
const int0 = new Intl.NumberFormat('de-DE', { maximumFractionDigits: 0 });
const price3 = new Intl.NumberFormat('de-DE', { minimumFractionDigits: 3, maximumFractionDigits: 3 });

export const MONTH_NAMES_DE = [
  'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
  'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember',
];

export const MONTH_NAMES_EN = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

/** UI language for month names. App keeps it in sync with the active locale. */
export const monthLang = signal<'de' | 'en'>('de');

/** Localized full month names; reactive so signal readers re-render on language change. */
export function monthNames(): string[] {
  return monthLang() === 'en' ? MONTH_NAMES_EN : MONTH_NAMES_DE;
}

export const fmtEUR = (n: number | null | undefined): string => eur.format(n ?? 0);
export const fmtKWh = (n: number | null | undefined): string => `${int0.format(n ?? 0)} kWh`;
export const fmtPct = (n: number | null | undefined): string => `${int0.format(n ?? 0)} %`;
export const fmtKm = (n: number | null | undefined): string => `${int0.format(n ?? 0)} km`;
/** Energy prices need three decimals; fmtEUR would round 0,284 down to 0,28. */
export const fmtEURperKwh = (n: number | null | undefined): string => `${price3.format(n ?? 0)} €/kWh`;

/** "2026-06" -> "Juni 2026" / "June 2026" */
export function monthLongLabel(period: string): string {
  const [y, m] = period.split('-').map(Number);
  return `${monthNames()[m - 1]} ${y}`;
}

/** "2026-06" -> "Jun '26" */
export function monthShortLabel(period: string): string {
  const [y, m] = period.split('-').map(Number);
  return `${monthNames()[m - 1].slice(0, 3)} '${String(y).slice(2)}`;
}

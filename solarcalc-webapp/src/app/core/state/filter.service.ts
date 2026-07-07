import { Injectable, signal } from '@angular/core';

export type FilterMode = 'ytd' | '12m' | 'all' | 'custom';

/**
 * Global time-range filter. Selection is data-relative (last 12 months of the
 * data, latest year present, …) and applied client-side to the server-computed
 * month list — changing it recomputes every KPI/chart/table instantly.
 */
@Injectable({ providedIn: 'root' })
export class FilterService {
  readonly mode = signal<FilterMode>('12m');
  readonly customStart = signal<string>('');
  readonly customEnd = signal<string>('');

  /** Filters an ascending-by-period list to the active range. */
  select<T extends { period: string }>(months: T[]): T[] {
    const mode = this.mode();
    if (mode === 'all' || months.length === 0) return months;
    if (mode === '12m') return months.slice(-12);
    if (mode === 'ytd') {
      const latestYear = Math.max(...months.map(m => Number(m.period.split('-')[0])));
      return months.filter(m => Number(m.period.split('-')[0]) === latestYear);
    }
    const start = this.customStart();
    const end = this.customEnd();
    return months.filter(m => (!start || m.period >= start) && (!end || m.period <= end));
  }
}

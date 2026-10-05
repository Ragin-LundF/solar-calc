import {TestBed} from '@angular/core/testing';
import {provideTranslateService} from '@ngx-translate/core';
import {MonthlySummary} from '@/core/api/models';
import {YearCompareComponent} from './year-compare.component';

function month(period: string, patch: Partial<MonthlySummary> = {}): MonthlySummary {
  return { period, ...patch } as MonthlySummary;
}

const previous = month('2025-06', { generationKwh: 1000, gridKwh: 100, householdSavings: 40, totalSavings: 40 });
const current = month('2026-06', { generationKwh: 1150, gridKwh: 150, householdSavings: 50, totalSavings: 50 });

async function render(months: MonthlySummary[]) {
  await TestBed.configureTestingModule({
    imports: [YearCompareComponent],
    providers: [provideTranslateService({ lang: 'de', fallbackLang: 'de' })],
  }).compileComponents();
  const fixture = TestBed.createComponent(YearCompareComponent);
  fixture.componentRef.setInput('months', months);
  fixture.detectChanges();
  return fixture;
}

function rowTexts(el: HTMLElement): string[] {
  return Array.from(el.querySelectorAll('[data-row]')).map(r =>
    Array.from(r.querySelectorAll('td'))
      .map(td => (td.textContent ?? '').replace(/\s+/g, ' ').trim())
      .join(' | '),
  );
}

describe('YearCompareComponent', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('shows the energy comparison by default, with better and worse changes coloured', async () => {
    const fixture = await render([previous, month('2025-12', { generationKwh: 1 }), current]);
    const el: HTMLElement = fixture.nativeElement;

    const rows = rowTexts(el);
    expect(rows).toEqual([
      'solar.col.generation | 1.000 kWh | 1.150 kWh | +15 % ▲',
      'solar.col.purchased | 100 kWh | 150 kWh | +50 % ▲',
    ]);
    const deltas = el.querySelectorAll('[data-row] td:last-child');
    expect(deltas[0].classList).toContain('text-solar-green');
    expect(deltas[1].classList).toContain('text-solar-red');
    expect(el.querySelector('[data-no-previous]')).toBeNull();
  });

  it('switches to the savings comparison including the total', async () => {
    const fixture = await render([previous, current]);
    const el: HTMLElement = fixture.nativeElement;

    (el.querySelector('[data-tab="savings"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(rowTexts(el)).toEqual([
      'solar.col.household | 40,00 € | 50,00 € | +25 % ▲',
      'solar.col.total | 40,00 € | 50,00 € | +25 % ▲',
    ]);
  });

  it('says so when the same month of the previous year was not entered', async () => {
    const fixture = await render([current]);
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('[data-no-previous]')).not.toBeNull();
    expect(rowTexts(el)[0]).toBe('solar.col.generation | – | 1.150 kWh | –');
  });

  it('renders nothing without any month', async () => {
    const fixture = await render([]);
    expect(fixture.nativeElement.textContent.trim()).toBe('');
  });
});

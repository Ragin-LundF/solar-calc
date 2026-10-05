import {ChangeDetectionStrategy, Component, computed} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {
  fmtDeltaPct,
  fmtDeltaPts,
  fmtEUR,
  fmtEURperKwh,
  monthLang,
  monthLongLabel,
  monthNames,
  monthShortLabel
} from './format';

describe('month labels', () => {
  afterEach(() => monthLang.set('de'));

  it('formats German by default', () => {
    monthLang.set('de');
    expect(monthLongLabel('2026-06')).toBe('Juni 2026');
    expect(monthShortLabel('2026-06')).toBe("Jun '26");
    expect(monthNames()[2]).toBe('März');
  });

  it('formats English when monthLang is en', () => {
    monthLang.set('en');
    expect(monthLongLabel('2026-06')).toBe('June 2026');
    expect(monthShortLabel('2026-06')).toBe("Jun '26");
    expect(monthNames()[2]).toBe('March');
  });

  it('a computed that calls the label re-runs when monthLang changes', () => {
    monthLang.set('de');
    const label = computed(() => monthLongLabel('2026-01'));
    expect(label()).toBe('Januar 2026');
    monthLang.set('en');
    expect(label()).toBe('January 2026');
  });

  it('re-renders an OnPush template on language switch', async () => {
    @Component({
      selector: 'test-label',
      changeDetection: ChangeDetectionStrategy.OnPush,
      template: `{{ label('2026-01') }}`,
    })
    class Host {
      readonly label = monthLongLabel;
    }

    monthLang.set('de');
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent.trim()).toBe('Januar 2026');

    monthLang.set('en');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent.trim()).toBe('January 2026');
  });
});

describe('fmtEURperKwh', () => {
  it('keeps the third decimal that fmtEUR would round away', () => {
    // Dynamic tariffs differ in the third decimal, so 0,284 must not collapse to 0,28.
    expect(fmtEUR(0.284)).toBe('0,28\u00a0€');
    expect(fmtEURperKwh(0.284)).toBe('0,284 €/kWh');
  });

  it('pads to three decimals so a column of prices lines up', () => {
    expect(fmtEURperKwh(0.3)).toBe('0,300 €/kWh');
  });

  it('renders a missing price as zero rather than NaN', () => {
    expect(fmtEURperKwh(null)).toBe('0,000 €/kWh');
    expect(fmtEURperKwh(undefined)).toBe('0,000 €/kWh');
  });
});

describe('fmtDeltaPct / fmtDeltaPts', () => {
  afterEach(() => monthLang.set('de'));

  it('signs a change and rounds to whole numbers', () => {
    expect(fmtDeltaPct(12.6)).toBe('+13 %');
    expect(fmtDeltaPct(-5.2)).toBe('-5 %');
    expect(fmtDeltaPct(0)).toBe('0 %');
  });

  it('shows a dash when there is nothing to compare', () => {
    expect(fmtDeltaPct(null)).toBe('–');
    expect(fmtDeltaPts(undefined)).toBe('–');
  });

  it('labels percentage points in the active language', () => {
    monthLang.set('de');
    expect(fmtDeltaPts(3.2)).toBe('+3 Pp.');
    monthLang.set('en');
    expect(fmtDeltaPts(-3.2)).toBe('-3 pp');
  });
});

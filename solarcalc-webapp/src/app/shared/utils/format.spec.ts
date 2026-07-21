import { ChangeDetectionStrategy, Component, computed } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { monthLang, monthLongLabel, monthNames, monthShortLabel } from './format';

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

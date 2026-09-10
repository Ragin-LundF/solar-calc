import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { SettingsComponent } from './settings.component';
import { ProfileStore } from '@/core/api/profile.store';
import { EnergyProfile, HeatingReferenceType } from '@/core/api/models';

function profile(heatingReferenceType: HeatingReferenceType): EnergyProfile {
  return {
    id: 'p1',
    name: 'Home',
    hasWallbox: true,
    hasHeatPump: true,
    heatingReferenceType,
    defaultElectricityPrice: 0.3,
    defaultFeedInTariff: 0.08,
    defaultPetrolPrice: 1.8,
    defaultOilReferenceCost: 2400,
    defaultGasReferenceCost: 2000,
    kmPerKwh: 5,
    litersPer100km: 8,
    investKosten: 20000,
    heatingMonthlyDistribution: [22, 18, 12, 5, 3, 1, 1, 1, 2, 3, 13, 19],
    overviewLayout: 'KPI',
    usableAreaSqm: 140,
    heatPumpScop: 3.5,
  };
}

/** Stub store so the component renders without any HTTP round-trip. */
function storeWith(p: EnergyProfile | null) {
  return { profile: signal<EnergyProfile | null>(p), save: () => undefined, reload: () => undefined };
}

async function renderWith(p: EnergyProfile | null) {
  await TestBed.configureTestingModule({
    imports: [SettingsComponent],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      provideTranslateService({ lang: 'de', fallbackLang: 'de' }),
      { provide: ProfileStore, useValue: storeWith(p) },
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(SettingsComponent);
  fixture.detectChanges();
  return fixture;
}

describe('SettingsComponent heating reference select', () => {
  afterEach(() => TestBed.resetTestingModule());

  // Regression: the options are created by @for *after* the select's own bindings run,
  // so a [value] binding on the <select> was silently discarded and the browser fell
  // back to the first option ("NONE") on every page load.
  it('pre-selects the stored heating reference on first render', async () => {
    const fixture = await renderWith(profile('GAS'));
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');

    expect(select.value).toBe('GAS');
    expect(select.selectedIndex).toBe(2);
  });

  it('pre-selects OIL as well, not just the first option', async () => {
    const fixture = await renderWith(profile('OIL'));
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');

    expect(select.value).toBe('OIL');
  });

  it('keeps NONE selected when that is the stored value', async () => {
    const fixture = await renderWith(profile('NONE'));
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');

    expect(select.value).toBe('NONE');
  });

  it('follows a later change of the stored value', async () => {
    const fixture = await renderWith(profile('NONE'));
    const store = TestBed.inject(ProfileStore) as unknown as { profile: ReturnType<typeof signal<EnergyProfile | null>> };
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    expect(select.value).toBe('NONE');

    store.profile.set(profile('OIL'));
    fixture.detectChanges();

    expect(select.value).toBe('OIL');
  });
});

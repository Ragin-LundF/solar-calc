import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { SetupWizardComponent } from './setup-wizard.component';
import { AuthService } from '@/core/auth/auth.service';
import { AppStateService } from '@/core/state/app-state.service';
import { SetupStep } from '@/core/auth/setup-step.enum';
import { EnergyProfile } from '@/core/api/models';

const STORED_PROFILE: EnergyProfile = {
  id: 'p1',
  name: 'Home',
  hasWallbox: true,
  hasHeatPump: true,
  heatingReferenceType: 'OIL',
  defaultElectricityPrice: 0.3,
  defaultFeedInTariff: 0.08,
  defaultPetrolPrice: 1.8,
  defaultOilReferenceCost: 2400,
  defaultGasReferenceCost: null,
  kmPerKwh: 5,
  litersPer100km: 8,
  investKosten: 20000,
  usableAreaSqm: 140,
  heatPumpScop: 3.5,
  heatingMonthlyDistribution: [22, 18, 12, 5, 3, 1, 1, 1, 2, 3, 13, 19],
  overviewLayout: 'KPI',
};

describe('SetupWizardComponent', () => {
  let http: HttpTestingController;

  /** Boots the wizard at the prices step with STORED_PROFILE already loaded. */
  async function bootAtPricesStep() {
    localStorage.setItem('profileId', 'p1');
    await TestBed.configureTestingModule({
      imports: [SetupWizardComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'de', fallbackLang: 'de' }),
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    const auth = TestBed.inject(AuthService);
    // AppStateService clears the active profile whenever there is no token.
    auth.token.set('test-token');
    auth.setupStep.set(SetupStep.ALLOCATION_SET);
    TestBed.inject(AppStateService).profileId.set('p1');

    const fixture = TestBed.createComponent(SetupWizardComponent);
    fixture.detectChanges();

    http.expectOne('/api/v1/profiles/p1').flush(STORED_PROFILE);
    http.match('/api/v1/profiles/p1/allocation-policies').forEach(r => r.flush([]));
    return fixture;
  }

  afterEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('keeps profile fields the first step does not edit', async () => {
    const fixture = await bootAtPricesStep();
    const wizard = fixture.componentInstance;
    wizard.profileForm.patchValue({ name: 'Renamed' });

    wizard.saveProfile();

    // The form carries 7 fields; the server replaces the whole profile on write, so the
    // rest has to be sent back or it is silently cleared.
    const put = http.expectOne(r => r.method === 'PUT' && r.url === '/api/v1/profiles/p1');
    expect(put.request.body.name).toBe('Renamed');
    expect(put.request.body.investKosten).toBe(20000);
    expect(put.request.body.usableAreaSqm).toBe(140);
    expect(put.request.body.heatPumpScop).toBe(3.5);
    expect(put.request.body.kmPerKwh).toBe(5);
    expect(put.request.body.litersPer100km).toBe(8);
    expect(put.request.body.defaultOilReferenceCost).toBe(2400);
    expect(put.request.body.heatingMonthlyDistribution).toEqual(STORED_PROFILE.heatingMonthlyDistribution);
  });

  it('saves the prices onto the profile under the names the API expects', async () => {
    const fixture = await bootAtPricesStep();
    const wizard = fixture.componentInstance;
    wizard.pricesForm.patchValue({
      defaultElectricityPrice: 0.35,
      defaultFeedInTariff: 0.07,
      defaultPetrolPrice: 1.9,
      defaultOilReferenceCost: 2600,
    });

    wizard.savePrices();

    // Previously this posted to /prices with names like electricityPricePerKwh, which the
    // API does not know — every value was dropped and an empty price_snapshot was created.
    http.expectNone(r => r.url === '/api/v1/profiles/p1/prices');
    const put = http.expectOne(r => r.method === 'PUT' && r.url === '/api/v1/profiles/p1');
    expect(put.request.body.defaultElectricityPrice).toBe(0.35);
    expect(put.request.body.defaultFeedInTariff).toBe(0.07);
    expect(put.request.body.defaultPetrolPrice).toBe(1.9);
    expect(put.request.body.defaultOilReferenceCost).toBe(2600);
    // and still does not clobber what the prices step never showed
    expect(put.request.body.investKosten).toBe(20000);
    expect(put.request.body.usableAreaSqm).toBe(140);
  });

  it('pre-fills the prices step from the stored profile', async () => {
    const fixture = await bootAtPricesStep();

    expect(fixture.componentInstance.pricesForm.getRawValue()).toEqual({
      defaultElectricityPrice: 0.3,
      defaultFeedInTariff: 0.08,
      defaultPetrolPrice: 1.8,
      defaultOilReferenceCost: 2400,
      defaultGasReferenceCost: null,
    });
  });
});

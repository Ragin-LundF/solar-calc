import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideTranslateService} from '@ngx-translate/core';
import {DataComponent} from './data.component';
import {AppStateService} from '@/core/state/app-state.service';
import {AuthService} from '@/core/auth/auth.service';
import {MonthlyInput} from '@/core/api/models';

const STORED_MONTH: MonthlyInput = {
  id: 'm1',
  period: '2025-06',
  generationKwh: 500,
  feedInKwh: 100,
  householdConsumptionKwh: 300,
  heatPumpConsumptionKwh: 100,
  wallboxConsumptionKwh: 100,
  electricityPriceOverride: null,
  feedInTariffOverride: null,
  petrolPriceOverride: 1.75,
  heatingReferenceCostOverride: 2400,
};

describe('DataComponent price fields', () => {
  let http: HttpTestingController;

  async function render(months: MonthlyInput[] = [STORED_MONTH]) {
    await TestBed.configureTestingModule({
      imports: [DataComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'de', fallbackLang: 'de' }),
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    TestBed.inject(AuthService).token.set('test-token');
    TestBed.inject(AppStateService).profileId.set('p1');

    const fixture = TestBed.createComponent(DataComponent);
    fixture.detectChanges();
    http.match('/api/v1/profiles/p1/monthly-inputs').forEach(r => r.flush(months));
    http.match(r => r.url === '/api/v1/profiles/p1').forEach(r => r.flush({}));
    await Promise.resolve();
    fixture.detectChanges();
    return fixture;
  }

  /** Answers the effective-prices lookup the form fires when a month is chosen. */
  async function flushEffective(feedInTariff: number | null, electricityPrice = 0.28) {
    const req = http.match(r => r.url.startsWith('/api/v1/profiles/p1/prices/effective'));
    req.forEach(r => r.flush({
      period: '2025-07', electricityPrice, feedInTariff, petrolPrice: null, heatingReferenceCost: null,
    }));
    await Promise.resolve();
    await Promise.resolve();
  }

  afterEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('prefills the feed-in tariff from the price in effect for the chosen month', async () => {
    const fixture = await render();
    fixture.componentInstance.patch('period', '2025-07');

    await flushEffective(0.081);

    expect(fixture.componentInstance.form().feedInTariff).toBe('0.081');
  });

  it('never prefills the electricity price', async () => {
    const fixture = await render();
    fixture.componentInstance.patch('period', '2025-07');

    await flushEffective(0.081, 0.28);

    // The electricity field records the dynamic price actually paid. Seeding it with the contract
    // price would make gridCost equal gridCostAtReferencePrice and zero out every tariff delta.
    expect(fixture.componentInstance.form().electricityPrice).toBe('');
  });

  it('does not overwrite a feed-in tariff the user already typed', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;
    data.patch('feedInTariff', '0.05');
    data.patch('period', '2025-07');

    await flushEffective(0.081);

    expect(data.form().feedInTariff).toBe('0.05');
  });

  it('keeps a tariff typed into the number input when the month is then chosen', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;

    // A type="number" input emits a number, not a string; the prefill used to throw on it
    // and silently overwrite what the user had typed.
    data.patch('feedInTariff', 0.05);
    expect(data.form().feedInTariff).toBe('0.05');

    data.patch('period', '2025-07');
    await flushEffective(0.081);

    expect(data.form().feedInTariff).toBe('0.05');
  });

  it('treats a cleared number input as empty rather than null text', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;
    data.patch('generation', 1234.5);
    expect(data.form().generation).toBe('1234.5');
    data.patch('generation', null);
    expect(data.form().generation).toBe('');
  });

  it('clears the feed-in tariff so the month inherits again', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;
    data.patch('period', '2025-07');
    await flushEffective(0.081);

    data.clear('feedInTariff');

    expect(data.form().feedInTariff).toBe('');
  });

  it('stores an empty feed-in tariff as null rather than zero', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;
    data.patch('period', '2025-08');
    await flushEffective(null);
    data.patch('generation', '400');

    data.submit();
    await Promise.resolve();

    const post = http.expectOne(r => r.method === 'POST');
    expect(post.request.body.feedInTariffOverride).toBeNull();
  });

  it('sends the feed-in tariff and keeps the overrides it does not edit', async () => {
    const fixture = await render();
    const data = fixture.componentInstance;
    data.startEdit(STORED_MONTH);
    await flushEffective(0.081);
    data.patch('feedInTariff', '0.075');

    data.submit();
    await Promise.resolve();

    const put = http.expectOne(r => r.method === 'PUT');
    expect(put.request.body.feedInTariffOverride).toBe(0.075);
    // Petrol and heating have no input on this page, so editing a month must not wipe them.
    expect(put.request.body.petrolPriceOverride).toBe(1.75);
    expect(put.request.body.heatingReferenceCostOverride).toBe(2400);
  });
});

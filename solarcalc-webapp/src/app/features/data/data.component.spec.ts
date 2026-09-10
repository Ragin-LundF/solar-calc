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

  it('never prefills the electricity price from the contract price', async () => {
    const fixture = await render();
    fixture.componentInstance.patch('period', '2025-07');

    // 0.28 is the contract price for that month, and it must not reach the form: the summary
    // derives gridCost from this override and gridCostAtReferencePrice from the contract price,
    // so seeding both to the same number would zero out every tariff delta.
    await flushEffective(0.081, 0.28);

    // The only recorded month stored no price of its own, so there is nothing to carry over.
    expect(fixture.componentInstance.form().electricityPrice).toBe('');
  });

  it('carries the electricity price over from the most recent month that recorded one', async () => {
    const fixture = await render([
      { ...STORED_MONTH, id: 'm1', period: '2025-05', electricityPriceOverride: 0.271 },
      { ...STORED_MONTH, id: 'm2', period: '2025-06', electricityPriceOverride: 0.294 },
    ]);

    // June is the latest recorded month, so its price is the one worth correcting from.
    expect(fixture.componentInstance.form().electricityPrice).toBe('0.294');
    // The energy readings are specific to a month and stay empty.
    expect(fixture.componentInstance.form().generation).toBe('');
    expect(fixture.componentInstance.form().period).toBe('');
  });

  it('skips months that recorded no price of their own when carrying one over', async () => {
    const fixture = await render([
      { ...STORED_MONTH, id: 'm1', period: '2025-05', electricityPriceOverride: 0.271 },
      { ...STORED_MONTH, id: 'm2', period: '2025-06', electricityPriceOverride: null },
    ]);

    expect(fixture.componentInstance.form().electricityPrice).toBe('0.271');
  });

  it('does not overwrite an electricity price the user already typed', async () => {
    const fixture = await render([
      { ...STORED_MONTH, id: 'm1', period: '2025-05', electricityPriceOverride: 0.271 },
    ]);
    const data = fixture.componentInstance;
    data.patch('electricityPrice', 0.35);

    // A reload must not clobber what is already half-entered.
    TestBed.inject(AppStateService).profileId.set('p2');
    await fixture.whenStable();
    http.match(r => r.url === '/api/v1/profiles/p2/monthly-inputs')
      .forEach(r => r.flush([{ ...STORED_MONTH, id: 'm9', period: '2025-08', electricityPriceOverride: 0.9 }]));
    await fixture.whenStable();

    expect(data.form().electricityPrice).toBe('0.35');
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

  it('shows a dash for overrides the server omitted from the response', async () => {
    const sparse = [{ id: 9, period: '2025-03' }] as unknown as MonthlyInput[];
    const fixture = await render(sparse);
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;
    const cells = [...row.querySelectorAll('td')].map(td => td.textContent!.trim());

    // The two price-override columns are the ones that distinguish "not set" from a real value.
    expect(cells[6]).toBe('—');
    expect(cells[7]).toBe('—');
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

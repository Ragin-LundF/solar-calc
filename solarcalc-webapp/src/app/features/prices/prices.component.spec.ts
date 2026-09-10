import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideTranslateService} from '@ngx-translate/core';
import {PricesComponent} from './prices.component';
import {AppStateService} from '@/core/state/app-state.service';
import {AuthService} from '@/core/auth/auth.service';
import {PriceSnapshot} from '@/core/api/models';

const ENTRIES: PriceSnapshot[] = [
  {
    id: 2, validFrom: '2025-01', electricityPrice: 0.28, feedInTariff: null, petrolPrice: null,
    heatingReferenceType: 'GAS', oilReferenceCost: null, gasReferenceCost: 1800,
  },
  {
    id: 1, validFrom: '2024-01', electricityPrice: 0.32, feedInTariff: 0.08, petrolPrice: 1.72,
    heatingReferenceType: 'OIL', oilReferenceCost: 2400, gasReferenceCost: null,
  },
];

describe('PricesComponent', () => {
  let http: HttpTestingController;

  async function render(entries: PriceSnapshot[] = ENTRIES) {
    await TestBed.configureTestingModule({
      imports: [PricesComponent],
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

    const fixture = TestBed.createComponent(PricesComponent);
    fixture.detectChanges();
    http.match('/api/v1/profiles/p1/prices').forEach(r => r.flush(entries));
    // ProfileStore also boots and fetches; let it resolve to nothing.
    http.match(r => r.url === '/api/v1/profiles/p1').forEach(r => r.flush({}));
    fixture.detectChanges();
    return fixture;
  }

  /** Sets a control's value the way a user would, so the real value accessor runs. */
  function setValue(fixture: ComponentFixture<PricesComponent>, selector: string, value: string): void {
    const el = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    el.value = value;
    el.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  afterEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('lists the timeline newest start month first', async () => {
    const fixture = await render();

    expect(fixture.componentInstance.rows().map(e => e.validFrom)).toEqual(['2025-01', '2024-01']);
  });

  it('marks the entry that is in effect today', async () => {
    const fixture = await render();

    // Both entries already started, so the newer one is the one that applies.
    expect(fixture.componentInstance.currentId()).toBe(2);
  });

  it('marks no entry as in effect when the timeline starts in the future', async () => {
    const fixture = await render([
      {
        id: 9, validFrom: '2999-01', electricityPrice: 0.1, feedInTariff: null, petrolPrice: null,
        heatingReferenceType: null, oilReferenceCost: null, gasReferenceCost: null,
      },
    ]);

    expect(fixture.componentInstance.currentId()).toBeNull();
  });

  it('refuses to submit an entry that sets no price', async () => {
    const fixture = await render();
    const prices = fixture.componentInstance;
    prices.patch('validFrom', '2026-01');

    expect(prices.formHasPrice()).toBe(false);
    prices.submit();
    http.expectNone(r => r.method === 'POST');
  });

  it('creates an entry with only the prices that were filled in', async () => {
    const fixture = await render();
    const prices = fixture.componentInstance;
    prices.patch('validFrom', '2026-01');
    prices.patch('petrolPrice', '1.90');

    prices.submit();

    const post = http.expectOne(r => r.method === 'POST' && r.url === '/api/v1/profiles/p1/prices');
    expect(post.request.body.validFrom).toBe('2026-01');
    expect(post.request.body.petrolPrice).toBe(1.9);
    // Untouched prices stay null so the older entry keeps supplying them.
    expect(post.request.body.electricityPrice).toBeNull();
    // No fuel chosen means "unchanged", not "no heating".
    expect(post.request.body.heatingReferenceType).toBeNull();
  });

  it('records a fuel switch even when no cost is entered with it', async () => {
    const fixture = await render();
    const prices = fixture.componentInstance;
    prices.patch('validFrom', '2026-01');
    prices.patch('heatingReferenceType', 'GAS');

    // Stating the fuel alone is a legitimate entry; the cost can keep coming from elsewhere.
    expect(prices.formHasPrice()).toBe(true);
    prices.submit();

    const post = http.expectOne(r => r.method === 'POST');
    expect(post.request.body.heatingReferenceType).toBe('GAS');
  });

  it('reports a duplicate start month rather than a generic failure', async () => {
    const fixture = await render();
    const prices = fixture.componentInstance;
    prices.patch('validFrom', '2025-01');
    prices.patch('electricityPrice', '0.29');

    prices.submit();
    http.expectOne(r => r.method === 'POST').flush({}, { status: 409, statusText: 'Conflict' });
    await Promise.resolve();

    expect(prices.error()).toBe('solar.prices.duplicate');
  });

  it('enables Add once a start month and a price are typed into the form', async () => {
    const fixture = await render([]);
    const button = (): HTMLButtonElement => fixture.nativeElement.querySelector('button');
    expect(button().disabled).toBe(true);

    // Drive the real controls: a number input emits a number through ngModelChange,
    // which used to poison the string-typed form and wedge the button as disabled.
    setValue(fixture, 'input[type=month]', '2026-01');
    setValue(fixture, 'input[type=number]', '0.30');

    expect(fixture.componentInstance.form().electricityPrice).toBe('0.3');
    expect(button().disabled).toBe(false);
  });

  it('disables Add again when the only price typed is cleared', async () => {
    const fixture = await render([]);
    setValue(fixture, 'input[type=month]', '2026-01');
    setValue(fixture, 'input[type=number]', '0.30');
    setValue(fixture, 'input[type=number]', '');

    expect(fixture.componentInstance.form().electricityPrice).toBe('');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
  });

  it('posts a typed-in price as a number', async () => {
    const fixture = await render([]);
    setValue(fixture, 'input[type=month]', '2026-01');
    setValue(fixture, 'input[type=number]', '0.30');
    fixture.nativeElement.querySelector('button').click();

    const req = http.expectOne(r => r.method === 'POST' && r.url === '/api/v1/profiles/p1/prices');
    expect(req.request.body.validFrom).toBe('2026-01');
    expect(req.request.body.electricityPrice).toBe(0.3);
    req.flush({});
  });

  it('shows a dash for prices the server omitted from the response', async () => {
    // The server uses Jackson NON_EMPTY, so null fields are absent from the JSON rather than
    // being sent as null. Mirror that payload exactly instead of a hand-written null.
    const sparse = [{ id: 9, validFrom: '2025-03' }] as unknown as PriceSnapshot[];
    const fixture = await render(sparse);
    await fixture.whenStable();
    fixture.detectChanges();
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;
    const cells = [...row.querySelectorAll('td')].map(td => td.textContent!.trim());

    // Every unset column reads as a dash: no raw translation key, no 0,00 € standing in for "not set".
    expect(cells.slice(1, 7)).toEqual(['—', '—', '—', '—', '—', '—']);
    expect(row.textContent).not.toContain('heatingReferenceType_');
    expect(row.textContent).not.toContain('undefined');
  });

  it('loads an entry into the form for editing', async () => {
    const fixture = await render();
    const prices = fixture.componentInstance;

    prices.startEdit(ENTRIES[1]);

    expect(prices.editingId()).toBe(1);
    expect(prices.form().validFrom).toBe('2024-01');
    expect(prices.form().electricityPrice).toBe('0.32');
    // A price the entry does not carry stays empty rather than showing a zero.
    expect(prices.form().gasReferenceCost).toBe('');
    expect(prices.form().heatingReferenceType).toBe('OIL');
  });
});

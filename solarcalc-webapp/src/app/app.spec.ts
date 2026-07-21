import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateService, provideTranslateService } from '@ngx-translate/core';
import { App } from './app';
import { monthLang } from '@/shared/utils/format';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'de', fallbackLang: 'de' }),
      ],
    }).compileComponents();
  });

  afterEach(() => monthLang.set('de'));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('syncs month-name language with the active UI language', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(monthLang()).toBe('de');

    TestBed.inject(TranslateService).use('en');
    fixture.detectChanges();
    expect(monthLang()).toBe('en');
  });
});

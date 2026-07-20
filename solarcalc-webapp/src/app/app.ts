import { Component, computed, effect, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '@/core/auth/auth.service';
import { AppStateService } from '@/core/state/app-state.service';
import { FilterService } from '@/core/state/filter.service';
import { ProfileStore } from '@/core/api/profile.store';
import { monthLang } from '@/shared/utils/format';

interface Tab {
  id: string;
  route: string;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TranslatePipe],
  templateUrl: './app.html',
})
export class App {
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);
  readonly state = inject(AppStateService);
  readonly filterState = inject(FilterService);
  // Instantiated app-wide so a stale/missing profile id self-heals on any page.
  private readonly profileStore = inject(ProfileStore);

  readonly currentLang = computed(() => this.translate.currentLang() ?? 'de');

  readonly tabs: Tab[] = [
    { id: 'overview', route: '/overview' },
    { id: 'production', route: '/production' },
    { id: 'heating', route: '/heating' },
    { id: 'household', route: '/household' },
    { id: 'wallbox', route: '/wallbox' },
    { id: 'total', route: '/total' },
    { id: 'data', route: '/data' },
    { id: 'settings', route: '/settings' },
  ];

  readonly activeTab = signal<string>(this.tabFromUrl(this.router.url));
  readonly showFilter = computed(() => !['data', 'settings'].includes(this.activeTab()));

  constructor() {
    effect(() => monthLang.set(this.currentLang() === 'en' ? 'en' : 'de'));
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(e => this.activeTab.set(this.tabFromUrl(e.urlAfterRedirects)));
  }

  private tabFromUrl(url: string): string {
    const segment = url.split('?')[0].split('/').filter(Boolean)[0] ?? 'overview';
    return this.tabs.some(t => t.id === segment) ? segment : 'overview';
  }

  setFilterMode(mode: 'ytd' | '12m' | 'all' | 'custom'): void {
    this.filterState.mode.set(mode);
  }

  toggleLang(): void {
    this.translate.use(this.currentLang() === 'de' ? 'en' : 'de');
  }

  logout(): void {
    this.auth.logout();
  }
}

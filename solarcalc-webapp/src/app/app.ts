import { Component, computed, inject, signal, effect } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '@/core/auth/auth.service';
import { AppStateService } from '@/core/state/app-state.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './app.html',
})
export class App {
  private readonly translate = inject(TranslateService);
  readonly auth = inject(AuthService);
  readonly state = inject(AppStateService);

  readonly currentLang = computed(() => this.translate.currentLang() ?? 'de');
  readonly dark = signal(localStorage.getItem('darkMode') === 'true');

  readonly navItems = [
    { route: '/dashboard', labelKey: 'navigation.dashboard', icon: '⚡' },
    { route: '/monthly-input', labelKey: 'navigation.monthlyInput', icon: '📊' },
    { route: '/profile-settings', labelKey: 'navigation.profileSettings', icon: '⚙️' },
    { route: '/allocation-policy', labelKey: 'navigation.allocationPolicy', icon: '🔀' },
    { route: '/prices', labelKey: 'navigation.prices', icon: '💶' },
  ];

  constructor() {
    effect(() => {
      const isDark = this.dark();
      document.documentElement.classList.toggle('dark', isDark);
      localStorage.setItem('darkMode', String(isDark));
    });
  }

  toggleDark(): void {
    this.dark.update(v => !v);
  }

  toggleLang(): void {
    const next = this.currentLang() === 'de' ? 'en' : 'de';
    this.translate.use(next);
  }

  logout(): void {
    this.auth.logout();
  }
}

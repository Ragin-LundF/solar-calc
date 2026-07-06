import { Component, computed, inject, Signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AppStateService } from '@/core/state/app-state.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, FormsModule],
  templateUrl: './app.html',
})
export class App {
  private readonly translate = inject(TranslateService);
  readonly state = inject(AppStateService);

  readonly currentLang: Signal<string> = computed(() => this.translate.currentLang() ?? 'de');
  tenantIdInput = '';

  readonly navItems = [
    { route: '/dashboard', labelKey: 'navigation.dashboard', icon: '⚡' },
    { route: '/monthly-input', labelKey: 'navigation.monthlyInput', icon: '📊' },
    { route: '/profile-settings', labelKey: 'navigation.profileSettings', icon: '⚙️' },
    { route: '/prices', labelKey: 'navigation.prices', icon: '💶' },
    { route: '/allocation-policy', labelKey: 'navigation.allocationPolicy', icon: '🔀' },
    { route: '/scenarios', labelKey: 'navigation.scenarioComparison', icon: '🔬' },
  ];

  toggleLang(): void {
    const next = this.currentLang() === 'de' ? 'en' : 'de';
    this.translate.use(next);
  }

  applyTenantId(): void {
    const id = Number(this.tenantIdInput);
    if (id > 0) this.state.setTenant(id);
  }
}

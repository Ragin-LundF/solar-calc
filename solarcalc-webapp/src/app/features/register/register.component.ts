import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';
import {AuthService} from '@/core/auth/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink, TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  username = '';
  password = '';

  ngOnInit(): void {
    if (this.auth.isAuthenticated) {
      this.router.navigate(['/dashboard']);
    }
  }

  async submit(): Promise<void> {
    if (!this.username || !this.password) return;
    this.loading.set(true);
    this.error.set(null);
    try {
      await this.auth.register(this.username, this.password);
      this.router.navigate(['/dashboard']);
    } catch (e: unknown) {
      const conflict = e instanceof HttpErrorResponse && e.status === 409;
      this.error.set(conflict ? 'auth.usernameTaken' : 'auth.registerError');
    } finally {
      this.loading.set(false);
    }
  }
}

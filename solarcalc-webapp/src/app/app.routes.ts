import { Routes } from '@angular/router';
import { authGuard } from '@/core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('@/features/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('@/features/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('@/features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'monthly-input',
        loadComponent: () =>
          import('@/features/monthly-input/monthly-input.component').then(m => m.MonthlyInputComponent),
      },
      {
        path: 'profile-settings',
        loadComponent: () =>
          import('@/features/profile-settings/profile-settings.component').then(m => m.ProfileSettingsComponent),
      },
      {
        path: 'prices',
        loadComponent: () =>
          import('@/features/prices/prices.component').then(m => m.PricesComponent),
      },
      {
        path: 'allocation-policy',
        loadComponent: () =>
          import('@/features/allocation-policy/allocation-policy.component').then(m => m.AllocationPolicyComponent),
      },

    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

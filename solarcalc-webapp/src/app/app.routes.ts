import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
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
  {
    path: 'scenarios',
    loadComponent: () =>
      import('@/features/scenario-comparison/scenario-comparison.component').then(m => m.ScenarioComparisonComponent),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

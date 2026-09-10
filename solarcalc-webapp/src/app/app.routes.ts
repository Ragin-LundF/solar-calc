import { Routes } from '@angular/router';
import { authGuard } from '@/core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('@/features/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('@/features/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      { path: 'overview', loadComponent: () => import('@/features/overview/overview.component').then(m => m.OverviewComponent) },
      { path: 'production', loadComponent: () => import('@/features/production/production.component').then(m => m.ProductionComponent) },
      { path: 'heating', loadComponent: () => import('@/features/heating/heating.component').then(m => m.HeatingComponent) },
      { path: 'household', loadComponent: () => import('@/features/household/household.component').then(m => m.HouseholdComponent) },
      { path: 'wallbox', loadComponent: () => import('@/features/wallbox/wallbox.component').then(m => m.WallboxComponent) },
      { path: 'total', loadComponent: () => import('@/features/total/total.component').then(m => m.TotalComponent) },
      { path: 'grid', loadComponent: () => import('@/features/grid/grid.component').then(m => m.GridComponent) },
      { path: 'data', loadComponent: () => import('@/features/data/data.component').then(m => m.DataComponent) },
      { path: 'settings', loadComponent: () => import('@/features/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'setup', loadComponent: () => import('@/features/setup-wizard/setup-wizard.component').then(m => m.SetupWizardComponent) },
    ],
  },
  { path: '**', redirectTo: 'overview' },
];

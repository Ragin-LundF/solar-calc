---
name: angular-guidelines
description: >
  Angular frontend coding rules: version, project structure, component style,
  reactive patterns, i18n, styling with TailwindCSS 4, ZardUI primitives,
  icons, testing, and dependency rules. Load whenever creating or editing
  Angular TypeScript or HTML template files.
---

# Angular Frontend Guidelines

Load when working in `solarcalc-webapp`. These rules apply to all Angular TypeScript and template files.

## Stack versions

- Angular **22** (standalone components, signal-based reactivity where applicable)
- TailwindCSS **4** (Vite plugin, no config file required)
- ZardUI primitives via `shared/core/provider/providezard` — use `class-variance-authority` + `clsx` for variants
- `@ng-icons/core` + `@ng-icons/lucide` for icons
- `@ngx-translate/core` + `@ngx-translate/http-loader` for i18n (default language: `de`)

## Project structure

```
src/app/
├── app.config.ts          # ApplicationConfig with all providers
├── app.routes.ts          # Root route table
├── core/
│   ├── api/               # HTTP services (ApiService and domain-specific services)
│   ├── auth/              # Auth guard / interceptor
│   ├── i18n/              # Translation helpers
│   └── state/             # App-wide state (AppStateService)
├── features/              # Feature components (one folder per feature screen)
└── shared/
    ├── components/        # Reusable UI components (card, button, input, …)
    ├── core/              # Directives, ZardUI provider, index exports
    ├── pipes/             # Reusable pipes
    └── utils/             # Pure utility functions
```

- One component per folder. Export via `index.ts` barrel files where the folder is a shared component.
- Feature components live in `features/<name>/<name>.component.ts`. Do not nest feature components under other features.
- Shared components live in `shared/components/<name>/`. They must have no feature-specific knowledge.
- Do not put business logic in components; delegate to `core/api/` services or `core/state/`.

## Component style

- Use **standalone components** only. Do not use `NgModule`.
- Declare `imports` explicitly in each standalone component — no wildcard module imports.
- Template is in a separate `.html` file unless the template is two lines or fewer.
- Styles: prefer TailwindCSS utility classes in templates. Use `.css` files only for global styles or non-utility overrides.
- Component class has `inject()` for all dependencies (not constructor injection).
- Use `signal()`, `computed()`, `effect()` for reactive state when the logic is local to the component.
- Use `AsyncPipe` (`| async`) or `toSignal()` for `Observable` in templates — no `.subscribe()` in the component class except when the subscription lifetime is explicitly managed.

## Reactive patterns

- Use Angular `HttpClient` via `core/api/ApiService` — never import `HttpClient` directly in feature components.
- `HttpClient` calls return `Observable`. Use `firstValueFrom` for one-shot calls in component initialization.
- Error handling: propagate errors to the user via a shared error state or toast — never silently swallow `catchError`.
- Avoid nested `subscribe()`. Use `switchMap`, `combineLatest`, `forkJoin` instead.

## i18n

- All user-visible strings are translated via `@ngx-translate`. No hardcoded UI text.
- Translation keys are organized by feature: `feature.section.key`, e.g. `dashboard.title`.
- Translation files are in `src/assets/i18n/<lang>.json`.
- Use the `translate` pipe in templates: `{{ 'key' | translate }}`.

## Styling

- TailwindCSS 4 with the Vite plugin — no `tailwind.config.*` file.
- Use `class-variance-authority` (`cva`) for component variants in `shared/components/`.
- Use `clsx` + `tailwind-merge` (`cn`) for conditional class merging — see `shared/utils/merge-classes.ts`.
- Do not add inline `style=""` attributes unless absolutely necessary for dynamic values that TailwindCSS cannot produce.

## Icons

- Import icons from `@ng-icons/lucide`.
- Register icons via `provideIcons()` in the component's `providers` array or in `provideZard()`.
- Do not bundle icons globally unless they are used on every page.

## Dependencies

- Do not add new `npm` packages for functionality already covered by the installed dependencies.
- Do not add component libraries beyond the ZardUI primitives already in `shared/` — build from primitives.
- Check `shared/components/` before writing a new shared component — reuse what exists.

## Testing

- Use Angular's built-in `TestBed` for component tests.
- Test component behavior (rendered output, event responses), not implementation details.
- Use Jasmine or Vitest (whichever the project configures) — check `package.json` before adding a test runner.
- Prefer `ng test` for running tests locally.

## Forbidden shortcuts

- Do not use `any` types in TypeScript without a written justification.
- Do not use `document.querySelector` or direct DOM manipulation — use Angular template references or signals.
- Do not use `@ViewChild` to read form values — use reactive forms or template-driven forms with `ngModel`.
- Do not disable `strictTemplates` in `tsconfig`.

# Solar Calculator — Redesign Implementation

Rebuild of the solar/heat-pump/wallbox savings calculator to the design in
`.plan/design/` (README + `Solar Dashboard.dc.html` reference + screenshots).

Backend: Spring Boot 4 / Kotlin, all savings math in a `SummaryService`.
Frontend: Angular 22, standalone components, signals, ZardUI primitives.

Work proceeds **view by view** (backend + frontend together). This checklist is
the source of truth — update it as tasks complete.

## Task List

### 0. Foundation (shared across all views)
- [x] Backend: new `EnergyProfile` fields — `kmPerKwh`, `litersPer100km`,
      `investKosten`, `heatingMonthlyDistribution` (12 %), `overviewLayout`
      (+ edited `0003-create-energy-profile.xml`; added `reference_price` to
      `monthly_energy_input` in `0004-...xml`)
- [x] Backend: extend profile DTOs (`Create`/`Update`/`Response`) + controller
- [x] Backend: `SummaryService` — per-month enriched results, aggregates,
      cumulative payback projection, wallbox-vs-gasoline, heating distribution
      (verified by `SummaryServiceImplTest`)
- [x] Backend: Summary DTOs + domain controller + `GET .../summary` endpoint
- [x] Frontend: design tokens (dark theme palette) in `styles.css`
- [x] Frontend: app shell — top pill-tab bar (replaces sidebar), page header,
      brand mark, dark/lang/logout
- [x] Frontend: global time-range filter (chips + custom month pickers) as
      `FilterService` (client-side, data-relative selection)
- [x] Frontend: `SummaryStore` + `ProfileStore` (fetch once per profile) + routes
- [x] Frontend: shared `KpiCardComponent` + generic `BarChartComponent`
      (grouped/stacked) for reuse across views

### 1. Übersicht (Overview)
- [x] KPI-Karten layout: 5 KPI cards, combo bar+line chart, payback ring,
      stacked contribution bar
- [x] Story layout: hero card, compact KPI rows, cumulative line chart
- [x] Segmented control toggle (persisted via `overviewLayout`)

All analysis views share `AnalysisLayoutComponent` (states + KPI grid + chart) and
are fully i18n (English-slug keys under `solar.*`, de/en).

### 2. Erzeugung & Einspeisung (Production)
- [x] 4 KPIs, stacked bar chart (self-consumed / fed-in), table

### 3. Heizung (Heating)
- [x] 4 KPIs, grouped bar chart, table, "Monatsverteilung bearbeiten" link

### 4. Haushalt (Household)
- [x] 4 KPIs, grouped bar chart, table

### 5. Wallbox
- [x] 5 KPIs, grouped bar chart, table

### 6. Gesamtabrechnung (Total)
- [x] 4 KPIs, stacked/grouped bar chart, table

### 7. Daten (Data entry)
- [x] "Neuen Monat erfassen" upsert form (POST, or PUT when period exists;
      added `referencePrice` to input DTOs + mapping)
- [x] Months table (newest first)
- [x] "Heizungsverteilung über das Jahr" — 12 inputs, live sum pill (100 %),
      save disabled unless sum == 100

### 8. Einstellungen (Settings)
- [x] "Energie-Allokation" reorderable priority list (↑/↓, reload on change)
- [x] Profil card (name, wallbox/WP, heating reference)
- [x] Preise & Investition card (prices + L/100km + km/kWh + invest cost),
      auto-save on blur

### 9. Cleanup (remaining)
- [x] i18n keys (de/en) for all new copy — English-slug keys under `solar.*`
- [x] Build green — backend `compileKotlin` + `SummaryServiceImplTest`, frontend `ng build`
- [ ] Remove obsolete files (old `dashboard`, `monthly-input`, `prices`,
      `profile-settings`, `allocation-policy` feature components; stale `navigation.*`
      i18n keys) — currently unrouted but still on disk
- [ ] Manual end-to-end pass against a running backend with real data
- [ ] Backend: run full `./gradlew build` (all module tests) once
- [ ] Consider default `kmPerKwh` for existing profiles (exposed in Settings for now)

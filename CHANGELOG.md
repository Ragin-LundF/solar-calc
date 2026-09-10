# Changelog

All notable changes to Solar Calculator are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-09-11

First public release.

### What Solar Calculator does

Solar Calculator answers two questions about a photovoltaic system: **what is it
actually saving me each month, and when will it have paid for itself?** You enter
your own meter readings and your own prices — nothing is estimated from a vendor
brochure — and every figure in the app is derived from those numbers.

**Track your data**

- Record one entry per month: solar generation, feed-in to the grid, household
  consumption, and — if you have them — heat pump and wallbox consumption, all in kWh.
- Record what you actually paid that month: the average electricity price and the
  feed-in tariff that applied. Leave them empty and the month falls back to the price
  that was in effect.
- All recorded months stay editable, so a corrected meter reading is a two-click fix.
- A price timeline keeps your contract history: each entry states the prices valid
  **from** a given month until a later entry supersedes it. Past months keep costing
  what they really cost instead of being repriced with today's tariff. The timeline can
  also record a switch of heating fuel (oil ↔ gas), so months before the switch stay
  costed with the old fuel.
- Every screen has a time filter: this year, last 12 months, all, or a custom range.

**See your return**

- **Overview** — savings and payback at a glance: a donut showing how much of the
  investment is paid off, the running total against the system cost, and the expected
  break-even date.
- **Production & feed-in** — how much you generated, how much you used yourself, how
  much you sold, and what the feed-in earned.
- **Heating** — your heat pump against a reference oil or gas heating, including a rough
  German energy efficiency class (A+…H) once twelve consecutive months exist.
- **Household** — electricity cost with solar versus without.
- **Wallbox** — EV charging against refuelling a combustion car, with estimated km driven.
- **Total** — all savings stacked per month: feed-in, heating, household, wallbox.
- **Grid & tariff** — what you paid for grid electricity versus a fixed-price contract.
  Reported on its own: it measures the tariff choice, not the PV system, so it never
  distorts the savings totals or the payback projection.

**Set it up your way**

- A guided setup wizard walks through the first configuration.
- An ordered allocation policy (e.g. heat pump → wallbox → household) decides which
  consumer receives solar power first. It drives every cost and savings figure.
- Configure electricity price, feed-in tariff, petrol price, car consumption, EV
  efficiency, heating reference cost, and the total system investment used for payback.
- A yearly heating distribution (twelve percentages summing to 100%) spreads the annual
  reference heating cost across the months.
- Multiple installations per account, each with its own data and prices.
- Available in German and English.

**Your data stays yours**

- Accounts are protected with JWT authentication; every entry belongs to your account
  alone, if you register at https://solar-calc.oikal-host.de.
- Self-hosted: run it with Docker Compose against your own MariaDB. See the
  [README](README.md) for setup.

### Known limitations

- The energy efficiency class is an estimate derived from heat pump consumption. It is
  not an official Energieausweis and ignores the hot-water share and primary energy
  factors.
- The payback projection extrapolates from the months you have entered — the more
  history you record, the more reliable it becomes.

[1.0.0]: https://github.com/ragin-lundf/solar-calc/releases/tag/v1.0.0

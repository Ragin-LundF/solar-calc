# Solar Calculator

Solar Calculator (`solar-calc`) helps you find out what a photovoltaic system
really costs — and really saves. Instead of relying on the glossy numbers from a
sales brochure, you enter your own monthly energy data and the app works out how
much money the sun is actually putting back in your pocket.

## What it does

You feed the app your real numbers — month by month solar generation, grid
feed-in, household consumption, and optional loads like a heat pump or an EV
wallbox — together with your electricity and feed-in tariffs. From that it
calculates:

- **Self-consumption vs. feed-in** — how much of your generated power you use
  yourself and how much you sell back to the grid.
- **Monthly and cumulative savings** — what you save each month and over time,
  based on your own prices.
- **Cost allocation** — how savings are split across household, heat pump, and
  wallbox using configurable allocation policies.
- **Payback projection** — when your investment pays for itself.

Everything is tied to your account, so you can keep building up a history and
watch the picture get more accurate the more data you enter.

[Screenshots](docs/README.md)

## Who it's for

Anyone curious about the economics of going solar: homeowners weighing an
installation, existing PV owners who want to verify their real-world return, and
generally anyone who'd rather calculate the numbers themselves than take a
vendor's word for it. No engineering background required — if you can read your
electricity meter, you can use it.

## Tech stack

- **Backend:** Kotlin + Spring Boot, MariaDB, Liquibase, JWT authentication
- **Frontend:** Angular + Tailwind CSS
- **Build:** Gradle (backend), npm/Angular CLI (frontend)

## Running it locally

You need Docker, a JDK (Java 25), and Node.js/npm. The bundled Gradle and
Angular wrappers pull in everything else.

### Option A — everything in Docker (quickest)

Builds the app and database and starts them together:

```bash
cd devops
export DB_PASSWORD=solarcalc
export DB_ROOT_PASSWORD=root
export SOLARCALC_JWT_SECRET=$(openssl rand -base64 32)
docker compose up --build
```

The app is then available at http://localhost:8080.

### Option B — local development

Run the database in Docker and the backend/frontend from source, so you get
hot reload while working.

**1. Start the database:**

```bash
docker compose -f devops/docker-compose.local.yml up -d
```

**2. Start the backend** (uses the `local` profile, which ships a dev-only JWT
key so no secret is needed):

```bash
./gradlew :solarcalc-server:bootRun --args='--spring.profiles.active=local'
```

The API listens on http://localhost:8080.

**3. Start the frontend:**

```bash
cd solarcalc-webapp
npm install
npm start
```

The web app is served at http://localhost:4200 and talks to the backend on
port 8080.

## Building for production

```bash
./gradlew clean build          # backend, runs tests
cd solarcalc-webapp && npm run build   # frontend bundle
```

This produces a runnable Spring Boot jar under
`solarcalc-server/build/libs/`.

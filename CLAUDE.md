# Hizmet.tr — CLAUDE.md

Course project for BİL 4124 "Mobil Programlama II", Dokuz Eylül University, Fall 2026.
An Android app for booking appointments with service specialists (hairdresser, barber, manicure).
The original plan and screen sketches are in `../hizmet.tr_Proje_Konusu_Belirleme_Raporu.pdf`.

## Language

- **Everything in this repository is in English**: code, identifiers, comments, commit messages,
  branch names, PR titles/descriptions, README and docs.
  This rule overrides any global rule that asks for Russian commit messages.
- App UI strings: English and Turkish (`values/strings.xml` + `values-tr/strings.xml`).

## Structure (monorepo)

```
android/   — client: Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Coroutines, Retrofit
backend/   — server: NestJS (TypeScript strict), REST, Swagger, JWT, Prisma, PostgreSQL
```

The Android emulator reaches the local backend at `http://10.0.2.2:3000`.

## Git rules (mandatory)

- **Commits are authored only by the user** (their local `git config user.name/email`).
  Never add `Co-Authored-By`, "Generated with Claude" or any mention of AI —
  not in commits, PRs, or code.
- **Never commit directly to `main`.** Each task gets its own branch from an up-to-date `main`:
  `feat/<short-name>`, `fix/<short-name>`, `chore/<short-name>` (e.g. `feat/catalog-screen`).
  Changes reach `main` only through a Pull Request.
- Commit and push only when the user explicitly asks.
- No force push, no rewriting published history.
- Conventional Commits with a scope: `feat(android): ...`, `feat(backend): ...`, `fix(booking): ...`.
- Never commit: `.env`, `node_modules/`, `dist/`, `build/`, `.gradle/`, `local.properties`, `*.keystore`.

## Scope by stage

**Stage 1 (now) — client side, product demo:**
phone login, specialist catalog (search, filters, sorting), specialist profile,
booking a free time slot, "My appointments" (status, cancel). Specialists are seeded in the DB.

**Later:** specialist mode (services, working hours, requests, calendar), reviews, favorites,
notifications/reminders, map/GPS, camera.

## Agreed simplifications (do not "fix" these)

- **Fake OTP**: no SMS is sent, the server accepts code `1234` but issues real JWTs (access + refresh).
- **No Redis / BullMQ / cron.** Reminders 24 h and 2 h before a visit are local on the phone
  (WorkManager), in the "later" stage.
- **Double-booking protection** — a PostgreSQL exclusion constraint (raw SQL in a Prisma migration);
  on conflict the server returns `409` and the app asks the user to pick another time.

## How the code should look

- **Follow the sketches in the report** (Figures 2–7) for screens, layout and flow.
  Do not add screens or features that are not in the report or this file.
- **Course level, not production level.** Use what is taught in the course
  (ViewModel, StateFlow, lists, navigation, Room/DataStore later) and keep it plain:
  no extra architecture layers, no DI framework, no generic helpers "for the future".
- Every team member must be able to explain their part at the presentation,
  so prefer simple, readable code over clever code. Do not paste large chunks of code from the internet.
- Minimal diff, no drive-by refactors.
- Before saying "done": backend — `npm run lint && npm test && npm run build`;
  android — `./gradlew lint test assembleDebug`.

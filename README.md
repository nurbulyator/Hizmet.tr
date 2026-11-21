# Hizmet.tr

Android app for booking appointments with hairdressers, barbers and manicure specialists.
Course project for BİL 4124 Mobil Programlama II (Dokuz Eylül University, Fall 2026).

```
android/   Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Retrofit
backend/   NestJS, Prisma, PostgreSQL, JWT, Swagger
```

## Run the backend

Requirements: Node.js 22, Docker.

```bash
docker compose up -d            # PostgreSQL on localhost:5440
cd backend
cp .env.example .env
npm install
npx prisma migrate deploy       # create tables
npm run db:seed                 # demo specialists, bookings and reviews
npm run start:dev               # http://localhost:3000, API docs: http://localhost:3000/docs
```

## Run the Android app

Open `android/` in Android Studio and run the `app` configuration on an emulator.
The emulator reaches the backend at `http://10.0.2.2:3000` (set in `app/build.gradle.kts`).

Demo login: phone `532 123 45 67`, SMS code `1234` (no real SMS is sent).

## Stage 1 features (client)

- Sign in with phone number and code (JWT access + refresh tokens)
- Catalog: city, search, categories, sorting by rating or price, favorites
- Specialist profile: services, reviews, about, working hours
- Choosing a free time slot; double booking is blocked by a PostgreSQL exclusion constraint (HTTP 409)
- My bookings: upcoming / past, cancel, rate a completed visit
- English and Turkish UI

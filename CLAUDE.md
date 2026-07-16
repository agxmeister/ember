# Ember — Weight Tracking App for Android

## Features
- Log weigh-ins (daily or weekly) via a wheel picker; edit or delete past entries.
- Set a goal (starting weight, target, start date); the app derives the direction (lose/gain).
- Choose units (kg or lbs).
- Optional reminders, daily or weekly at a chosen time/day, delivered as notifications.
- Smart tracking: groups readings into time-of-day clusters and normalizes them so the time of weigh-in doesn't skew trends; the Check-in screen shows the current cluster.
- Monitor screen visualizes progress — equalizer view, weekly medians, a closeness score, streaks, volatility, weekly rate-of-change zones, and a projected ETA to the goal. The trend tolerates data gaps and hides when readings go stale.
- Monitor widgets show pending/locked states until enough measurements accumulate; readiness thresholds are tunable.
- Progress-aware coloring: UI accents shift with how close the latest weight is to the target.
- Contextual help dialogs throughout the UI; their icons are highlighted until first opened, and can be hidden entirely via a setting.
- Guided onboarding, plus a "start over" flow that can import historical weigh-ins.
- Light / dark / auto theme.
- Localized in English, German, French, Italian, and Spanish, with in-app language switching.
- Opt-out analytics (Aptabase) tracking weigh-in events with logging coverage.
- Settings split across main, additional, and development screens; the development screen tunes the approximation/clustering/readiness parameters.

## Principles
- SOLID principles are the foundation for all design decisions.
- Keep CLAUDE.md lightweight — don't duplicate what's obvious from the code.

## Stack
- Kotlin, Jetpack Compose, Room, Vico, WorkManager, Hilt, Material 3

## Architecture
- Clean architecture: data / domain / presentation layers
- Domain layer owns interfaces; data layer implements them

## Configuration
- User-configurable algorithm constants live in `AlgorithmConfig` (`domain/model/AlgorithmConfig.kt`)
- Add new tunable parameters there; expose via `UserPreferencesRepository.algorithmConfig` flow

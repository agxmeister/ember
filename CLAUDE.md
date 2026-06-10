# Ember — Weight Tracking App for Android

## Features
- Log weigh-ins (daily or weekly) via a wheel picker; edit or delete past entries.
- Set a goal (starting weight, target, start date); the app derives the direction (lose/gain).
- Choose units (kg or lbs).
- Optional reminders, daily or weekly at a chosen time/day, delivered as notifications.
- Smart tracking: groups readings into time-of-day clusters and normalizes them so the time of weigh-in doesn't skew trends; the home screen shows the current cluster.
- Trends screen visualizes progress — equalizer/candle view, weekly medians, a closeness score, streaks, delta to target, weekly rate-of-change zones, and a projected ETA to the goal.
- Progress-aware coloring: UI accents shift with how close the latest weight is to the target.
- Guided onboarding, plus a "start over" flow that can import historical weigh-ins.
- Light / dark / auto theme.
- Localized in English, German, and French, with in-app language switching.
- Hidden developer settings to tune the approximation/clustering parameters.

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

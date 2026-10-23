# SafeCircle AI — Women's Safety & Live Guardian Platform

SafeCircle AI is a comprehensive women's safety and live guardian ecosystem built with **Kotlin, Jetpack Compose (Material 3), Hilt, Room, and Firebase (Auth, Firestore, Cloud Messaging)** alongside an interactive **SafeCircle AI Web Command Center**.

## Key Features

- **Multi-Channel Emergency SOS Engine**: 5-second countdown with 1-tap cancel or instant SOS dispatch, broadcasting live GPS coordinates and recording 30-second encrypted audio evidence.
- **Live Geospatial Guardian Radar**: Real-time location sharing with trusted circles, safe-zone geofences (`Home`, `Metro`, `Campus`), and offline-resilient Room database GPS queueing (`LocationDao`).
- **AI Safety Trainer & Neural Route Scorer**: Evaluates commute corridors based on street-lighting luminance, CCTV & police patrol proximity, and 24/7 safe-haven density.
- **Smart Deterrence & Escape Toolkit**:
  - **Fake Escape Call**: Realistic incoming guardian call screen with live call timer.
  - **110dB Deterrent Siren**: High-decibel alternating frequency alarm.
  - **Automated Safe Check-In Timer**: Countdown timer that automatically dispatches SOS if the user does not check in.
  - **Cab Ride Monitor**: Logs vehicle registration plate and monitors route deviation.
- **Anti-Stalkerware Mutual Consent Circles**: Explicit, revocable per-member consent ledger with 6-character invite codes.
- **24/7 Verified Safe Havens & Helplines**: One-tap access to `1091` (Women Helpline), `112` (National ERSS), `108` (Medical Emergency), and `181`.

## Project Structure

- `app/` — Android application module (Kotlin, Jetpack Compose, Hilt DI, Room DB, Firebase Auth/Firestore)
- `web/index.html` — Standalone interactive SafeCircle AI Web Command Center & Neural Risk Training Studio
- `firebase/` — Firebase security rules and Cloud Functions

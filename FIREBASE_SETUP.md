# SafeCircle — Complete Firebase & Google Cloud Setup Guide

Follow these steps to connect **SafeCircle** to your own Firebase project and Google Maps/Places APIs.

---

## 1. Create the Firebase Project & Register the Android App

1. Open the [Firebase Console](https://console.firebase.google.com/) and click **Add project** → Name it `SafeCircle`.
2. Enable **Google Analytics** (required for Crashlytics).
3. In the Project Overview, click the **Android icon** to add an app:
   - **Android package name**: `com.safecircle.app`
   - **App nickname**: `SafeCircle Android`
   - **Debug signing certificate SHA-1** (required for Phone OTP Auth, Google Maps restriction, and App Check):
     Run this in your Android Studio terminal:
     ```bash
     ./gradlew signingReport
     ```
     Or via keytool on Windows:
     ```powershell
     keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
     Copy both the **SHA-1** and **SHA-256** fingerprints into the Firebase Console.
4. Download `google-services.json` and replace [`app/google-services.json`](file:///c:/Users/prabu/womens%20safety/app/google-services.json).

---

## 2. Enable Firebase Services

1. **Authentication**:
   - Go to **Build → Authentication → Sign-in method**.
   - Enable **Email/Password**.
   - Enable **Phone** (add test phone numbers if testing on emulators without SIM cards).
2. **Realtime Database**:
   - Go to **Build → Realtime Database → Create Database**.
   - Choose your region and start in **Locked mode**.
   - Open the **Rules** tab and paste the contents of [`firebase/database.rules.json`](file:///c:/Users/prabu/womens%20safety/firebase/database.rules.json), then click **Publish**.
3. **Storage**:
   - Go to **Build → Storage → Get started**.
   - Open the **Rules** tab and paste the contents of [`firebase/storage.rules`](file:///c:/Users/prabu/womens%20safety/firebase/storage.rules), then click **Publish**.
4. **Cloud Messaging (FCM)**:
   - Enabled automatically. Verify under **Project Settings → Cloud Messaging** that **Firebase Cloud Messaging API (V1)** is enabled.
5. **Crashlytics**:
   - Go to **Release & Monitor → Crashlytics → Enable Crashlytics**.
6. **App Check**:
   - Go to **Build → App Check → Apps**.
   - Register `com.safecircle.app` with **Play Integrity** for release builds.
   - For debug builds, `SafeCircleApp` automatically installs `DebugAppCheckProviderFactory` and logs a debug token to Logcat (`DebugAppCheckProvider: Enter this debug secret into the allow list`). Copy that token into **App Check → Manage debug tokens**.

---

## 3. Deploy Cloud Functions (Push Fan-Out & Join Code Validation)

Note: Cloud Functions require the Firebase **Blaze (pay-as-you-go)** plan (includes generous free tier).

```bash
npm install -g firebase-tools
firebase login
cd firebase/functions
npm install
firebase deploy --only functions
```

---

## 4. Google Maps SDK & Places API Key

1. Open the [Google Cloud Console](https://console.cloud.google.com/) and select your `SafeCircle` project.
2. Go to **APIs & Services → Library** and enable:
   - **Maps SDK for Android**
   - **Places API**
3. Go to **APIs & Services → Credentials → Create Credentials → API key**.
4. Restrict the key to **Android apps** (`com.safecircle.app` + your SHA-1 fingerprint) and restrict APIs to **Maps SDK for Android** and **Places API**.
5. Paste your key into [`gradle.properties`](file:///c:/Users/prabu/womens%20safety/gradle.properties):
   ```properties
   MAPS_API_KEY=AIzaSyYourActualGoogleMapsKey
   ```


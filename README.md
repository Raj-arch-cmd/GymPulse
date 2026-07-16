# GymPulse 🏋️‍♂️📊

GymPulse is a modern Android application built to streamline the gym experience for both members and gym owners. Utilizing background geofencing for automated check-ins and Google's Gemini API for personalized fitness analytics, GymPulse provides real-time gym capacity tracking and smart insights.

<!-- Tech Badges for a professional look -->
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)
![Gemini API](https://img.shields.io/badge/Gemini%20API-121212?style=for-the-badge&logo=google&logoColor=white)

---

## 🛠️ Tech Stack

The project is built entirely in **Kotlin** using modern Android development practices and clean architectural design patterns.

*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** Model-View-ViewModel (MVVM) with Repositories and StateFlow
*   **Navigation:** Jetpack Navigation Compose
*   **Authentication:** Firebase Authentication (Email/Password & Google Sign-In)
*   **Database:** Firebase Firestore (for storing Users, Gyms, and Sessions)
*   **Location & Geofencing:** Google Play Services Location API (`GeofencingClient`, `GeofenceBroadcastReceiver`)
*   **Artificial Intelligence:** Google Gemini API (`com.google.ai.client.generativeai.GenerativeModel`)
*   **Asynchronous Programming:** Kotlin Coroutines & Flows

---

## ✨ Features

### 🔐 Authentication & Roles
*   **Multi-Role Support:** Users are assigned roles as either a `"member"` or an `"owner"`.
*   **Secure Login:** Features a seamless `LoginScreen` and `RegisterScreen` supporting both standard credentials and Google Sign-In via `GoogleAuthProvider`.
*   **Role-Based Routing:** Automatically redirects users to the `MemberHomeScreen` or `OwnerHomeScreen` upon login based on their account type.

### 📍 Automated Geofence Check-Ins
*   **Smart Automation:** Replaces physical key fobs by using a `GeofenceManager` and a `GeofenceBroadcastReceiver` to detect when a member enters or exits the gym's perimeter.
*   **Session Tracking:** Automatically creates or concludes a `Session` (recording `checkInTime` and `checkOutTime`) in the background.
*   **Push Notifications:** Alerts users via system notifications when they have been successfully checked in or out using the `GeofenceNotificationHelper`.

### 🤖 GymPulse AI Insights
*   **Gemini Integration:** Uses the `AnalyticsViewModel` and `AnalyticsRepository` to interact with a Generative AI model.
*   **Smart Dashboard:** The `MemberHomeScreen` features a *"GymPulse AI Insights"* card that provides users with dynamically generated text insights, loading states, and error handling for retry attempts.

### 🏢 Gym Management & Live Capacity
*   **Live Counts:** The `GymViewModel` tracks and broadcasts the `currentCount` of members currently at a specific location, allowing users to see how busy the gym is in real time before arriving.
*   **Gym Selection:** Members can browse and select their preferred gym location using the `SelectGymScreen`.
*   **Owner Tools:** Owners can register their gym's geographic location (using `LocationServices` to fetch latitude and longitude coordinates) from the `OwnerHomeScreen`.

---

## 🚀 Project Status & Roadmap

While the core geofencing and analytics features are fully scaffolded, the project is actively under development. Several UI panels and backend implementations are currently mapped out as part of the roadmap:

- [ ] **Gamification UI:** Build out the leaderboard screens and repository logic to utilize the `points` and `streakCount` properties stored in the User model.
- [ ] **Session History Panel:** Add a `"History"` route in `AppNavigation` to let members view their past workouts recorded in the `Session` model (`dayOfWeek`, `hourSlot`, etc.).
- [ ] **Owner Dashboard Depth:** Expand the `OwnerHomeScreen` beyond basic location configurations into an advanced dashboard displaying live capacity, member management lists, and analytics.
- [ ] **Profile & Settings Screen:** Create dedicated navigation routes for users to update profile information, switch selected gyms, or perform password resets.
- [ ] **Auto-Checkout Fallback:** Implement an Android `WorkManager` routine to forcefully check out users who bypass the geofence exit trigger (e.g., if a device runs out of battery), utilizing the `autoCheckedOut` boolean flag.
- [ ] **Data Validation & UI Polish:** Add loading shimmers, visual feedback, and empty states across screens like `SelectGymScreen`.

# Implementation Plan - Phase 1: Core Session Engine

This plan outlines the architectural blueprint for Phase 1, focusing on establishing a reliable manual session engine and real-time occupancy tracking, while removing automated check-in logic.

## User Review Required

> [!IMPORTANT]
> **Data Integrity:** To ensure the "Pulse" (occupancy) is accurate, we will strictly use Firestore Transactions. This prevents the gym counter from drifting out of sync with active sessions.
> **Auto Check-in Removal:** This phase will completely disable the `GEOFENCE_TRANSITION_DWELL` trigger for check-ins in the `GeofenceBroadcastReceiver`.

## Architectural Blueprint

### 1. Firestore Schema & Data Models

#### [MODIFY] [Gym.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/model/Gym.kt)
- Add `maxCapacity: Int` (Hardcoded to 30 for this phase).

#### [MODIFY] [Session.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/model/Session.kt)
- Standardize `sessionStatus` using strings: `"active"`, `"manual_checkout"`, `"auto_checkout"`. (Constants cleanup moved to end of phase).

### 2. Repository Layer (The Engine)

#### [MODIFY] [SessionRepository.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/repository/SessionRepository.kt)
- **Atomic Operations:** Wrap `checkIn` and `checkOut` logic in a `firestore.runTransaction`.
    - **Check-in:** Create session doc AND increment `gyms/{gymId}/currentCount` in one atomic step.
    - **Check-out:** Update session doc AND decrement `gyms/{gymId}/currentCount`.
- **Validation:** Add a check during check-in to ensure the user doesn't already have an `ACTIVE` session elsewhere.

#### [MODIFY] [GymRepository.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/repository/GymRepository.kt)
- Remove `updateGymCount` (it will be handled by `SessionRepository` to ensure atomicity).
- Add `getGymCapacityInfo` to retrieve thresholds.

### 3. ViewModel Layer (State Management)

#### [REUSE] `SessionViewModel` & `GymViewModel`
- `SessionViewModel`: Focuses on the user's current session state (`isCheckedIn`).
- `GymViewModel`: Focuses on the "Pulse".
    - Implement hardcoded range logic for crowd level:
        - 0–10  → "Light"
        - 11–20 → "Moderate"
        - 21–30 → "Heavy"
        - 31+   → "Very Heavy"

### 4. Background Services (Cleanup)

#### [MODIFY] [GeofenceBroadcastReceiver.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/geofence/GeofenceBroadcastReceiver.kt)
- **REMOVE** the loitering (`DWELL`) trigger that calls `sessionRepository.checkIn`.
- Retain the `EXIT` trigger but modify it for Phase 2 (for now, it should only log or be disabled until the notification logic is ready).

### 5. UI Layer (Compose)

#### [MODIFY] [MemberHomeScreen.kt](file:///Users/rajsingh/AndroidStudioProjects/GymPulse/app/src/main/java/com/example/gympulse/ui/theme/screens/MemberHomeScreen.kt)
- Update the occupancy card to display the crowd level label (e.g., "Light Crowd") based on the new `OccupancyState`.
- Add an informational warning message when occupancy exceeds 30: "⚠️ The gym is currently above its recommended capacity. Equipment may be limited."
- Simplify the check-in button to be the single source of truth for session control.

---

## Data Flow: Check-In Logic

1.  **UI:** User taps "Check In" button.
2.  **ViewModel:** `SessionViewModel.checkIn(userId, gymId)` is triggered.
3.  **Repository:** `SessionRepository.checkIn` initiates a Firestore Transaction.
4.  **Firestore (Transaction):**
    - Checks if `gyms/{gymId}` exists and fetches `currentCount`.
    - Writes a new `sessions` document with `status: "ACTIVE"`.
    - Updates `gyms/{gymId}` by incrementing `currentCount`.
5.  **Real-time Update:**
    - `GymViewModel`'s snapshot listener on the `gyms` document detects the change.
    - UI updates the "Pulse" counter and crowd level badge instantly.
    - `SessionViewModel`'s snapshot listener on the `sessions` query detects the active session, updating the button to "Check Out".

---

## Architectural Issues to Address
- **Transaction Reliability:** Ensure the app handles network failures during transactions gracefully (Firestore handles retries, but UI should show an error if it ultimately fails).
- **String Constant Centralization:** Before starting, I will create a `Constants` object to replace magic strings like `"active"` and `"owner"`.

## Verification Plan

### Automated Tests
- Unit tests for `OccupancyState` calculation logic.
- Integration tests (if possible) for Firestore transactions.

### Manual Verification
- Verify that entering a geofence NO LONGER triggers an auto-check-in.
- Verify that manual check-in updates the global gym counter for all users in real-time.
- Verify that the crowd level label (Light/Moderate/Heavy) changes correctly as occupancy increases.

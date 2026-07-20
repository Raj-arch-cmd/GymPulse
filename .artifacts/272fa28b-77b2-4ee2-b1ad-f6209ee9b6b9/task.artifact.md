# Phase 1: Core Session Engine Implementation Roadmap

This roadmap breaks down the implementation of Phase 1 into small, testable chunks. Tasks are ordered to prioritize feature development.

## Task 1: Enhance Gym Model & Firestore Schema
- **Goal:** Add `maxCapacity` to the `Gym` model (fixed at 30).
- **Files to modify:** `Gym.kt`, `GymRepository.kt`
- **Dependencies:** None
- **Outcome:** `Gym` objects have a `maxCapacity` property.
- **Git Commit:** `feat: add maxCapacity to Gym model`
- **Status:** [x] Done

## Task 2: Disable Auto Check-in Logic
- **Goal:** Remove geofence-triggered check-ins to ensure manual-only sessions.
- **Files to modify:** `GeofenceBroadcastReceiver.kt`, `GeofenceManager.kt`
- **Dependencies:** None
- **Outcome:** Geofence `DWELL` transition no longer triggers check-in.
- **Git Commit:** `refactor: remove geofence-triggered auto check-in`
- **Status:** [x] Done

## Task 3: Atomic Check-in Transaction
- **Goal:** Use Firestore transactions to keep `currentCount` in sync with active sessions.
- **Files to modify:** `SessionRepository.kt`
- **Dependencies:** Task 1
- **Outcome:** Check-in creates a session and increments gym count atomically.
- **Git Commit:** `feat: implement atomic check-in transaction`
- **Status:** [x] Done

## Task 4: Atomic Check-out Transaction
- **Goal:** Use Firestore transactions to decrement count on checkout.
- **Files to modify:** `SessionRepository.kt`, `Session.kt`
- **Dependencies:** Task 3
- **Outcome:** Check-out updates session and decrements gym count atomically.
- **Git Commit:** `feat: implement atomic check-out transaction`
- **Status:** [x] Done

## Task 5: Repository Cleanup
- **Goal:** Remove redundant count update methods in `GymRepository`.
- **Files to modify:** `GymRepository.kt`
- **Dependencies:** Task 4
- **Outcome:** `SessionRepository` is the sole owner of occupancy count changes.
- **Git Commit:** `refactor: remove redundant gym count update logic`

## Task 6: Crowd Level Logic in ViewModel
- **Goal:** Implement fixed-range crowd level labels (0-10 Light, 11-20 Moderate, 21-30 Heavy, 31+ Very Heavy).
- **Files to modify:** `GymViewModel.kt`
- **Dependencies:** Task 1
- **Outcome:** ViewModel exposes the calculated crowd label.
- **Git Commit:** `feat: add fixed-range crowd level calculation`

## Task 7: UI Update
- **Goal:** Update the dashboard to show the new crowd level labels and a warning if occupancy exceeds 30.
- **Files to modify:** `MemberHomeScreen.kt`
- **Dependencies:** Task 6
- **Outcome:** Dashboard displays "Light", "Moderate", "Heavy", or "Very Heavy" and a warning if capacity is exceeded.
- **Git Commit:** `ui: update dashboard with crowd level labels and capacity warning`

## Task 8: Constants Cleanup
- **Goal:** Centralize roles and session status strings to prevent typos.
- **Files to create:** `util/Constants.kt`
- **Files to modify:** `AuthRepository.kt`, `SessionRepository.kt`, `AuthViewModel.kt`, etc.
- **Dependencies:** Task 7
- **Outcome:** Codebase uses unified constants for all key strings.
- **Git Commit:** `chore: centralize status and role constants`

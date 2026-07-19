You are working on GymPulse, a native Android application.

Tech Stack:
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Repository Pattern
- Firebase Authentication
- Cloud Firestore
- Material 3

Rules:
1. Preserve the existing MVVM architecture.
2. Modify only the minimum number of files required.
3. Never refactor unrelated code.
4. Never rename files, packages, or classes unless explicitly requested.
5. Explain which files will be modified before making changes.
6. Never remove existing functionality.
7. Prefer small, incremental changes over large rewrites.
8. Follow Kotlin best practices.
9. Use Firestore update() for partial document updates instead of overwriting documents unless explicitly requested.
10. If requirements are unclear, ask questions instead of making assumptions.
11. Keep code readable and production-ready.
12. Do not change Gradle files, dependencies, or project configuration unless explicitly requested.
13. Preserve Firebase Authentication and Firestore data models.
14. After implementing a feature, summarize exactly what changed.
15. If a task affects multiple modules, propose a plan before writing code.
16. Never analyze or modify the entire project unless explicitly requested. Restrict analysis to the files related to the current task.
17. Never change package names or folder structure.

18. Preserve existing UI unless the task specifically requests UI changes.

19. If a Firestore write is performed, use partial updates (update()) whenever possible instead of replacing entire documents.

20. Before modifying more than 3 files, list the files and wait for confirmation.
# TumbasPOS

TumbasPOS is a modern, offline-first Point of Sale (POS) application built natively for Android using Kotlin and Jetpack Compose. It is designed to help small businesses manage sales, inventory, and reporting efficiently.

## Features

*   **Point of Sale (POS):**
    *   Streamlined checkout process.
    *   Support for discounts and tax calculations.
    *   Guest checkout or Customer assignment.
*   **Inventory Management:**
    *   Manage products, categories, and stock levels.
    *   **Barcode Scanning:** Integrated CameraX barcode scanner for quick product lookup.
*   **Hardware Support:**
    *   **Thermal Printing:** Support for Bluetooth ESC/POS thermal printers for printing receipts.
*   **Data Management:**
    *   **Offline-First:** Built on Room Database for reliable offline access.
    *   **Backup & Restore:** Secure cloud backup and restore functionality using Cloudflare R2.
*   **Security:**
    *   **Activation System:** Secure device activation using App ID and License Keys.
*   **Reporting:**
    *   View sales history and basic analytics.

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Koin
*   **Database:** Room
*   **Async/Concurrency:** Coroutines & Flow
*   **Networking:** Ktor (for Backup/Restore)
*   **Camera:** CameraX & ML Kit (for Barcode Scanning)

## Build & Run

To build and run the application:

1.  Open the project in Android Studio.
2.  Sync Gradle project.
3.  Run the `app` configuration.

Or via command line:

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the GNU General Public License v3.0 (GPLv3) - see the [LICENSE](LICENSE) file for details.
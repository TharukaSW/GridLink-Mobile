# GridLink Mobile Application
**Pure Native Android Application (Kotlin) with Local SQLite Database**
*Module: SE4040 Enterprise Application Development - Assignment 1*

---

## 👥 Project Team & Member Allocations

| # | Registration No. | Name with Initials | Email Address | Individual Assigned Module |
|:-:|:----------------:|:-------------------|:--------------|:---------------------------|
| 1 | **IT23373266** | **Sachintha W C D T** | [it23373266@my.sliit.lk](mailto:it23373266@my.sliit.lk) | **Authentication & User Management** |
| 2 | **IT23217218** | **O.R. Fernando** | [it23217218@my.sliit.lk](mailto:it23217218@my.sliit.lk) | **Microgrid Management** |
| 3 | **IT23137646** | **Ajanth R** | [it23137646@my.sliit.lk](mailto:it23137646@my.sliit.lk) | **Prosumer Management** |
| 4 | **IT23442948** | **Arachchi M Y K** | [it23442948@my.sliit.lk](mailto:it23442948@my.sliit.lk) | **Energy Reservation & Booking** |

## 🔗 Official GitHub Repositories
- **Backend API**: [https://github.com/TharukaSW/GridLink-API](https://github.com/TharukaSW/GridLink-API)
- **Web Interface**: [https://github.com/TharukaSW/GridLink-Web-Interface](https://github.com/TharukaSW/GridLink-Web-Interface)
- **Mobile Application**: [https://github.com/TharukaSW/GridLink-Mobile](https://github.com/TharukaSW/GridLink-Mobile)

---

## 1. Overview & Architecture

GridLink Mobile is a pure native Android client written in **Kotlin** designed for the **Smart Solar Microgrid Trading System**. It adheres strictly to the assignment specifications and marking rubric:

- **FAT Service Architecture Compliance**: All backend business logic (reservation rules, user status transitions, capacity validation) resides strictly in the centralized ASP.NET Core C# Web API. The mobile application operates purely as an intuitive, rich client interface communicating exclusively via RESTful API calls.
- **Pure Native Android with Local SQLite Persistence**: Built without any prohibited cross-platform frameworks (no React Native, Flutter, Cordova, etc.). All local user sessions, authentication tokens, profile data, and reference caches (stations and bookings) are persisted using native Android **SQLite** (`android.database.sqlite.SQLiteOpenHelper`).
- **Dual Role Support**:
  - **Solar Prosumer**: Property owners who trade solar energy with the microgrid.
  - **Grid Operator**: Field and site engineers responsible for verifying transactions via QR codes, updating battery slot availability, and monitoring microgrid stations.

---

## 2. Key Features Implemented

### A. Authentication & Account Management
1. **Role-Based Login**:
   - Unified login interface routing Prosumers directly to the **Prosumer Dashboard** and Grid Operators to the **Operator Mode Hub**.
   - Includes convenient Demo Quick-Fill buttons for both roles.
   - Live server configuration dialog to switch base URLs (`http://10.0.2.2:5015/` for Android Emulator or custom LAN IPs) with integrated server ping tests.
2. **Prosumer Registration**:
   - Registers prosumers using **National Identity Card (NIC)** as the primary key.
   - Validates NIC length (10–12 characters), full name, email, phone number, address, and password.
3. **Prosumer Profile Management**:
   - View profile attributes and account status (`Active`, `PendingActivation`, `DeactivationRequested`).
   - Self-service profile editing (Name, Phone number, Address) with immediate SQLite session sync.
4. **Account Deactivation Request**:
   - Prosumers can submit account deactivation requests directly from the app.
   - Enforces the business rule that deactivated accounts can only be reactivated by a Backoffice officer.

### B. Reservation Workflow & Booking Management
1. **Create Energy Slot Booking**:
   - Prosumers reserve solar drop-off (selling) or grid charging (buying) slots.
   - **7-Day Rule Enforced**: Validates that reservations are scheduled strictly within the upcoming 7 days.
   - Configurable requested energy (kWh), duration (minutes), and instant estimated trading tariff preview.
2. **Summary Page After Each Action**:
   - Implements dedicated receipt summary pages after **Create**, **Update**, and **Cancel** actions (fulfilling the 2-mark rubric requirement).
3. **12-Hour Notice Rule on Updates & Cancellations**:
   - Updates and cancellations require at least 12 hours advance notice before the scheduled slot time.
   - App checks slot timestamps and locks actions if fewer than 12 hours remain, giving clear feedback.
4. **Transaction QR Dispatch**:
   - Once a booking is approved/confirmed, the app generates and displays a high-resolution, secure transaction QR code using ZXing.

### C. Booking Views & Operational Dashboard
1. **Live KPI Dashboard**:
   - Real-time counts fetched live from the API:
     - Pending reservations count
     - Count of approved future reservations
     - Total reservations count
2. **Current / Pending Bookings & Full Booking History**:
   - Tabbed interface separating *Active & Pending* from *History (Completed / Cancelled)*.
   - Real-time keyword search filter (by booking number, station, or NIC).
   - Trading direction filter chips (*All*, *Drop-Off*, *Charging*).

### D. Google Maps & Solar Grid Nodes
1. **Nearby Hubs Discovery**:
   - Plots solar microgrid stations from stored GPS latitude and longitude.
   - Proximity search radius filters (15 km, 30 km, 50 km, 100 km).
   - Station details bottom sheet displays: Station Code, Station Name, Address, Capacity (kW/h), Available Battery Slots / Total Slots, Operating Hours, and Status.
   - **"Book Energy Slot Here"** button directly opens the booking form with the station pre-selected.

### E. Grid Operator Mode
1. **Operator Dashboard**:
   - Displays pending QR verification jobs and overall system trading statistics.
2. **Transaction QR Code Scanner**:
   - Built with AndroidX CameraX live preview and camera reticle overlay.
   - Manual QR payload testing input for seamless execution on emulators.
   - Verifies scanned prosumer QR payload against central server (`POST /api/reservations/verify`).
   - Displays verified prosumer name, NIC, station, and requested energy.
3. **Finalize Energy Transfer (Mark Job as Done)**:
   - Operator inputs actual energy transferred (kWh) and notes.
   - Calls `PUT /api/reservations/{id}/finalize` to complete business logic on the server and mark the booking as completed.
4. **Battery Storage Slot Management**:
   - Operators can view all microgrid hubs and update available battery slots (`PATCH /api/stations/{id}/battery-slots`).

---

## 3. Project Structure

```
GridLink-Mobile/
├── app/
│   ├── build.gradle.kts          # Dependencies (Retrofit, CameraX, ZXing, Maps, Material3)
│   └── src/main/
│       ├── AndroidManifest.xml   # Permissions (Internet, Camera, Location, Cleartext HTTP)
│       └── java/com/example/gridlink/app/
│           ├── MainActivity.kt   # App entry point
│           ├── data/
│           │   ├── local/
│           │   │   ├── GridLinkDbHelper.kt       # Native SQLite database helper
│           │   │   ├── SessionManager.kt         # SQLite user session & settings storage
│           │   │   ├── LocalStationCache.kt      # Offline SQLite stations cache
│           │   │   └── LocalReservationCache.kt  # Offline SQLite reservations cache
│           │   ├── remote/
│           │   │   ├── dto.kt                    # DTOs matching ASP.NET Core models
│           │   │   ├── GridLinkApi.kt            # Retrofit REST API interface
│           │   │   └── ApiClient.kt              # Dynamic Retrofit client with Bearer auth
│           │   └── repository/
│           │       ├── AuthRepository.kt         # Login, registration & session sync
│           │       ├── ProsumerRepository.kt     # Profile & deactivation request
│           │       ├── ReservationRepository.kt  # Bookings, QR verify, finalize transfer
│           │       └── StationRepository.kt      # Stations & battery slot updates
│           ├── util/
│           │   ├── QRCodeHelper.kt               # ZXing QR Bitmap generator
│           │   └── DateUtils.kt                  # ISO formatting, 7-day & 12-hour rules
│           └── ui/
│               ├── theme/                        # Solar emerald, amber & dark palette
│               ├── components/
│               │   ├── GridLinkAppBar.kt         # Header with branding & server config
│               │   ├── StatusBadge.kt            # Color-coded status chips
│               │   └── ServerConfigDialog.kt     # In-app server URL configuration & ping
│               ├── screens/
│               │   ├── auth/
│               │   │   ├── LoginScreen.kt        # Role-based login & demo quick fills
│               │   │   └── RegisterScreen.kt     # Prosumer registration (NIC primary key)
│               │   ├── prosumer/
│               │   │   ├── ProsumerHomeScreen.kt         # Prosumer live KPI dashboard
│               │   │   ├── CreateReservationScreen.kt    # Energy booking form (7-day rule)
│               │   │   ├── BookingsListScreen.kt         # Filterable bookings & history
│               │   │   ├── ReservationDetailScreen.kt    # QR code display & 12h actions
│               │   │   ├── ActionSummaryScreen.kt        # Summary receipt after each action
│               │   │   ├── ProsumerProfileScreen.kt      # Profile & deactivation request
│               │   │   └── StationMapScreen.kt           # Google Maps nearby hubs visualizer
│               │   └── operator/
│               │       ├── OperatorHomeScreen.kt         # Operator dashboard
│               │       ├── OperatorQrScannerScreen.kt    # Camera QR verify & finalization
│               │       ├── OperatorStationsScreen.kt     # Battery slot availability updater
│               │       └── OperatorReservationsScreen.kt # System-wide bookings monitor
│               └── navigation/
│                   └── AppNavigation.kt          # Role-based router & back stack manager
```

---

## 4. How to Build & Run

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 21 (bundled in Android Studio or OpenJDK 21)
- Android SDK Platform 36 & Build-Tools

### Running the App
1. Open the project in Android Studio:
   - File -> Open -> Select `d:\Other Porjects\Modifications\GridLink-Mobile`
2. Ensure your ASP.NET Core API (`GridLink-API`) is running on port `5015`.
3. Launch an Android Virtual Device (AVD) or connect a physical Android device via USB debugging.
4. Run the app (`Shift + F10` or tap the Run button in Android Studio).
5. If running in the Android Emulator, the default API Base URL `http://10.0.2.2:5015/` connects directly to your Windows host machine. You can also tap the Settings gear icon on the top app bar to adjust or test the server connection at any time.

### Default Seeded System Accounts (from MongoDbContext.cs)
- **Grid Operator**:
  - NIC / Identifier: `OPER00001V` (or Email: `operator@gridlink.com`)
  - Password: `Operator@123`
- **Backoffice Administrator**:
  - NIC / Identifier: `ADMIN00001V` (or Email: `admin@gridlink.com`)
  - Password: `Admin@123`
- **Solar Prosumer**:
  - Prosumers can self-register using the **"Register Account"** link on the mobile app or web portal using their NIC as primary key (e.g. NIC: `200012345678`, Password: `Password123!`).
  - *(Tapping the "Prosumer Demo" button on the login screen pre-fills `200012345678` / `Password123!`)*.

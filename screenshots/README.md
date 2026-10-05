# GridLink Mobile Application — Screenshots Gallery

This directory contains high-resolution native screenshots of all 14 screens from the **GridLink Mobile Application** (built with Kotlin + Jetpack Compose for Android), verified against the **EAD SE4040 Assignment Specification** and running against the **.NET 8 Web API** and **SQLite** persistence layer.

---

## Screen Inventory

| File Name | Screen Description | Functional Role | Key Features |
|---|---|:---:|---|
| [`01_login_screen.png`](./01_login_screen.png) | Unified Dual-Role Login | Auth | Prosumer/Operator sign-in with NIC/Email, password masking toggle, server settings gear icon |
| [`02_server_settings.png`](./02_server_settings.png) | Dynamic Server Config Dialog | System | Real-time API base URL configuration saved into local SQLite database |
| [`03_register_screen.png`](./03_register_screen.png) | Prosumer Registration | Prosumer | Sri Lankan NIC key validation (9-digit+V/X and 12-digit), contact inputs, PendingActivation notice |
| [`04_prosumer_dashboard.png`](./04_prosumer_dashboard.png) | Prosumer Live Dashboard | Prosumer | Live KPI counters (Pending, Approved, Total), quick trading action shortcuts, active reservations overview |
| [`05_create_reservation_screen.png`](./05_create_reservation_screen.png) | Reserve Energy Slot | Prosumer | 7-day advance booking window, 12h rule banner, hub picker with capacity & hours, tariff estimator, slot time chips |
| [`06_station_map_screen.png`](./06_station_map_screen.png) | Solar Microgrid OpenStreetMap | Prosumer | Interactive Leaflet OpenStreetMap with real Sri Lanka geospatial tiles, radius filter chips (15km-100km), GPS centering, interactive pins with popups, and preview card |
| [`06b_station_map_list_view.png`](./06b_station_map_list_view.png) | Microgrid Station List Explorer | Prosumer | List view mode toggle showing all nearby stations, capacity, slots, hours, and 1-tap 'Select & Book Energy Slot' button |
| [`07_my_bookings_screen.png`](./07_my_bookings_screen.png) | My Energy Bookings | Prosumer | Filter tabs (Active & Pending, History, All), search bar, trading direction chips, status badges |
| [`08_reservation_detail_screen.png`](./08_reservation_detail_screen.png) | Reservation Details & QR | Prosumer | Live ZXing 2D transaction QR barcode for operator scanning, reservation details, 12h cancellation/modification rule enforcement |
| [`09_action_summary_screen.png`](./09_action_summary_screen.png) | Action Summary Receipt | Prosumer | Digital confirmation receipt after create, update, or cancel actions with reference number, hub, time, and status |
| [`10_prosumer_profile_screen.png`](./10_prosumer_profile_screen.png) | Prosumer Profile Management | Prosumer | Profile details synced with SQLite session, edit contact info, account deactivation request submission |
| [`10b_deactivate_user_dialog.png`](./10b_deactivate_user_dialog.png) | Account Deactivation Request Dialog | Prosumer | Modal confirmation dialog explaining the backoffice reactivation rule with 'Confirm Request' & 'Cancel' |
| [`11_operator_dashboard.png`](./11_operator_dashboard.png) | Operator Control Hub | Operator | System-wide operational stats (Pending, Completed Trades, Total System), quick task shortcuts, pending dispatch queue |
| [`12_operator_qr_scanner_screen.png`](./12_operator_qr_scanner_screen.png) | Operator Camera QR Scanner | Operator | Camera viewfinder with active ZXing scanner, manual booking reference entry fallback, and server verification |
| [`13_operator_battery_slots_screen.png`](./13_operator_battery_slots_screen.png) | Station Battery Storage Slots | Operator | Real-time station capacity and available battery slots management (`8 / 10 Slots`, etc.) |
| [`14_operator_reservations_screen.png`](./14_operator_reservations_screen.png) | Global Reservations Monitor | Operator | System-wide multi-criteria search and filter by NIC, booking reference, station, and status |

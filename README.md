# TBH Monitor Mobile

Companion mobile app untuk **[TBH Monitor](https://github.com/naufal-backup/tbh-monitor)** (TaskbarHero Background Monitor) — aplikasi desktop yang membaca save file game **TaskbarHero** dan mengekspos datanya (gold, heroes, inventory, rune, pet) lewat REST API + QR code.

Tujuan repo ini: menyediakan client mobile agar data tersebut bisa dipantau langsung dari HP, tanpa perlu buka desktop app.

> 🚧 **Status: Planning.** Repo & README dibuat duluan, development belum dimulai.

## Latar Belakang

`tbh-monitor` (desktop, Rust/egui) sudah punya fitur local API server + generate QR code untuk akses cepat dari HP. Endpoint yang tersedia saat server jalan:

| Endpoint              | Deskripsi                           |
|------------------------|---------------------------------------|
| `GET /api/data`        | Seluruh save data mentah              |
| `GET /api/player`      | Data player (hero, item, rune, dll)   |
| `GET /api/inventory`   | Daftar item inventory saja            |

Mobile app ini akan jadi client yang consume endpoint-endpoint tersebut (scan QR code dari desktop app untuk connect ke local server, biasanya lewat ngrok/local network).

## Rencana Fitur

- [ ] Scan QR code untuk connect ke `tbh-monitor` desktop (auto-fill base URL API)
- [ ] Dashboard — ringkasan gold, jumlah hero, item, progres rune
- [ ] Heroes — detail level, EXP, ability points, skill & gear per slot
- [ ] Inventory — daftar item dengan search, filter kategori, sorting
- [ ] Runes & Pets — progres rune tree dan daftar pet/companion
- [ ] Auto-refresh data secara berkala

## Tech Stack

Belum diputuskan (TBD) — kandidat: Flutter, React Native, atau native (Kotlin/Swift). Akan diupdate begitu development dimulai.

## Getting Started

Belum tersedia — project masih tahap planning. Instruksi setup akan ditambahkan begitu development dimulai.

## Repo Terkait

- Backend/desktop app: [naufal-backup/tbh-monitor](https://github.com/naufal-backup/tbh-monitor)

## Disclaimer

Project independen/fan-made, tidak berafiliasi dengan developer resmi TaskbarHero.

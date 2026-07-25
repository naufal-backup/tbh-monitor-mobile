# TBH Monitor Mobile

Companion mobile app untuk TBH Monitor (TaskbarHero Background Monitor), desktop app yang baca save file game TaskbarHero dan expose datanya (gold, heroes, inventory, rune, pet) lewat REST API + QR code.

Backend: https://github.com/naufal-backup/tbh-monitor

Status: masih planning, belum ada development.

## Latar belakang

tbh-monitor (desktop) punya local API server + QR code buat akses dari HP. Endpoint yang tersedia:

- GET /api/data - seluruh save data mentah
- GET /api/player - data player (hero, item, rune, dll)
- GET /api/inventory - daftar item inventory

Mobile app ini nantinya jadi client yang consume endpoint-endpoint itu, scan QR dari desktop app buat connect ke server-nya.

## Rencana fitur

- Scan QR code buat connect ke desktop app
- Dashboard ringkasan gold, hero, item, progres rune
- Detail hero: level, EXP, ability points, skill & gear
- Inventory dengan search, filter, sorting
- Rune tree & pet/companion
- Auto-refresh data

## Tech stack

Kotlin (native Android).

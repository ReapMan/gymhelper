<div align="center">

# 🏋️‍♂️ GymHelper

**Современный, ультрабыстрый и надежный трекер силовых тренировок для зала**  
*Создан спортсменами для спортсменов. Чистый оффлайн, AMOLED Dark тема, мгновенный учет подходов и максимальная защита данных.*

[![Version](https://img.shields.io/badge/Version-1.0.1%20Stable-00E676?style=for-the-badge&logo=android)](https://github.com/ReapMan/gymhelper/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-2979FF?style=for-the-badge&logo=android)](https://github.com/ReapMan/gymhelper)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![DB](https://img.shields.io/badge/Storage-SQLite%20WAL-00E5FF?style=for-the-badge&logo=sqlite)](https://www.sqlite.org/wal.html)
[![Creator](https://img.shields.io/badge/Creator-reapman-FF1744?style=for-the-badge)](https://github.com/ReapMan)

</div>

---

## 📸 Скриншоты интерфейса

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Главный экран</b></td>
      <td align="center"><b>Сценарий тренировки</b></td>
      <td align="center"><b>Цветовые схемы</b></td>
      <td align="center"><b>Фоновый баннер</b></td>
      <td align="center"><b>Меню и бэкапы</b></td>
    </tr>
    <tr>
      <td><img src="screenshots/main_screen.png" width="180" alt="Главный экран"/></td>
      <td><img src="screenshots/workout_active.png" width="180" alt="Активная тренировка"/></td>
      <td><img src="screenshots/themes_settings.png" width="180" alt="Цветовые схемы"/></td>
      <td><img src="screenshots/templates_banner.png" width="180" alt="Фоновый баннер"/></td>
      <td><img src="screenshots/menu_about.png" width="180" alt="Меню приложения"/></td>
    </tr>
  </table>
</div>

---

## ⚡ Ключевые возможности

### 🎨 Кастомизация и AMOLED Dark дизайн
- **Чистый глубокий черный фон** — экономит заряд батареи на OLED/AMOLED экранах во время долгих тренировок.
- **6 спортивных акцентных схем:**
  - 🟢 **Зеленый Неон** (Cyber Green — по умолчанию)
  - 🔵 **Кибер-синий** (Ice Cyan)
  - 🟠 **Огненный** (Flame Amber)
  - 🟣 **Фиолетовый Неон** (Electric Purple)
  - 🔴 **Красный Рубин** (Crimson Fury)
  - 🟡 **Золотой Титан** (Titanium Gold)
- Мгновенное переключение тем оформления во всем приложении в один клик.

### 🏋️‍♂️ Управление тренировочным процессом
- **Свободная тренировка** или **Запуск по готовой программе** из папок сплитов.
- **Роадмап сценария:** добавление, замена и перемещение упражнений на лету.
- **Фоновый режим тренировки:** при переходе в историю или программы внизу появляется горизонтальный виджет с цифровым моноширинным секундомером и быстрым возвратом в зал.
- **Фишки подходов:** учет разминки, дропсетов, работы до отказа и негативных повторений.
- **Интеллектуальные подсказки:** показ веса и повторений из прошлого подхода.

### 🛡️ Промышленная надежность данных
- **SQLite WAL-режим (Write-Ahead Logging):** максимальное быстродействие и защита от повреждения базы при внезапных сбоях.
- **Crash Recovery:** если телефон выключится или сядет аккумулятор во время тренировки, при перезапуске таймер и весь прогресс подходов автоматически восстанавливаются.
- **Автобэкап после каждой тренировки:** локальное автосохранение `gym_backup_auto.json`.
- **Полный Snapshot Backup & Restore:** экспорт всей базы тренировок, программ и справочников в JSON-файл для переноса на новый телефон в пару кликов.

### 🤖 Экспорт для ИИ (AI Data Export)
- Выгрузка тренировочных объемов и истории в структурированном виде **Markdown** и **JSON**.
- Готовые промпты для анализа периодизации, прогрессивной перегрузки и восстановления в **ChatGPT**, **Claude** и **Gemini**.

---

## 📲 Скачать и установить

Готовый подписанный релизный APK доступен прямо в репозитории:

- 📥 **[Скачать GymHelper-v1.0.1-reapman.apk](GymHelper-v1.0.1-reapman.apk)**

> [!TIP]
> Приложение подписано официальным ключом релиза. При установке поверх предыдущей версии все данные и история тренировок сохраняются автоматически.

---

## 🛠 Технологический стек

- **Язык:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Архитектура:** MVVM (Model-View-ViewModel) + StateFlow
- **База данных:** SQLite 3 (WAL mode, Foreign Keys enforcement)
- **Сборка:** Gradle 8.x + Android Gradle Plugin 8.9.x
- **Совместимость:** Android 7.0 (API 24) и новее

---

## 👤 Создатель

Разработано и поддерживается: **[reapman](https://github.com/ReapMan)**  
GymHelper — твой персональный напарник в зале.

# Быстрый старт / Quick Start

## Установка / Installation

1. **Требования / Requirements:**
   - PaperMC сервер версии 1.21.x
   - Java 21 или выше

2. **Установка плагина / Install Plugin:**
   ```bash
   # Скопируйте JAR файл в папку plugins вашего сервера
   # Copy the JAR file to your server's plugins folder
   cp target/story-plugin-1.0.0.jar /path/to/your/server/plugins/
   ```

3. **Запуск сервера / Start Server:**
   - Запустите сервер / Start the server
   - Плагин создаст конфигурационные файлы / Plugin will create configuration files
   - Остановите сервер для настройки / Stop server to configure

4. **Настройка (опционально) / Configure (optional):**
   - Отредактируйте `plugins/MmmmStoryPlugin/config.yml`
   - Настройте диалоги в `dialogs.yml`
   - Настройте сообщения в `messages_ru.yml`

## Первый запуск / First Run

1. **Запустите кампанию / Start Campaign:**
   ```
   /story start
   ```

2. **Основные команды / Basic Commands:**
   - `/story progress` - проверить прогресс / check progress
   - `/story reload` - перезагрузить конфиг / reload config
   - `/story skip <act>` - пропустить к акту / skip to act (admin)

3. **Игровой процесс / Gameplay:**
   - НПЦ появится у спавна / NPC will spawn at world spawn
   - Следуйте указаниям / Follow the instructions
   - Каждая 3-я ночь - волны скелетов / Every 3rd night - skeleton waves
   - Ищите Забытый Алтарь (алмазный блок) / Find Forgotten Altar (diamond block)
   - Найдите Узел Перекрёстков (изумрудный блок) / Find Crossroads Node (emerald block)

## Плейсхолдеры структур / Structure Placeholders

Пока структуры не загружены, используются плейсхолдеры:
Until structures are loaded, placeholders are used:

- **Забытый Алтарь / Forgotten Altar:** Алмазный блок / Diamond Block
- **Узел Перекрёстков / Crossroads Node:** Изумрудный блок / Emerald Block
- **Призыв Босса №1 / Boss 1 Summon:** Красный незеритовый кирпич / Red Nether Bricks
- **Призыв Босса №2 / Boss 2 Summon:** Плачущий обсидиан / Crying Obsidian

## Отладка / Debug

- Проверьте логи: `logs/latest.log`
- Проверьте данные: `plugins/MmmmStoryPlugin/data/global.yml`
- Сбросить прогресс: `/story reset all`

## Поддержка / Support

При возникновении проблем проверьте:
If you encounter issues, check:

1. Версию Paper / Paper version (`/version`)
2. Версию Java / Java version (`java -version`)
3. Логи сервера / Server logs
4. Права доступа / Permissions (`story.admin`)

---

Удачной игры! / Have fun!

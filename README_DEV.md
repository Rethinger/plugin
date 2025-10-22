# 🎮 MMMM Story Plugin - Developer Guide

## 📚 Документация

### Основные документы:
1. **[СЮЖЕТ_ФИНАЛЬНЫЙ.md](СЮЖЕТ_ФИНАЛЬНЫЙ.md)** - Полный сценарий с диалогами и эффектами
2. **[ИНСТРУКЦИЯ_СТРУКТУРЫ.md](ИНСТРУКЦИЯ_СТРУКТУРЫ.md)** - Создание и импорт структур
3. **[ТЕХНИЧЕСКИЕ_ТРЕБОВАНИЯ.md](ТЕХНИЧЕСКИЕ_ТРЕБОВАНИЯ.md)** - Текущий статус и TODO лист

---

## 🚀 Быстрый старт

### Компиляция:
```bash
cd plugin
mvn clean package
```

### Установка:
```bash
cp target/story-plugin-1.0.0.jar server/plugins/
```

### Перезагрузка конфига:
```bash
/story reload
```

---

## 🎯 Текущий статус

### ✅ Готово (40%):
- Акт 1: Посланник, волны, алтари
- Акт 2: Босс №1 с механиками
- Система диалогов с эффектами
- Менеджеры (Config, Data, Item, Dialog)
- Отслеживание прогресса

### ⚠️ В разработке (30%):
- Босс №2 (Изверг)
- Структуры (спавн и импорт)
- Акт 3: Эндермены, стражи, фантомы

### 📋 Планируется (30%):
- Акт 4: Артефакты в городах
- Акт 5: Ритуал запечатывания
- Финальный босс (будущее)

---

## 🔧 Архитектура

### Основные пакеты:
```
com.mmmm.story/
├── commands/          # Команды плагина
├── listeners/         # Обработчики событий
│   ├── Act1Listener   # Волны, алтари
│   ├── Act2Listener   # Боссы 1-2
│   ├── Act3Listener   # Край
│   ├── Act4Listener   # Артефакты
│   └── Act5Listener   # Финал
├── managers/          # Менеджеры систем
│   ├── ConfigManager
│   ├── DataManager
│   ├── DialogManager
│   ├── ItemManager
│   ├── NPCManager
│   ├── StructureManager
│   └── PlayerPlacedBlocksManager
└── MmmmStoryPlugin    # Главный класс
```

### Конфигурация:
```
src/main/resources/
├── plugin.yml         # Метаданные плагина
├── config.yml         # Основные настройки
├── dialogs.yml        # Диалоги с таймингами
├── items.yml          # Сюжетные предметы
└── structures/        # .nbt файлы структур
```

---

## 💻 Примеры кода

### Добавление нового диалога:
```yaml
# dialogs.yml
my_dialog:
  hologram: false
  actionBar: true
  title: false
  lines:
    - delay: 0
      text: "&6NPC: &7Привет!"
      sound: "ENTITY_VILLAGER_AMBIENT"
    - delay: 3
      text: "&eЗадание: Найди структуру"
      sound: "ENTITY_PLAYER_LEVELUP"
```

### Воспроизведение диалога:
```java
// Для одного игрока
plugin.getDialogManager().playDialog(player, "my_dialog");

// Для всех игроков
plugin.getDialogManager().playDialogForAll("my_dialog");
```

### Создание сюжетного предмета:
```java
ItemStack item = plugin.getItemManager().createStoryItem("stabilization_core");
player.getInventory().addItem(item);
```

### Проверка прогресса:
```java
DataManager data = plugin.getDataManager();

// Проверить достижение
if (data.isBoss1Defeated()) {
    // Босс 1 побеждён
}

// Выдать достижение
data.giveAchievement(player, "boss1_defeated");
```

### Спавн босса с диалогами:
```java
private void spawnBoss(Location loc) {
    // 1. Проиграть диалог призыва
    dialogManager.playDialogForAll("boss.summon");
    
    // 2. Заспавнить босса через 13 секунд (после диалогов)
    new BukkitRunnable() {
        @Override
        public void run() {
            Skeleton boss = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
            boss.setCustomName("§4Босс");
            // ... настройка босса
        }
    }.runTaskLater(plugin, 260L); // 13 секунд
}
```

---

## 🎨 Визуальные эффекты

### Партиклы:
```java
// Круг партикл
Location center = player.getLocation();
for (int i = 0; i < 360; i += 10) {
    double rad = Math.toRadians(i);
    double x = center.getX() + Math.cos(rad) * 3;
    double z = center.getZ() + Math.sin(rad) * 3;
    Location loc = new Location(world, x, center.getY(), z);
    world.spawnParticle(Particle.PORTAL, loc, 1, 0, 0, 0, 0);
}

// Луч света вверх
for (int y = 0; y < 50; y++) {
    Location loc = center.clone().add(0, y, 0);
    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
}
```

### Звуки:
```java
// Обычный звук
player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

// Для всех игроков
for (Player p : Bukkit.getOnlinePlayers()) {
    p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 2.0f, 0.8f);
}
```

### Эффекты на игрока:
```java
// Левитация
player.addPotionEffect(new PotionEffect(
    PotionEffectType.LEVITATION, 
    1200,  // 60 секунд
    1,     // уровень 2
    false, // не ambient
    false  // не показывать партиклы
));

// Слепота
player.addPotionEffect(new PotionEffect(
    PotionEffectType.BLINDNESS,
    400,   // 20 секунд
    255    // максимальный уровень
));
```

---

## 🐛 Отладка

### Логирование:
```java
// INFO
plugin.getLogger().info("Message");

// WARNING
plugin.getLogger().warning("Warning message");

// SEVERE
plugin.getLogger().severe("Error message");
```

### Отладочные команды:
```bash
# Информация о плагине
/story debug

# Телепорт к структуре
/story tp boss1_altar

# Выдать предмет
/story give <player> <item>

# Пропустить к акту
/story skip 2

# Перезагрузить конфиг
/story reload
```

### Проверка структур:
```bash
# Проверить загруженные структуры
/story debug structures

# Заспавнить структуру вручную
/story spawn_structure forgotten_altar
```

---

## 📦 Зависимости

### Обязательные:
- **Paper API 1.21.4+** - Серверная платформа
- **Java 21** - JDK для компиляции

### Опциональные:
- **Citizens** - Для NPC (если нужны)
- **WorldEdit** - Для создания структур
- **MythicMobs** - Для кастомных боссов (будущее)

### Maven dependencies:
```xml
<dependencies>
    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 🧪 Тестирование

### Тестовый сервер:
```bash
# Запустить тестовый сервер
cd test-server
./start.sh  # или start.bat на Windows

# Подключиться
minecraft://localhost:25565
```

### Сценарии тестирования:

**Акт 1:**
1. Первый вход → Посланник появляется
2. Дождаться 3-й ночи → Волна скелетов
3. Найти Забытый Алтарь → Получить ядро
4. Найти Узел → Активировать портал

**Акт 2:**
1. Войти в Ад
2. Найти алтарь призыва
3. Призвать Босса №1
4. Проверить механики:
   - Фазы (100 HP)
   - Притягивание сверху (5 сек)
   - Анти-застройка (8+ блоков)
   - Воины не дерутся между собой
5. Победить босса → Волны прекращаются

---

## 🔐 Безопасность

### Права доступа:
```yaml
permissions:
  story.admin:      # Админ команды
  story.debug:      # Отладка
  story.bypass:     # Обход ограничений
```

### Проверка прав в коде:
```java
if (!player.hasPermission("story.admin")) {
    player.sendMessage("§cНедостаточно прав!");
    return;
}
```

---

## 📊 Производительность

### Оптимизация:

1. **Партиклы:**
   - Не спамить каждый тик
   - Использовать разумное количество
   - Batch spawning

2. **Структуры:**
   - Кешировать загруженные .nbt
   - Async размещение больших структур

3. **Боссы:**
   - Лимит воинов (max 15)
   - Проверка дистанции перед расчётами
   - Кулдауны на механики

4. **Диалоги:**
   - BukkitRunnable для задержек
   - Не дублировать звуки

---

## 📝 Соглашения о коде

### Стиль:
```java
// Классы: PascalCase
public class MyManager { }

// Методы: camelCase
public void myMethod() { }

// Константы: UPPER_SNAKE_CASE
private static final int MAX_HEALTH = 200;

// Переменные: camelCase
private int playerCount;
```

### Комментарии:
```java
/**
 * Спавнит босса на указанной локации
 * @param location Локация спавна
 * @return Заспавненный босс
 */
public Skeleton spawnBoss(Location location) {
    // Диалоги призыва
    dialogManager.playDialogForAll("boss.summon");
    
    // Спавн через 13 секунд
    // ...
}
```

---

## 🤝 Вклад в проект

### Как добавить новую функцию:

1. **Изучите документацию:**
   - Прочитайте СЮЖЕТ_ФИНАЛЬНЫЙ.md
   - Проверьте ТЕХНИЧЕСКИЕ_ТРЕБОВАНИЯ.md

2. **Создайте ветку:**
   ```bash
   git checkout -b feature/my-feature
   ```

3. **Напишите код:**
   - Следуйте стилю проекта
   - Добавьте комментарии
   - Тестируйте локально

4. **Сделайте коммит:**
   ```bash
   git add .
   git commit -m "feat: add my feature"
   ```

5. **Создайте PR:**
   - Опишите изменения
   - Прикрепите скриншоты (если UI)

---

## 📞 Поддержка

### Проблемы:
- Проверьте логи: `logs/latest.log`
- Используйте `/story debug`
- Проверьте config.yml на ошибки

### Ошибки компиляции:
```bash
# Очистить и пересобрать
mvn clean
mvn compile
mvn package
```

### Контакты:
- GitHub Issues: [создать issue](https://github.com/your-repo/issues)
- Discord: (если есть сервер)

---

## 🗺️ Roadmap

### Версия 1.0 (Текущая)
- [x] Акт 1: Посланник, волны, алтари
- [x] Акт 2: Босс №1
- [ ] Акт 2: Босс №2
- [ ] Акт 3: Край (базовые механики)

### Версия 1.1
- [ ] Акт 3: Полная реализация
- [ ] Акт 4: Артефакты
- [ ] Акт 5: Ритуал запечатывания

### Версия 2.0 (Будущее)
- [ ] Измерение Тьмы
- [ ] Финальный босс
- [ ] Кинематографичные кат-сцены
- [ ] Альтернативные концовки

---

## 📄 Лицензия

Этот проект распространяется под лицензией MIT.

---

**Удачи в разработке!** 🚀✨

*"Когда печати ломаются, тьма пробуждается..."*

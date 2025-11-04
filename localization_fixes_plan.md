# План исправления локализации обыска сундуков

## Проблемы обнаруженные:

1. **Жестко закодированные сообщения в ChestSpawnManager.java** (строки 409-411):
   ```java
   String notifyText = lang.equals("en") 
       ? "✓ Material found in this structure!"
       : "✓ Материал найден в этой структуре!";
   ```

2. **Отсутствует ключ локализации** для сообщения о нахождении материала в структуре

3. **Несоответствие сообщений** между разными файлами локализации

## Необходимые исправления:

### 1. Добавить ключи локализации

#### В файл `src/main/resources/messages_ru.yml`:
```yaml
# Chest search messages
chest:
  search:
    searching: "🔍 Обыск сундука..."
    searching_item: "Искомый предмет: {0}"
    found: "✓ НАЙДЕНО: {0}!"
    success: "✓ УСПЕХ!"
    nothing: "✗ В этом сундуке ничего не найдено ({0}/{1})"
    broke: "✗ Сундук сломался после 3 неудачных попыток!"
    cooldown: "✗ Этот сундук уже был обыскан недавно ({0} сек)"
    material_found: "✓ Материал найден в этой структуре!"
```

#### В файл `src/main/resources/messages_en.yml`:
```yaml
# Chest search messages
chest:
  search:
    searching: "🔍 Searching chest..."
    searching_item: "Searching for: {0}"
    found: "✓ FOUND: {0}!"
    success: "✓ SUCCESS!"
    nothing: "✗ Nothing found in this chest ({0}/{1})"
    broke: "✗ Chest broke after 3 failed attempts!"
    cooldown: "✗ This chest was already searched recently ({0} sec)"
    material_found: "✓ Material found in this structure!"
```

### 2. Исправить ChestSpawnManager.java

Заменить жестко закодированные сообщения (строки 409-411):
```java
// Было:
String notifyText = lang.equals("en") 
    ? "✓ Material found in this structure!"
    : "✓ Материал найден в этой структуре!";

// Стало:
String notifyText = messageManager.getMessage(p, "chest.search.material_found");
```

### 3. Проверить сообщения о ядре стабилизации

Убедиться, что все сообщения о ядре стабилизации переведены:
- `items.stabilization_core.name` - "Ядро Стабилизации" ✓ (уже переведено)
- `items.stabilization_core.lore` - описание на русском ✓ (уже переведено)

### 4. Проверить системные сообщения при успешном обыске

Убедиться, что все сообщения при успешном обыске используют систему локализации:
- `chest.search.found` - "✓ НАЙДЕНО: {0}!" ✓ (уже переведено)
- `chest.search.success` - "✓ УСПЕХ!" ✓ (уже переведено)

## Файлы для изменения:

1. `src/main/resources/messages_ru.yml` - добавить ключ `chest.search.material_found`
2. `src/main/resources/messages_en.yml` - добавить ключ `chest.search.material_found`
3. `src/main/java/com/mmmm/story/managers/ChestSpawnManager.java` - заменить жестко закодированные сообщения

## Тестирование:

После внесения изменений проверить:
1. При обыске сундука сообщения отображаются на русском языке
2. При нахождении ядра стабилизации сообщение переведено
3. Системные сообщения при успешном обыске переведены
4. Нет смешения языков (русский/английский) в одном сообщении
# 📦 Руководство по созданию текстур-пака для сюжетных предметов

## 🎨 Для арт-девелопера

Это руководство поможет вам создать кастомные текстуры для сюжетных предметов плагина Mmmm Story Plugin.

---

## 📋 Список сюжетных предметов

### Акт 1: Верхний Мир
1. **Ядро Стабилизации** (`stabilization_core`)
   - Базовый предмет: `NETHER_STAR`
   - CustomModelData: `1001`
   - Описание: Ядро с энергией разрушенного портала
   - Цветовая схема: Фиолетовый, черный, синий

### Акт 2: Нижний Мир
2. **Ключ Призыва Босса №1** (`boss1_summon_key`)
   - Базовый предмет: `BLAZE_ROD`
   - CustomModelData: `1002`
   - Описание: Горящий ключ для призыва Повелителя Скелетов
   - Цветовая схема: Красный, оранжевый, черный

3. **Фрагмент Гнева** (`boss1_material`)
   - Базовый предмет: `NETHERITE_SCRAP`
   - CustomModelData: `1003`
   - Описание: Осколок силы побежденного босса
   - Цветовая схема: Темно-красный, серый, черный

4. **Катализатор Пустоты** (`boss1_catalyst`)
   - Базовый предмет: `ECHO_SHARD`
   - CustomModelData: `1004`
   - Описание: Кристалл с энергией Нижнего мира
   - Цветовая схема: Темно-фиолетовый, черный

5. **Ключ Врат Эндера** (`overworld_portal_key`)
   - Базовый предмет: `END_CRYSTAL`
   - CustomModelData: `1005`
   - Описание: Ключ, открывающий врата в Край
   - Цветовая схема: Зеленый, фиолетовый, белый

### Акт 4: Дальние Земли Края
6-10. **Артефакты Энда** (`end_artifact_1` - `end_artifact_5`)
   - Базовый предмет: `AMETHYST_SHARD`
   - CustomModelData: `1006`, `1007`, `1008`, `1009`, `1010`
   - Описание: Древние артефакты с силой Края
   - Цветовая схема: Фиолетовый, белый, черный
   - Уникальные особенности: Каждый артефакт должен быть уникален!

---

## 🛠️ Как создать текстур-пак

### Шаг 1: Создание структуры папок

```
MyStoryTexturePack/
├── pack.mcmeta
├── pack.png (иконка пака 128x128)
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── nether_star.json
        │       ├── blaze_rod.json
        │       ├── netherite_scrap.json
        │       ├── echo_shard.json
        │       ├── end_crystal.json
        │       └── amethyst_shard.json
        └── textures/
            └── item/
                └── story/
                    ├── stabilization_core.png
                    ├── boss1_summon_key.png
                    ├── boss1_material.png
                    ├── boss1_catalyst.png
                    ├── overworld_portal_key.png
                    ├── end_artifact_1.png
                    ├── end_artifact_2.png
                    ├── end_artifact_3.png
                    ├── end_artifact_4.png
                    └── end_artifact_5.png
```

### Шаг 2: Создание pack.mcmeta

Создайте файл `pack.mcmeta` в корне папки:

```json
{
  "pack": {
    "pack_format": 34,
    "description": "Mmmm Story Plugin Custom Textures"
  }
}
```

**Примечание:** `pack_format: 34` для Minecraft 1.21.x

### Шаг 3: Создание JSON моделей

#### Пример для `nether_star.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/nether_star"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1001
      },
      "model": "minecraft:item/story/stabilization_core"
    }
  ]
}
```

#### Пример для `blaze_rod.json`:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/blaze_rod"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1002
      },
      "model": "minecraft:item/story/boss1_summon_key"
    }
  ]
}
```

#### Пример для `amethyst_shard.json` (с несколькими артефактами):

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/amethyst_shard"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1006
      },
      "model": "minecraft:item/story/end_artifact_1"
    },
    {
      "predicate": {
        "custom_model_data": 1007
      },
      "model": "minecraft:item/story/end_artifact_2"
    },
    {
      "predicate": {
        "custom_model_data": 1008
      },
      "model": "minecraft:item/story/end_artifact_3"
    },
    {
      "predicate": {
        "custom_model_data": 1009
      },
      "model": "minecraft:item/story/end_artifact_4"
    },
    {
      "predicate": {
        "custom_model_data": 1010
      },
      "model": "minecraft:item/story/end_artifact_5"
    }
  ]
}
```

### Шаг 4: Создание моделей предметов

Создайте файлы моделей в `assets/minecraft/models/item/story/`:

#### Пример `stabilization_core.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/stabilization_core"
  }
}
```

Повторите для всех предметов, заменяя имя текстуры.

### Шаг 5: Создание текстур (PNG файлы)

Создайте PNG файлы размером **16x16 пикселей** в `assets/minecraft/textures/item/story/`:

#### Рекомендации по дизайну:

**1. Ядро Стабилизации** (`stabilization_core.png`)
- Сфера/ядро с пульсирующей энергией
- Фиолетовые и синие оттенки
- Эффект свечения по краям
- Трещины с энергией внутри

**2. Ключ Призыва Босса №1** (`boss1_summon_key.png`)
- Горящий жезл/ключ
- Красно-оранжевое пламя
- Темная основа (obsidian/netherite)
- Руны на поверхности

**3. Фрагмент Гнева** (`boss1_material.png`)
- Острый осколок
- Темно-красный с черными прожилками
- Эффект "злой" энергии
- Неровные края

**4. Катализатор Пустоты** (`boss1_catalyst.png`)
- Кристалл неправильной формы
- Темно-фиолетовый, почти черный
- Эффект пустоты/искажения
- Светящиеся вкрапления

**5. Ключ Врат Эндера** (`overworld_portal_key.png`)
- Изящный кристалл
- Зеленые и фиолетовые оттенки
- Эффект End Portal (звездное небо)
- Геометрическая форма

**6-10. Артефакты Энда** (`end_artifact_1-5.png`)
- Каждый УНИКАЛЕН!
- Фиолетовая база
- Различные формы:
  - Артефакт 1: Кристалл
  - Артефакт 2: Сфера с руной
  - Артефакт 3: Осколок
  - Артефакт 4: Призма
  - Артефакт 5: Звезда
- Добавьте уникальные детали для каждого

---

## 🎨 Советы по созданию текстур

### Общие рекомендации:
1. **Размер:** Всегда 16x16 пикселей (можно масштабировать до 32x32 для детализации)
2. **Стиль:** Соответствуйте ванильному стилю Minecraft
3. **Читаемость:** Предмет должен быть узнаваем в инвентаре
4. **Контраст:** Используйте достаточный контраст для видимости
5. **Прозрачность:** Используйте альфа-канал для краев

### Цветовые палитры:

**Нижний Мир:**
- `#8B0000` (Темно-красный)
- `#FF4500` (Оранжевый)
- `#2C1810` (Темно-коричневый)
- `#FFD700` (Золотой)

**Край:**
- `#8B00FF` (Фиолетовый)
- `#E0B0FF` (Светло-фиолетовый)
- `#FFFFFF` (Белый)
- `#1C1C1C` (Черный)

**Порталы:**
- `#00CED1` (Бирюзовый)
- `#9370DB` (Средний фиолетовый)
- `#7FFF00` (Зеленый)

### Эффекты свечения:
- Добавьте светлые пиксели по краям (1-2px)
- Используйте градиенты от светлого к темному
- Создайте "ауру" вокруг ключевых элементов

---

## 📦 Экспорт и использование

### Шаг 1: Упаковка
1. Выберите всю папку `MyStoryTexturePack`
2. Создайте ZIP-архив
3. Переименуйте в `MmmmStoryTextures.zip`

### Шаг 2: Установка
1. Откройте `.minecraft/resourcepacks/`
2. Скопируйте туда `MmmmStoryTextures.zip`
3. В игре: `Options` → `Resource Packs`
4. Переместите пак в "Selected"

### Шаг 3: Тестирование
1. Запустите сервер с плагином
2. Используйте команду: `/story give <игрок> <предмет>`
3. Проверьте текстуры в инвентаре
4. Убедитесь что CustomModelData работает

---

## 🔧 Отладка

### Проблема: Текстура не отображается
**Решения:**
1. Проверьте `pack_format` в `pack.mcmeta`
2. Убедитесь что путь к текстуре правильный
3. Проверьте что CustomModelData совпадает
4. Убедитесь что PNG файлы 16x16px

### Проблема: Отображается ванильная текстура
**Решения:**
1. Проверьте JSON override в базовом предмете
2. Убедитесь что модель существует
3. Перезагрузите ресурс-пак (F3+T)

### Проблема: Пак не загружается
**Решения:**
1. Проверьте синтаксис JSON (используйте jsonlint.com)
2. Убедитесь что структура папок правильная
3. Проверьте логи игры на ошибки

---

## 📝 Чеклист для арт-девелопера

- [ ] Создана структура папок
- [ ] Создан `pack.mcmeta`
- [ ] Создана иконка пака `pack.png`
- [ ] Созданы JSON модели для базовых предметов
- [ ] Созданы JSON модели для кастомных предметов
- [ ] Нарисованы все 10 текстур (16x16px)
- [ ] Проверены пути к файлам
- [ ] Проверены CustomModelData значения
- [ ] Создан ZIP-архив
- [ ] Протестировано в игре
- [ ] Все текстуры отображаются корректно

---

## 🎯 CustomModelData Reference

| Предмет | Базовый Item | CustomModelData |
|---------|--------------|-----------------|
| Ядро Стабилизации | NETHER_STAR | 1001 |
| Ключ Призыва Босса №1 | BLAZE_ROD | 1002 |
| Фрагмент Гнева | NETHERITE_SCRAP | 1003 |
| Катализатор Пустоты | ECHO_SHARD | 1004 |
| Ключ Врат Эндера | END_CRYSTAL | 1005 |
| Артефакт Энда 1 | AMETHYST_SHARD | 1006 |
| Артефакт Энда 2 | AMETHYST_SHARD | 1007 |
| Артефакт Энда 3 | AMETHYST_SHARD | 1008 |
| Артефакт Энда 4 | AMETHYST_SHARD | 1009 |
| Артефакт Энда 5 | AMETHYST_SHARD | 1010 |

---

## 📚 Полезные ресурсы

- **Minecraft Wiki:** https://minecraft.wiki/w/Resource_pack
- **Custom Model Data:** https://minecraft.wiki/w/Tutorials/Models
- **JSON Validator:** https://jsonlint.com/
- **Pixel Art Editor:** https://www.piskelapp.com/
- **Color Picker:** https://colorhunt.co/

---

## 🤝 Контакты и поддержка

Если возникли вопросы или нужна помощь:
1. Проверьте этот гайд еще раз
2. Посмотрите примеры текстур Minecraft
3. Свяжитесь с разработчиком плагина

**Удачи в создании потрясающих текстур! 🎨✨**

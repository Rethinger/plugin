# 📦 JSON Templates для Текстур-Пака

Готовые шаблоны JSON файлов для быстрого старта.

---

## 📋 pack.mcmeta

```json
{
  "pack": {
    "pack_format": 34,
    "description": "§dMmmm Story Plugin §7Custom Textures\n§8by YourName"
  }
}
```

---

## 🔧 Базовые предметы (models/item/)

### nether_star.json
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

### blaze_rod.json
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

### netherite_scrap.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/netherite_scrap"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1003
      },
      "model": "minecraft:item/story/boss1_material"
    }
  ]
}
```

### echo_shard.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/echo_shard"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1004
      },
      "model": "minecraft:item/story/boss1_catalyst"
    }
  ]
}
```

### end_crystal.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/end_crystal"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1005
      },
      "model": "minecraft:item/story/overworld_portal_key"
    }
  ]
}
```

### amethyst_shard.json
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

---

## 🎨 Кастомные модели (models/item/story/)

### stabilization_core.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/stabilization_core"
  }
}
```

### boss1_summon_key.json
```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/story/boss1_summon_key"
  }
}
```

### boss1_material.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/boss1_material"
  }
}
```

### boss1_catalyst.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/boss1_catalyst"
  }
}
```

### overworld_portal_key.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/overworld_portal_key"
  }
}
```

### end_artifact_1.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/end_artifact_1"
  }
}
```

### end_artifact_2.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/end_artifact_2"
  }
}
```

### end_artifact_3.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/end_artifact_3"
  }
}
```

### end_artifact_4.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/end_artifact_4"
  }
}
```

### end_artifact_5.json
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/story/end_artifact_5"
  }
}
```

---

## 📁 Итоговая структура файлов

```
MmmmStoryTextures/
├── pack.mcmeta
├── pack.png
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── nether_star.json
        │       ├── blaze_rod.json
        │       ├── netherite_scrap.json
        │       ├── echo_shard.json
        │       ├── end_crystal.json
        │       ├── amethyst_shard.json
        │       └── story/
        │           ├── stabilization_core.json
        │           ├── boss1_summon_key.json
        │           ├── boss1_material.json
        │           ├── boss1_catalyst.json
        │           ├── overworld_portal_key.json
        │           ├── end_artifact_1.json
        │           ├── end_artifact_2.json
        │           ├── end_artifact_3.json
        │           ├── end_artifact_4.json
        │           └── end_artifact_5.json
        └── textures/
            └── item/
                └── story/
                    ├── stabilization_core.png (16x16)
                    ├── boss1_summon_key.png (16x16)
                    ├── boss1_material.png (16x16)
                    ├── boss1_catalyst.png (16x16)
                    ├── overworld_portal_key.png (16x16)
                    ├── end_artifact_1.png (16x16)
                    ├── end_artifact_2.png (16x16)
                    ├── end_artifact_3.png (16x16)
                    ├── end_artifact_4.png (16x16)
                    └── end_artifact_5.png (16x16)
```

---

## 🚀 Быстрый старт

1. **Создайте структуру папок** как показано выше
2. **Скопируйте все JSON** из этого файла в соответствующие места
3. **Создайте PNG текстуры** (16x16 пикселей) в папке `textures/item/story/`
4. **Создайте pack.png** (иконка пака 128x128)
5. **Упакуйте в ZIP** и переименуйте в `MmmmStoryTextures.zip`
6. **Установите** в `.minecraft/resourcepacks/`
7. **Активируйте** в настройках игры

---

## ✅ Проверочный список

- [ ] Все JSON файлы созданы
- [ ] Все пути правильные
- [ ] CustomModelData совпадают с плагином
- [ ] Все PNG текстуры 16x16px
- [ ] pack.mcmeta содержит правильный pack_format
- [ ] Структура папок соответствует примеру
- [ ] ZIP-архив создан корректно
- [ ] Протестировано в игре

---

**Готово! Теперь у вас есть все шаблоны для создания текстур-пака! 🎨**

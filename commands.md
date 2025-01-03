# Команды плагина `SpawnProtectionPlugin`

Этот плагин включает в себя несколько команд для управления защитой спавна на сервере Minecraft. Ниже приведены команды, доступные администраторам.

## Доступные команды

### `/spawnprotection help`
**Описание**: Отображает список всех доступных команд и их краткое описание.

**Пример**:

/spawnprotection help

### `/spawnprotection block <игрок>`
**Описание**: Блокирует возможность ломать блоки для указанного игрока.

**Пример**:

/spawnprotection block JustKeel
- **playerName**: Имя игрока, которому необходимо заблокировать возможность ломать блоки.

### `/spawnprotection unblock <игрок>`
**Описание**: Разрешает указанному игроку ломать блоки, убирая его из списка заблокированных.

**Пример**:

/spawnprotection unblock JustKeel
- **playerName**: Имя игрока, которому нужно разрешить ломать блоки.

### `/spawnprotection radius <радиус>`
**Описание**: Устанавливает радиус защиты спавна в блоках, отсчитываемый от координат (0, y, 0). Радиус защищенной зоны ограничивает, насколько далеко игроки могут взаимодействовать с окружающим миром.

**Пример**:

/spawnprotection radius 100
- **радиус**: Положительное целое число, которое задает новый радиус защиты спавна в блоках.

### `/spawnprotection stats [игрок]`
**Описание**: Показывает статистику времени, которое игрок провел на сервере, для выполнения условий на поломку блоков спавна. Если имя игрока не указано, отображается информация для испопользующего игрока.

**Пример**:

/spawnprotection stats
/spawnprotection stats JustKeel
- **playerName** (опционально): Имя игрока, для которого нужно отобразить статистику.

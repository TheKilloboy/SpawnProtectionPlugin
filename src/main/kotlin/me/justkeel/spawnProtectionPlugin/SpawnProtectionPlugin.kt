package me.justkeel.spawnProtectionPlugin

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

class SpawnProtectionPlugin : JavaPlugin(), Listener {

    private lateinit var blockedPlayersManager: BlockedPlayersManager
    private var spawnProtectionRadius = 50 // Радиус защиты спавна (в блоках)

    override fun onEnable() {
        // Проверка и создание конфигурационного файла, если его нет
        if (!dataFolder.exists()) {
            dataFolder.mkdir() // Создаем папку плагина, если ее нет
        }

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            // Если конфиг не существует, создаем его с дефолтными значениями
            saveDefaultConfig() // Этот метод автоматически создает config.yml с дефолтными значениями
        }

        // Загружаем конфигурацию
        saveDefaultConfig()  // Это также сохранит default config, если он не существует
        spawnProtectionRadius = config.getInt("spawn_protection_radius", 50)

        // Регистрация событий
        server.pluginManager.registerEvents(this, this)

        // Инициализация менеджера заблокированных игроков
        blockedPlayersManager = BlockedPlayersManager(this)

        // Регистрация команды
        val command = getCommand("spawnprotection")
        if (command != null) {
            command.setExecutor(CommandHandler())
            command.tabCompleter = CommandHandler()
        } else {
            logger.warning("Команда /spawnprotection не зарегистрирована!")
        }

        logger.info("SpawnProtectionPlugin включён!")
    }

    override fun onDisable() {
        logger.info("SpawnProtectionPlugin выключен!")
    }



    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        // Если игрок заблокирован
        if (blockedPlayersManager.getBlockedPlayers().contains(player.name)) {
            event.isCancelled = true
            player.sendMessage("§cАдминистратор заблокировал возможность ломать блоки на спавне для вас.")
            return
        }

        // Проверяем, находится ли игрок в радиусе защиты спавна
        if (isWithinSpawnProtection(player.location)) {
            // Получаем наигранное время через PlaceholderAPI
            val secondsPlayedString = PlaceholderAPI.setPlaceholders(player, "%statistic_seconds_played%")
            val secondsPlayed = try {
                secondsPlayedString.toInt()
            } catch (e: NumberFormatException) {
                player.sendMessage("§cОшибка при обработке времени: $secondsPlayedString")
                event.isCancelled = true
                return
            }

            val requiredSeconds = 5 * 60 * 60 // 5 часов в секундах

            // Проверка времени
            if (secondsPlayed < requiredSeconds) {
                event.isCancelled = true
                val remainingSeconds = requiredSeconds - secondsPlayed
                val remainingMinutes = floor(remainingSeconds / 60.0).toInt()
                val remainingHours = remainingMinutes / 60
                val displayMinutes = remainingMinutes % 60
                player.sendActionBar("§cОсталось $remainingHours ч. $displayMinutes мин. до возможности ломать блоки.")
            }
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val spawnLocation = server.worlds[0].spawnLocation // Локация спавна
        val explosionLocation = event.location

        // Проверяем, находится ли взрыв в радиусе защиты
        if (isWithinSpawnProtection(explosionLocation)) {
            event.blockList().clear() // Убираем блоки
            event.isCancelled = true
            logger.info("Взрыв заблокирован в зоне защиты спавна!")
        }
    }

    private fun isWithinSpawnProtection(location: Location): Boolean {
        // Координаты спавна: 0, 0, 0
        val spawnLocation = Location(location.world, 0.0, location.y, 0.0)
        // Проверка, находится ли точка в пределах радиуса защиты (только по X и Z)
        return spawnLocation.distanceSquared(location) <= (spawnProtectionRadius * spawnProtectionRadius)
    }

    inner class CommandHandler : TabExecutor {

        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
        ): Boolean {
            if (args.isEmpty()) {
                sender.sendMessage("§cУкажите команду. Используйте §a/spawnprotection help §cдля списка доступных команд.")
                return true
            }

            // Проверка на наличие опки
            if (args[0].lowercase() != "help" && args[0].lowercase() != "stats" && !sender.hasPermission("spawnprotection.use")) {
                sender.sendMessage("§cУ вас нет прав для выполнения этой команды!")
                return true
            }

            when (args[0].lowercase()) {
                "help" -> {
                    sender.sendMessage("§aДоступные команды:")
                    sender.sendMessage("§a/spawnprotection help §7- Показать список команд.")
                    sender.sendMessage("§a/spawnprotection stats [игрок] §7- Показать статистику.")
                    sender.sendMessage("§a/spawnprotection block <игрок> §7- Запретить игроку ломать блоки.")
                    sender.sendMessage("§a/spawnprotection unblock <игрок> §7- Разрешить игроку ломать блоки.")
                    sender.sendMessage("§a/spawnprotection radius <число> §7- Установить радиус защиты спавна.")
                    return true
                }

                "stats" -> {
                    if (args.size == 1) {
                        if (sender is Player) {
                            showPlayerStats(sender, sender)
                        } else {
                            sender.sendMessage("§cЭту команду можно использовать только в игре!")
                        }
                    } else if (args.size == 2) {
                        val target = Bukkit.getPlayerExact(args[1])
                        if (target == null) {
                            sender.sendMessage("§cИгрок ${args[1]} не найден.")
                        } else {
                            showPlayerStats(sender, target)
                        }
                    } else {
                        sender.sendMessage("§cИспользуйте: /spawnprotection stats [игрок]")
                    }
                    return true
                }

                "block" -> {
                    if (args.size != 2) {
                        sender.sendMessage("§cИспользуйте: /spawnprotection block <игрок>")
                        return true
                    }
                    val targetName = args[1]
                    blockedPlayersManager.addBlockedPlayer(targetName)
                    sender.sendMessage("§aИгроку $targetName теперь запрещено ломать блоки.")
                    return true
                }

                "unblock" -> {
                    if (args.size != 2) {
                        sender.sendMessage("§cИспользуйте: /spawnprotection unblock <игрок>")
                        return true
                    }
                    val targetName = args[1]
                    blockedPlayersManager.removeBlockedPlayer(targetName)
                    sender.sendMessage("§aИгроку $targetName теперь разрешено ломать блоки.")
                    return true
                }

                "radius" -> {
                    if (args.size != 2) {
                        sender.sendMessage("§cИспользуйте: /spawnprotection radius <число>")
                        return true
                    }
                    val radius = args[1].toIntOrNull()
                    if (radius == null || radius <= 0) {
                        sender.sendMessage("§cРадиус должен быть положительным числом!")
                        return true
                    }
                    spawnProtectionRadius = radius

                    // Сохраняем новый радиус в config.yml
                    config.set("spawn_protection_radius", spawnProtectionRadius)
                    saveConfig()

                    sender.sendMessage("§aРадиус защиты спавна установлен на $radius блоков.")
                    return true
                }

                else -> {
                    sender.sendMessage("§cНеизвестная команда. Используйте /spawnprotection help.")
                    return true
                }
            }
        }

        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
        ): MutableList<String> {
            val suggestions = mutableListOf<String>()

            if (args.size == 1) {
                suggestions.addAll(listOf("help", "stats", "block", "unblock", "radius"))
            } else if (args.size == 2) {
                when (args[0].lowercase()) {
                    "stats", "block", "unblock" -> {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            suggestions.add(player.name)
                        }
                    }

                    "radius" -> {
                        suggestions.addAll(listOf("10", "50", "100"))
                    }
                }
            }

            return suggestions.filter { it.startsWith(args.last(), ignoreCase = true) }.toMutableList()
        }

        private fun showPlayerStats(sender: CommandSender, target: Player) {
            val secondsPlayedString = PlaceholderAPI.setPlaceholders(target, "%statistic_seconds_played%")
            val secondsPlayed = try {
                secondsPlayedString.toInt()
            } catch (e: NumberFormatException) {
                sender.sendMessage("§cОшибка при обработке времени: $secondsPlayedString")
                return
            }
            val requiredSeconds = 5 * 60 * 60
            val remainingSeconds = (requiredSeconds - secondsPlayed).coerceAtLeast(0)
            val remainingMinutes = (remainingSeconds / 60)
            val remainingHours = remainingMinutes / 60
            val displayMinutes = remainingMinutes % 60

            sender.sendMessage("§aИгроку ${target.name}: §7${remainingHours}ч ${displayMinutes}м до возможности ломать блоки.")
        }
    }


}

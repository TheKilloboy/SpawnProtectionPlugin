package me.justkeel.spawnProtectionPlugin

import org.bukkit.plugin.java.JavaPlugin
import java.io.File

import org.bukkit.configuration.file.YamlConfiguration

class BlockedPlayersManager(private val plugin: JavaPlugin) {
    private val blockedPlayersFile: File = File(plugin.dataFolder, "blocked_players.yml")
    private var config: YamlConfiguration = YamlConfiguration.loadConfiguration(blockedPlayersFile)

    init {
        if (!blockedPlayersFile.exists()) {
            plugin.saveResource("blocked_players.yml", false)
        }
        config = YamlConfiguration.loadConfiguration(blockedPlayersFile)
    }

    fun getBlockedPlayers(): Set<String> {
        return config.getStringList("blocked_players").toSet()
    }

    fun addBlockedPlayer(playerName: String) {
        val blockedPlayers = getBlockedPlayers().toMutableList()
        if (!blockedPlayers.contains(playerName)) {
            blockedPlayers.add(playerName)
            config.set("blocked_players", blockedPlayers)
            config.save(blockedPlayersFile)
        }
    }

    fun removeBlockedPlayer(playerName: String) {
        val blockedPlayers = getBlockedPlayers().toMutableList()
        if (blockedPlayers.contains(playerName)) {
            blockedPlayers.remove(playerName)
            config.set("blocked_players", blockedPlayers)
            config.save(blockedPlayersFile)
        }
    }
}

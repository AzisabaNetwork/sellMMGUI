package com.github.sirokuri_.sellmmgui

import com.github.sirokuri_.sellmmgui.command.AdminCommand
import com.github.sirokuri_.sellmmgui.command.SmgCommand
import com.github.sirokuri_.sellmmgui.config.Settings
import com.github.sirokuri_.sellmmgui.gui.SellMenu
import com.github.sirokuri_.sellmmgui.listener.SellMenuListener
import com.github.sirokuri_.sellmmgui.listener.TomatoDropListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

class SellMMGUI : JavaPlugin() {

    lateinit var economy: Economy
        private set

    lateinit var settings: Settings
        private set

    override fun onEnable() {
        val provider = server.servicesManager.getRegistration(Economy::class.java)
        if (provider == null) {
            logger.severe("Could not find an economy plugin. Please install one to use SellMMGUI.")
            server.pluginManager.disablePlugin(this)
            return
        }
        economy = provider.provider

        saveDefaultConfig()
        loadSettings()

        server.pluginManager.registerEvents(SellMenuListener(this), this)
        server.pluginManager.registerEvents(TomatoDropListener(this), this)

        registerCommand("smg", SmgCommand(this))
        registerCommand("sellmmgui", AdminCommand(this))
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { player ->
            val menu = player.openInventory.topInventory.holder as? SellMenu ?: return@forEach
            menu.finish()
            player.closeInventory()
        }
    }

    fun loadSettings() {
        reloadConfig()
        settings = Settings.load(config, logger)
    }

    private fun <T> registerCommand(name: String, handler: T) where T : CommandExecutor, T : TabCompleter {
        val command = requireNotNull(getCommand(name)) { "plugin.yml にコマンド $name がありません" }
        command.setExecutor(handler)
        command.tabCompleter = handler
    }
}

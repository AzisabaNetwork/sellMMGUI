package com.github.sirokuri_.sellmmgui.command

import com.github.sirokuri_.sellmmgui.SellMMGUI
import com.github.sirokuri_.sellmmgui.util.MythicItems
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class AdminCommand(private val plugin: SellMMGUI) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val settings = plugin.settings
        when (args.firstOrNull()?.lowercase()) {
            "reload" -> {
                plugin.loadSettings()
                sender.sendMessage(plugin.settings.message("reload", "count" to plugin.settings.items.size))
            }
            "check" -> {
                if (sender !is Player) {
                    sender.sendMessage(settings.message("player-only"))
                    return true
                }
                val type = MythicItems.typeOf(sender.inventory.itemInMainHand)
                val entry = settings.find(type)
                sender.sendMessage(when {
                    type == null -> settings.message("check-not-mythic")
                    entry == null -> settings.message("check-not-sellable", "type" to type)
                    else -> settings.message("check", "type" to type, "price" to plugin.economy.format(entry.price))
                })
            }
            else -> sender.sendMessage(settings.message("admin-usage", "label" to label))
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> =
        if (args.size == 1) SUBCOMMANDS.filter { it.startsWith(args[0], ignoreCase = true) } else emptyList()

    private companion object {
        val SUBCOMMANDS = listOf("reload", "check")
    }
}

package com.github.sirokuri_.sellmmgui.command

import com.github.sirokuri_.sellmmgui.SellMMGUI
import com.github.sirokuri_.sellmmgui.gui.SellMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SmgCommand(private val plugin: SellMMGUI) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.firstOrNull().equals("list", ignoreCase = true)) {
            SellMenu.sendSellList(plugin, sender)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.settings.message("player-only"))
            return true
        }
        SellMenu(plugin, sender).open()
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> =
        if (args.size == 1) listOf("list").filter { it.startsWith(args[0], ignoreCase = true) } else emptyList()
}

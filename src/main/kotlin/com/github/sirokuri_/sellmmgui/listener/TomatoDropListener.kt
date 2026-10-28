package com.github.sirokuri_.sellmmgui.listener

import com.github.sirokuri_.sellmmgui.SellMMGUI
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.random.Random

class TomatoDropListener(private val plugin: SellMMGUI) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val drop = plugin.settings.tomatoDrop
        if (!drop.enabled || drop.command.isEmpty()) return

        val player = event.player
        if (player.world.name != drop.world || event.block.type != drop.block) return
        if (Random.nextDouble() >= drop.chance) return
        if (!drop.matchesTool(player.inventory.itemInMainHand)) return
        if (player.inventory.firstEmpty() == -1) return

        player.playSound(player, Sound.BLOCK_BELL_USE, 1f, 1f)
        plugin.server.dispatchCommand(plugin.server.consoleSender, drop.command.replace("{player}", player.name))
    }
}

package com.github.sirokuri_.sellmmgui.listener

import com.github.sirokuri_.sellmmgui.SellMMGUI
import com.github.sirokuri_.sellmmgui.gui.SellMenu
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

class SellMenuListener(private val plugin: SellMMGUI) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        val menu = event.view.topInventory.holder as? SellMenu ?: return
        if (event.whoClicked !is Player) return

        if (menu.isControlSlot(event.rawSlot)) {
            event.isCancelled = true
            val slot = event.rawSlot
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (!menu.finished) menu.onButton(slot)
            })
            return
        }
        if (event.action == InventoryAction.COLLECT_TO_CURSOR) {
            event.isCancelled = true
            return
        }
        menu.scheduleRefresh()
    }

    @EventHandler(ignoreCancelled = true)
    fun onDrag(event: InventoryDragEvent) {
        val menu = event.view.topInventory.holder as? SellMenu ?: return
        if (event.rawSlots.any(menu::isControlSlot)) {
            event.isCancelled = true
            return
        }
        menu.scheduleRefresh()
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        (event.inventory.holder as? SellMenu)?.finish()
    }
}

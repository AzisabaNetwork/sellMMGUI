package com.github.sirokuri_.sellmmgui.gui

import com.github.sirokuri_.sellmmgui.SellMMGUI
import com.github.sirokuri_.sellmmgui.util.Text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SellMenu(private val plugin: SellMMGUI, private val player: Player) : InventoryHolder {

    private val settings get() = plugin.settings
    private val economy get() = plugin.economy
    private val buttonKey = NamespacedKey(plugin, "button")

    private val inventory: Inventory = Bukkit.createInventory(this, SIZE, Text.color(settings.guiTitle))

    private var refreshScheduled = false

    var finished = false
        private set

    init {
        val filler = button(Material.BLACK_STAINED_GLASS_PANE, " ")
        for (slot in STORAGE_SIZE until SIZE) inventory.setItem(slot, filler)
        inventory.setItem(SLOT_LIST, listButton())
        inventory.setItem(SLOT_DEPOSIT, button(
            Material.HOPPER, "&bインベントリから一括投入",
            "&7手持ちの売却可能アイテムを",
            "&7まとめてこの画面に移します",
        ))
        inventory.setItem(SLOT_INFO, button(
            Material.YELLOW_STAINED_GLASS_PANE, "&e使い方",
            "&f上の欄に売りたいアイテムを入れて",
            "&a売却する &fを押してください",
            "",
            if (settings.sellOnClose) "&7画面を閉じたときも自動で売却されます" else "&7画面を閉じると中身は返却されます",
            "&7売却できないアイテムは返却されます",
        ))
        inventory.setItem(SLOT_RETURN, button(
            Material.CHEST, "&6中身をすべて返却",
            "&7投入したアイテムを売却せずに",
            "&7インベントリへ戻します",
        ))
        inventory.setItem(SLOT_CLOSE, button(
            Material.BARRIER, "&c閉じる",
            if (settings.sellOnClose) "&7売却可能なアイテムは売却されます" else "&7中身は返却されます",
        ))
        refresh()
    }

    override fun getInventory(): Inventory = inventory

    fun open() {
        player.openInventory(inventory)
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1f, 1f)
    }

    fun isControlSlot(rawSlot: Int): Boolean = rawSlot in STORAGE_SIZE until SIZE

    fun onButton(rawSlot: Int) {
        when (rawSlot) {
            SLOT_LIST -> {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                sendSellList(plugin, player)
            }
            SLOT_DEPOSIT -> depositFromPlayer()
            SLOT_SELL -> sell(quiet = false)
            SLOT_RETURN -> {
                val count = returnItems { true }
                player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
                if (count > 0) player.sendMessage(settings.message("returned", "count" to count))
            }
            SLOT_CLOSE -> player.closeInventory()
            else -> return
        }
        refresh()
    }

    fun scheduleRefresh() {
        if (refreshScheduled || finished) return
        refreshScheduled = true
        Bukkit.getScheduler().runTask(plugin, Runnable {
            refreshScheduled = false
            if (!finished) refresh()
        })
    }

    fun finish() {
        if (finished) return
        finished = true
        if (settings.sellOnClose) sell(quiet = true)
        val count = returnItems { true }
        if (count > 0) player.sendMessage(settings.message("returned", "count" to count))
        player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1f, 1f)
    }

    private fun refresh() {
        var total = 0.0
        var sellable = 0
        var unsellable = 0
        storageItems().forEach { (_, item) ->
            val entry = settings.find(item)
            if (entry == null) {
                unsellable += item.amount
            } else {
                sellable += item.amount
                total += entry.price * item.amount
            }
        }
        val lore = mutableListOf(
            "&7売却予定額: &e${economy.format(total)}",
            "&7売却可能: &a${sellable}個",
        )
        if (unsellable > 0) lore += "&7売却対象外: &c${unsellable}個 &8(返却されます)"
        lore += ""
        lore += "&eクリックで売却"
        inventory.setItem(SLOT_SELL, button(Material.EMERALD, "&a&l売却する", *lore.toTypedArray()))
    }

    private fun sell(quiet: Boolean) {
        val sold = mutableListOf<ItemStack>()
        var total = 0.0
        storageItems().forEach { (slot, item) ->
            val entry = settings.find(item) ?: return@forEach
            total += entry.price * item.amount
            sold += item.clone()
            inventory.setItem(slot, null)
        }
        if (sold.isEmpty()) {
            if (!quiet) {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                player.sendMessage(settings.message("nothing-to-sell"))
            }
            return
        }

        val response = economy.depositPlayer(player, total)
        if (!response.transactionSuccess()) {
            give(sold)
            player.sendMessage(settings.message("sell-failed", "error" to (response.errorMessage ?: "unknown")))
            return
        }

        val count = sold.sumOf { it.amount }
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        player.sendMessage(settings.message(
            "sold",
            "count" to count,
            "amount" to economy.format(response.amount),
            "balance" to economy.format(response.balance),
        ))
        plugin.logger.info("${player.name} が ${count} 個のアイテムを ${economy.format(response.amount)} で売却しました (所持金: ${economy.format(response.balance)})")
    }

    private fun depositFromPlayer() {
        val playerInventory = player.inventory
        var moved = 0
        for (slot in 0 until PLAYER_STORAGE_SIZE) {
            val item = playerInventory.getItem(slot) ?: continue
            if (settings.find(item) == null) continue
            val amount = item.amount
            val leftover = inventory.addItem(item.clone()).values.firstOrNull()
            playerInventory.setItem(slot, leftover)
            moved += amount - (leftover?.amount ?: 0)
            if (leftover != null) break
        }
        if (moved == 0) {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            player.sendMessage(settings.message("deposit-none"))
        } else {
            player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
            player.sendMessage(settings.message("deposited", "count" to moved))
        }
    }

    private fun returnItems(filter: (ItemStack) -> Boolean): Int {
        val items = mutableListOf<ItemStack>()
        storageItems().forEach { (slot, item) ->
            if (!filter(item)) return@forEach
            items += item.clone()
            inventory.setItem(slot, null)
        }
        give(items)
        return items.sumOf { it.amount }
    }

    private fun give(items: List<ItemStack>) {
        if (items.isEmpty()) return
        val leftovers = player.inventory.addItem(*items.toTypedArray()).values
        if (leftovers.isEmpty()) return
        leftovers.forEach { player.world.dropItemNaturally(player.location, it) }
        player.sendMessage(settings.message("dropped"))
    }

    private fun storageItems(): List<Pair<Int, ItemStack>> =
        (0 until STORAGE_SIZE).mapNotNull { slot ->
            inventory.getItem(slot)?.takeUnless { it.type.isAir }?.let { slot to it }
        }

    private fun listButton(): ItemStack {
        val lore = settings.items.values.map { "&f${it.display}&7: &e${economy.format(it.price)}" }
            .ifEmpty { listOf("&7売却可能なアイテムはありません") }
        return button(Material.BOOK, "&b売却可能アイテム一覧", *lore.toTypedArray(), "", "&eクリックでチャットに表示")
    }

    private fun button(material: Material, name: String, vararg lore: String): ItemStack =
        ItemStack.of(material).apply {
            editMeta { meta ->
                meta.displayName(Text.color(name))
                if (lore.isNotEmpty()) meta.lore(lore.map(Text::color))
                meta.persistentDataContainer.set(buttonKey, PersistentDataType.BYTE, 1)
            }
        }

    companion object {
        const val SIZE = 54
        const val STORAGE_SIZE = 45
        private const val PLAYER_STORAGE_SIZE = 36

        private const val SLOT_LIST = 45
        private const val SLOT_DEPOSIT = 46
        private const val SLOT_INFO = 47
        private const val SLOT_SELL = 49
        private const val SLOT_RETURN = 51
        private const val SLOT_CLOSE = 53

        fun sendSellList(plugin: SellMMGUI, sender: CommandSender) {
            val settings = plugin.settings
            sender.sendMessage(settings.message("list-header", prefix = false))
            settings.items.values.forEach {
                sender.sendMessage(settings.message(
                    "list-entry",
                    "display" to it.display,
                    "id" to it.id,
                    "price" to plugin.economy.format(it.price),
                    prefix = false,
                ))
            }
            sender.sendMessage(settings.message("list-footer", prefix = false))
        }
    }
}

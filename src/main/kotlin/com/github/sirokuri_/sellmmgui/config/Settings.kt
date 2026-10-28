package com.github.sirokuri_.sellmmgui.config

import com.github.sirokuri_.sellmmgui.util.MythicItems
import com.github.sirokuri_.sellmmgui.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import java.util.logging.Logger

data class SellItem(val id: String, val price: Double, val display: String)

data class TomatoDrop(
    val enabled: Boolean,
    val world: String,
    val block: Material,
    val chance: Double,
    val toolMythicType: String,
    val toolName: String,
    val command: String,
) {
    fun matchesTool(item: ItemStack): Boolean {
        if (toolMythicType.isNotEmpty()) {
            return MythicItems.typeOf(item).equals(toolMythicType, ignoreCase = true)
        }
        if (toolName.isEmpty()) return false
        val name = item.itemMeta?.displayName() ?: return false
        return Text.plain(name) == Text.plain(toolName)
    }
}

class Settings private constructor(
    val items: Map<String, SellItem>,
    val sellOnClose: Boolean,
    val guiTitle: String,
    val tomatoDrop: TomatoDrop,
    private val messages: ConfigurationSection?,
) {
    fun find(mythicType: String?): SellItem? = mythicType?.let { items[it.lowercase()] }

    fun find(item: ItemStack?): SellItem? = find(MythicItems.typeOf(item))

    fun message(key: String, vararg placeholders: Pair<String, Any>, prefix: Boolean = true): Component {
        var raw = messages?.getString(key) ?: key
        placeholders.forEach { (name, value) -> raw = raw.replace("{$name}", value.toString()) }
        if (prefix) raw = (messages?.getString("prefix") ?: "") + raw
        return Text.color(raw)
    }

    companion object {
        fun load(config: FileConfiguration, logger: Logger): Settings {
            val items = linkedMapOf<String, SellItem>()
            config.getConfigurationSection("mmitem")?.let { section ->
                for (id in section.getKeys(false)) {
                    val entry = section.getConfigurationSection(id) ?: continue
                    val price = entry.getDouble("sellprice", -1.0)
                    if (price < 0) {
                        logger.warning("mmitem.$id.sellprice が設定されていないためスキップします")
                        continue
                    }
                    items[id.lowercase()] = SellItem(id, price, entry.getString("display") ?: id)
                }
            }

            val tomato = config.getConfigurationSection("tomato-drop")
            val blockName = tomato?.getString("block") ?: "OAK_LEAVES"
            val block = Material.matchMaterial(blockName) ?: Material.OAK_LEAVES.also {
                logger.warning("tomato-drop.block の値 $blockName は不正なため OAK_LEAVES を使用します")
            }

            return Settings(
                items = items,
                sellOnClose = config.getBoolean("sell-on-close", true),
                guiTitle = config.getString("gui-title") ?: "&cSELLMMITEM SHOP",
                tomatoDrop = TomatoDrop(
                    enabled = tomato?.getBoolean("enabled", false) ?: false,
                    world = tomato?.getString("world") ?: "resource",
                    block = block,
                    chance = tomato?.getDouble("chance", 0.1) ?: 0.1,
                    toolMythicType = tomato?.getString("tool-mythic-type") ?: "",
                    toolName = tomato?.getString("tool-name") ?: "",
                    command = tomato?.getString("command") ?: "",
                ),
                messages = config.getConfigurationSection("messages"),
            )
        }
    }
}

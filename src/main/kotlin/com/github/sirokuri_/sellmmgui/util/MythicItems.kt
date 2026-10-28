package com.github.sirokuri_.sellmmgui.util

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object MythicItems {
    private val TYPE_KEY = NamespacedKey("mythicmobs", "type")

    fun typeOf(item: ItemStack?): String? {
        if (item == null || item.type.isAir) return null
        return item.persistentDataContainer.get(TYPE_KEY, PersistentDataType.STRING)
    }
}

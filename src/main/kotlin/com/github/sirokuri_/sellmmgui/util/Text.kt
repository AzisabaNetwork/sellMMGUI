package com.github.sirokuri_.sellmmgui.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object Text {
    private val legacy = LegacyComponentSerializer.legacyAmpersand()

    fun color(text: String): Component =
        legacy.deserialize(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

    fun plain(component: Component): String = PlainTextComponentSerializer.plainText().serialize(component)

    fun plain(text: String): String = plain(legacy.deserialize(text))
}

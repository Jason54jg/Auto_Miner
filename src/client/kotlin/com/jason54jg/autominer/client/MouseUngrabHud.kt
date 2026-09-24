package com.jason54jg.autominer.client

import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

/**
 * Rappel persistant à l'écran tant que la souris est libérée : contrairement au minage (qui a déjà
 * son propre HUD), rien n'indiquait que ce mode restait actif une fois le message chat passé.
 */
object MouseUngrabHud {
    private val TEXT_COLOR = 0xFFFFFF55.toInt()

    /** [ungrabKey] plutôt qu'une touche codée en dur : le joueur a pu la réassigner dans les options. */
    fun render(graphics: GuiGraphicsExtractor, active: Boolean, ungrabKey: KeyMapping) {
        if (!active) return
        val client = Minecraft.getInstance()
        val font = client.font
        val text = Component.translatable("hud.autominer.mouse_released", ungrabKey.translatedKeyMessage).string
        val x = (client.window.guiScaledWidth - font.width(text)) / 2
        val y = client.window.guiScaledHeight - 68
        graphics.text(font, text, x, y, TEXT_COLOR, true)
    }
}

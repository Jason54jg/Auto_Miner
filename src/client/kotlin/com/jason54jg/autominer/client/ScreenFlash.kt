package com.jason54jg.autominer.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

/** Fait clignoter l'écran (fond rouge semi-transparent) pendant 3 secondes après une alerte. */
object ScreenFlash {
    private const val DURATION_MS = 3000L
    private const val BLINK_PERIOD_MS = 250L

    private var endAtMs = 0L

    fun start() {
        endAtMs = System.currentTimeMillis() + DURATION_MS
    }

    fun render(graphics: GuiGraphicsExtractor) {
        val remaining = endAtMs - System.currentTimeMillis()
        if (remaining <= 0) return
        if ((remaining / BLINK_PERIOD_MS) % 2 != 0L) return

        val window = Minecraft.getInstance().window
        graphics.fill(0, 0, window.guiScaledWidth, window.guiScaledHeight, 0x55FF0000)
    }
}

package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.AutoMiner
import com.jason54jg.miningmod.MiningModConfig
import com.jason54jg.miningmod.MiningStats
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

/** Petit panneau affiché pendant le minage : temps écoulé, blocs cassés, détail des minerais. */
object MiningHud {
    private const val PADDING = 6
    private const val LINE_HEIGHT = 10
    private val PANEL_COLOR = 0x90000000.toInt()
    private val TEXT_COLOR = 0xFFFFFFFF.toInt()
    private val TITLE_COLOR = 0xFF55FF55.toInt()

    private var offsetX = MiningModConfig.load().hudX
    private var offsetY = MiningModConfig.load().hudY

    /** Position du panneau (coin haut-gauche), modifiable depuis l'écran de config. */
    fun applyConfig(config: MiningModConfig) {
        offsetX = config.hudX
        offsetY = config.hudY
    }

    fun render(graphics: GuiGraphicsExtractor) {
        if (!AutoMiner.active) return
        val font = Minecraft.getInstance().font

        val elapsedSeconds = MiningStats.elapsedMs() / 1000
        val time = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)

        val lines = buildList {
            add("Mining Mod" to TITLE_COLOR)
            add("Temps : $time" to TEXT_COLOR)
            add("Blocs cassés : ${MiningStats.blocksBroken}" to TEXT_COLOR)
            MiningStats.ores.forEach { (name, count) -> add("$name : $count" to TEXT_COLOR) }
        }

        val panelWidth = lines.maxOf { (text, _) -> font.width(text) } + PADDING * 2
        val panelHeight = lines.size * LINE_HEIGHT + PADDING * 2

        graphics.fill(offsetX, offsetY, offsetX + panelWidth, offsetY + panelHeight, PANEL_COLOR)
        lines.forEachIndexed { index, (text, color) ->
            graphics.text(font, text, offsetX + PADDING, offsetY + PADDING + index * LINE_HEIGHT, color, true)
        }
    }
}

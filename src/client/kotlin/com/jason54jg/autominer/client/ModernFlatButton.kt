package com.jason54jg.autominer.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

enum class ModernButtonStyle {
    TAB,
    SECONDARY,
    PRIMARY,
    GHOST,
    SLOT,
}

class ModernFlatButton private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Component,
    private val style: ModernButtonStyle,
    private val selected: () -> Boolean,
    onPress: OnPress,
) : Button(x, y, width, height, message, onPress, DEFAULT_NARRATION) {

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val font = Minecraft.getInstance().font
        val hovered = isHovered
        val enabled = active
        val sel = selected()

        val bg = when (style) {
            ModernButtonStyle.PRIMARY -> when {
                !enabled -> 0xFF3A4558.toInt()
                hovered -> 0xFF4A9EFF.toInt()
                else -> 0xFF3D8AE6.toInt()
            }
            ModernButtonStyle.SECONDARY -> when {
                !enabled -> 0xFF252830.toInt()
                hovered -> 0xFF3A404C.toInt()
                else -> 0xFF2E333D.toInt()
            }
            ModernButtonStyle.TAB -> when {
                sel -> 0xFF3D8AE6.toInt()
                !enabled -> 0xFF252830.toInt()
                hovered -> 0xFF353B48.toInt()
                else -> 0xFF2A2F38.toInt()
            }
            ModernButtonStyle.GHOST -> when {
                !enabled -> 0x00000000
                hovered -> 0x33FFFFFF
                else -> 0x00000000
            }
            ModernButtonStyle.SLOT -> when {
                hovered -> 0xFF3A404C.toInt()
                else -> 0xFF1E2228.toInt()
            }
        }

        val left = x
        val top = y
        val right = x + width
        val bottom = y + height

        if (bg ushr 24 != 0) {
            graphics.fill(left, top, right, bottom, bg)
        }

        val border = when (style) {
            ModernButtonStyle.PRIMARY -> if (hovered && enabled) 0xFF6BB0FF.toInt() else 0xFF2E6BB8.toInt()
            ModernButtonStyle.SLOT -> if (hovered) 0xFF6BB0FF.toInt() else 0xFF555C68.toInt()
            ModernButtonStyle.TAB -> if (sel) 0xFF6BB0FF.toInt() else 0xFF444B56.toInt()
            else -> if (hovered && enabled) 0xFF666E7A.toInt() else 0xFF444B56.toInt()
        }
        graphics.fill(left, top, right, top + 1, border)
        graphics.fill(left, bottom - 1, right, bottom, border)
        graphics.fill(left, top, left + 1, bottom, border)
        graphics.fill(right - 1, top, right, bottom, border)

        if (message.string.isNotEmpty()) {
            val textColor = when {
                !enabled -> 0xFF888888.toInt()
                style == ModernButtonStyle.PRIMARY || (style == ModernButtonStyle.TAB && sel) -> 0xFFFFFFFF.toInt()
                else -> 0xFFE8ECF0.toInt()
            }
            val padH = 8
            val wrapSecondary = style == ModernButtonStyle.SECONDARY && width >= 100
            val maxTextW = (width - padH * 2).coerceAtLeast(20)
            val lines = if (wrapSecondary) {
                font.split(message, maxTextW)
            } else {
                font.split(message, width.coerceAtLeast(1))
            }
            val lineHeight = 9
            val blockHeight = lines.size * lineHeight
            var lineY = top + (height - blockHeight).coerceAtLeast(padH) / 2
            for (line in lines) {
                val lineW = font.width(line)
                val textX = if (wrapSecondary) left + padH else left + (width - lineW) / 2
                graphics.text(font, line, textX, lineY, textColor, false)
                lineY += lineHeight
            }
        }
    }

    companion object {
        fun secondaryHeight(font: net.minecraft.client.gui.Font, message: Component, width: Int): Int {
            val maxTextW = (width - 16).coerceAtLeast(20)
            val lines = font.split(message, maxTextW).size.coerceAtLeast(1)
            return lines * 9 + 10
        }

        fun builder(
            message: Component,
            style: ModernButtonStyle,
            selected: () -> Boolean = { false },
            onPress: OnPress,
        ): Builder = Builder(message, style, selected, onPress)

        class Builder(
            private val message: Component,
            private val style: ModernButtonStyle,
            private val selected: () -> Boolean,
            private val onPress: OnPress,
        ) {
            private var x = 0
            private var y = 0
            private var width = 120
            private var height = 20

            fun bounds(x: Int, y: Int, width: Int, height: Int) = apply {
                this.x = x
                this.y = y
                this.width = width
                this.height = height
            }

            fun build(): ModernFlatButton = ModernFlatButton(x, y, width, height, message, style, selected, onPress)
        }
    }
}

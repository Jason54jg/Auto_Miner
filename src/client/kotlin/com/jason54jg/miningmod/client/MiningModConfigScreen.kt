package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.AutoMiner
import com.jason54jg.miningmod.CustomOreSelection
import com.jason54jg.miningmod.MiningModConfig
import com.jason54jg.miningmod.MiningPattern
import com.jason54jg.miningmod.ToolSelection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

class MiningModConfigScreen(private val parent: Screen?) : Screen(Component.literal("Mining Mod")) {
    private var config = MiningModConfig.load()

    private val patternPresets = MiningPattern.values().toList()

    private var reachRowY = 0
    private var blocksRowY = 0
    private var targetYRowY = 0
    private var tunnelWidthRowY = 0
    private var toolSlotX = 0
    private var toolSlotY = 0
    private var oreSlotX = 0
    private var oreSlotY = 0

    override fun init() {
        val centerX = width / 2
        var y = height / 2 - 134

        reachRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.reach.toString()) { text ->
            text.toDoubleOrNull()?.let { config = config.copy(reach = it) }
        })
        y += 24

        blocksRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.blocksPerTick.toString()) { text ->
            text.toIntOrNull()?.let { config = config.copy(blocksPerTick = it.coerceAtLeast(1)) }
        })
        y += 24

        addRenderableWidget(
            Button.builder(patternLabel()) {
                config = config.copy(pattern = nextPreset(patternPresets, config.pattern))
                it.message = patternLabel()
            }.bounds(centerX - 100, y, 200, 20).build()
        )
        y += 24

        addRenderableWidget(
            Button.builder(lockYLabel()) {
                config = config.copy(lockYLevel = !config.lockYLevel)
                it.message = lockYLabel()
            }.bounds(centerX - 100, y, 200, 20).build()
        )
        y += 24

        targetYRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.targetY.toString()) { text ->
            text.toIntOrNull()?.let { config = config.copy(targetY = it) }
        })
        y += 24

        tunnelWidthRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.tunnelWidth.toString()) { text ->
            text.toIntOrNull()?.let { config = config.copy(tunnelWidth = it.coerceIn(0, 2)) }
        })
        y += 24

        // Slot outil : clic = capture l'item actuellement en main du joueur comme outil de minage.
        toolSlotX = centerX - 100
        toolSlotY = y
        addRenderableWidget(
            Button.builder(Component.empty()) {
                val held = minecraft?.player?.mainHandItem
                ToolSelection.item = held?.takeUnless { it.isEmpty }?.item
            }.bounds(toolSlotX, toolSlotY, 20, 20).build()
        )
        y += 24

        // Slot minerai custom : clic = capture le bloc actuellement en main comme "à casser impérativement".
        oreSlotX = centerX - 100
        oreSlotY = y
        addRenderableWidget(
            Button.builder(Component.empty()) {
                val held = minecraft?.player?.mainHandItem ?: return@builder
                val block = Block.byItem(held.item)
                CustomOreSelection.block = block.takeUnless { it == Blocks.AIR }
            }.bounds(oreSlotX, oreSlotY, 20, 20).build()
        )
        y += 32

        addRenderableWidget(
            Button.builder(Component.literal("Terminé")) { onDone() }
                .bounds(centerX - 100, y, 200, 20)
                .build()
        )
    }

    /** Champ de texte libre pour une valeur numérique (au lieu d'un bouton qui ne fait que cycler des présélections). */
    private fun numberField(x: Int, y: Int, initial: String, onChange: (String) -> Unit): EditBox {
        val box = EditBox(font, x, y, 90, 20, Component.empty())
        box.value = initial
        box.setResponder(onChange)
        return box
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)

        val x = width / 2 - 100
        val white = 0xFFFFFFFF.toInt()
        graphics.text(font, "Portée :", x, reachRowY + 6, white, true)
        graphics.text(font, "Blocs/tick :", x, blocksRowY + 6, white, true)
        graphics.text(font, "Couche Y :", x, targetYRowY + 6, white, true)
        graphics.text(font, "Largeur tunnel :", x, tunnelWidthRowY + 6, white, true)

        val tool = ToolSelection.item
        if (tool != null) {
            graphics.item(ItemStack(tool), toolSlotX + 2, toolSlotY + 2)
        }
        graphics.text(font, "Outil : ${toolName(tool)}", toolSlotX + 26, toolSlotY + 6, white, true)

        val ore = CustomOreSelection.block
        if (ore != null) {
            graphics.item(ItemStack(ore), oreSlotX + 2, oreSlotY + 2)
        }
        graphics.text(font, "Bloc obligatoire : ${oreName(ore)}", oreSlotX + 26, oreSlotY + 6, white, true)
    }

    override fun onClose() {
        onDone()
    }

    private fun onDone() {
        MiningModConfig.save(config)
        AutoMiner.applyConfig(config)
        minecraft?.gui?.setScreen(parent)
    }

    private fun lockYLabel() = Component.literal("Verrouiller le niveau Y : ${if (config.lockYLevel) "Oui" else "Non"}")
    private fun patternLabel() = Component.literal("Style : ${patternName(config.pattern)}")

    private fun patternName(pattern: MiningPattern) = when (pattern) {
        MiningPattern.STRAIGHT -> "Tout droit"
        MiningPattern.ZIGZAG -> "Zigzag (gauche/droite)"
        MiningPattern.CIRCLE -> "Cercle (spirale)"
    }

    private fun toolName(tool: Item?): String {
        if (tool == null) return "main (aucun choisi)"
        return BuiltInRegistries.ITEM.getKey(tool).path.replace('_', ' ')
    }

    private fun oreName(block: Block?): String {
        if (block == null) return "aucun"
        return BuiltInRegistries.BLOCK.getKey(block).path.replace('_', ' ')
    }

    private fun <T> nextPreset(presets: List<T>, current: T): T {
        val index = presets.indexOf(current)
        return presets[(index + 1).mod(presets.size)]
    }
}

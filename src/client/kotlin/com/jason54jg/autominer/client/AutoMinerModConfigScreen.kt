package com.jason54jg.autominer.client

import com.jason54jg.autominer.AutoMiner
import com.jason54jg.autominer.CustomOreSelection
import com.jason54jg.autominer.AutoMinerModConfig
import com.jason54jg.autominer.ToolSelection
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult

class AutoMinerModConfigScreen(private val parent: Screen?) : Screen(Component.translatable("screen.autominer.title")) {
    private var config = AutoMinerModConfig.load()

    private var reachRowY = 0
    private var blocksRowY = 0
    private var targetYRowY = 0
    private var tunnelWidthRowY = 0
    private var hudXRowY = 0
    private var hudYRowY = 0
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

        hudXRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.hudX.toString()) { text ->
            text.toIntOrNull()?.let { config = config.copy(hudX = it) }
        })
        y += 24

        hudYRowY = y
        addRenderableWidget(numberField(centerX + 10, y, config.hudY.toString()) { text ->
            text.toIntOrNull()?.let { config = config.copy(hudY = it) }
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

        // Slot minerai custom : clic = capture le bloc visé (regardé juste avant d'ouvrir cet écran)
        // comme "à casser impérativement". Vise le bloc AVANT d'ouvrir la config avec /autominer.
        oreSlotX = centerX - 100
        oreSlotY = y
        addRenderableWidget(
            Button.builder(Component.empty()) {
                val hit = minecraft?.hitResult as? BlockHitResult ?: return@builder
                val level = minecraft?.level ?: return@builder
                val block = level.getBlockState(hit.blockPos).block
                CustomOreSelection.block = block.takeUnless { it == Blocks.AIR }
            }.bounds(oreSlotX, oreSlotY, 20, 20).build()
        )
        y += 32

        addRenderableWidget(
            Button.builder(Component.translatable("screen.autominer.done")) { onDone() }
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
        graphics.text(font, Component.translatable("screen.autominer.reach").string, x, reachRowY + 6, white, true)
        graphics.text(font, Component.translatable("screen.autominer.blocks_per_tick").string, x, blocksRowY + 6, white, true)
        graphics.text(font, Component.translatable("screen.autominer.y_level").string, x, targetYRowY + 6, white, true)
        graphics.text(font, Component.translatable("screen.autominer.tunnel_width").string, x, tunnelWidthRowY + 6, white, true)
        graphics.text(font, Component.translatable("screen.autominer.hud_x").string, x, hudXRowY + 6, white, true)
        graphics.text(font, Component.translatable("screen.autominer.hud_y").string, x, hudYRowY + 6, white, true)

        val tool = ToolSelection.item
        if (tool != null) {
            graphics.item(ItemStack(tool), toolSlotX + 2, toolSlotY + 2)
        }
        graphics.text(font, toolLabel(tool), toolSlotX + 26, toolSlotY + 6, white, true)

        val ore = CustomOreSelection.block
        if (ore != null) {
            graphics.item(ItemStack(ore), oreSlotX + 2, oreSlotY + 2)
        }
        graphics.text(font, mandatoryBlockLabel(ore), oreSlotX + 26, oreSlotY + 6, white, true)
    }

    override fun onClose() {
        onDone()
    }

    private fun onDone() {
        val finalConfig = config.copy(
            toolItemId = ToolSelection.item?.let(ToolSelection::idOf),
            customBlockId = CustomOreSelection.block?.let(CustomOreSelection::idOf)
        )
        AutoMinerModConfig.save(finalConfig)
        AutoMiner.applyConfig(finalConfig)
        MiningHud.applyConfig(finalConfig)
        minecraft?.gui?.setScreen(parent)
    }

    private fun lockYLabel() = Component.translatable(if (config.lockYLevel) "screen.autominer.lock_y_yes" else "screen.autominer.lock_y_no")

    /** Nom localisé de l'item (celui déjà fourni par Minecraft/le mod qui l'ajoute), pas une clé à nous. */
    private fun toolLabel(tool: Item?): String {
        val name = if (tool == null) Component.translatable("screen.autominer.tool_none") else Component.translatable(tool.descriptionId)
        return Component.translatable("screen.autominer.tool", name).string
    }

    private fun mandatoryBlockLabel(block: Block?): String {
        val name = if (block == null) Component.translatable("screen.autominer.mandatory_block_none") else Component.translatable(block.descriptionId)
        return Component.translatable("screen.autominer.mandatory_block", name).string
    }
}

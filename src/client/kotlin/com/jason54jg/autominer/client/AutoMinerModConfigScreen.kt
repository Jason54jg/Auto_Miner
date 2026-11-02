package com.jason54jg.autominer.client

import com.jason54jg.autominer.AutoMiner
import com.jason54jg.autominer.AutoMinerModConfig
import com.jason54jg.autominer.CustomOreSelection
import com.jason54jg.autominer.ObstacleNavigationMode
import com.jason54jg.autominer.ToolSelection
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult

class AutoMinerModConfigScreen(private val parent: Screen?) : Screen(Component.translatable("screen.autominer.title")) {
    private val logo = Identifier.fromNamespaceAndPath("autominer", "textures/logo.png")
    private enum class Tab {
        GENERAL, TUNNEL, OBSTACLES, DISPLAY
    }

    private var tab = Tab.GENERAL
    private var config = AutoMinerModConfig.load()
    private val openedConfig = config.copy()
    private val openedTool: Item? = ToolSelection.item
    private val openedBlock: Block? = CustomOreSelection.block

    private var panelLeft = 0
    private var panelTop = 0
    private var panelRight = 0
    private var panelBottom = 0
    private var contentLeft = 0
    private var contentTop = 0
    private var contentRight = 0
    private var sidebarRight = 0
    private var slotColumnLeft = 0

    private val sidebarWidth = 82
    private val tabButtonHeight = 22
    private val tabButtonPad = 6
    private val headerSubtitleY = 18
    private val headerGapBelowSubtitle = 14

    override fun init() {
        super.init()
        layoutPanel()
        rebuildTabWidgets()
    }

    private fun layoutPanel() {
        val panelW = 360
        val panelH = 268
        panelLeft = width / 2 - panelW / 2
        panelTop = height / 2 - panelH / 2
        panelRight = panelLeft + panelW
        panelBottom = panelTop + panelH
        sidebarRight = panelLeft + sidebarWidth
        contentLeft = sidebarRight + 8
        contentRight = panelRight - 88
        slotColumnLeft = panelRight - 80
        contentTop = computeContentTop()
    }

    private fun headerTextMaxWidth(): Int =
        (slotColumnLeft - contentLeft - 4).coerceAtLeast(40)

    private fun subtitleLineCount(): Int = hintLineCount(
        Component.translatable("screen.autominer.subtitle").string,
        headerTextMaxWidth(),
    )

    /** Bottom edge (Y) of the wrapped subtitle in the header band. */
    private fun headerSubtitleBottomY(): Int =
        panelTop + headerSubtitleY + subtitleLineCount() * 9

    private fun computeContentTop(): Int =
        headerSubtitleBottomY() + headerGapBelowSubtitle

    private fun rebuildTabWidgets() {
        clearWidgets()
        rowLabels.clear()
        layoutPanel()
        addTabButtons()
        when (tab) {
            Tab.GENERAL -> buildGeneralTab()
            Tab.TUNNEL -> buildTunnelTab()
            Tab.OBSTACLES -> buildObstaclesTab()
            Tab.DISPLAY -> buildDisplayTab()
        }
        buildSlotColumn()
        buildFooter()
    }

    private fun tabButtonWidth(): Int = sidebarWidth - tabButtonPad * 2

    private fun addTabButtons() {
        val tabW = tabButtonWidth()
        var y = (headerSubtitleBottomY() + 6).coerceAtLeast(panelTop + 30)
        for (entry in Tab.entries) {
            addRenderableWidget(
                ModernFlatButton.builder(tabLabel(entry), ModernButtonStyle.TAB, selected = { tab == entry }) {
                    tab = entry
                    rebuildTabWidgets()
                }.bounds(panelLeft + tabButtonPad, y, tabW, tabButtonHeight).build()
            )
            y += tabButtonHeight + 4
        }
    }

    private fun buildGeneralTab() {
        var y = contentTop
        addLabelRow("screen.autominer.section.general", y)
        y += 16
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.reach.toString()) { text ->
                text.toDoubleOrNull()?.let { config = config.copy(reach = it.coerceAtLeast(1.0)) }
            }
        )
        registerLabel("screen.autominer.reach", contentLeft, y)
        y += 24
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.blocksPerTick.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(blocksPerTick = it.coerceAtLeast(0)) }
            }
        )
        registerLabel("screen.autominer.blocks_per_tick", contentLeft, y)
    }

    private fun contentButtonWidth(): Int = contentRight - contentLeft

    private fun secondaryButtonHeight(message: Component): Int =
        ModernFlatButton.secondaryHeight(font, message, contentButtonWidth())

    private fun buildTunnelTab() {
        var y = contentTop
        addLabelRow("screen.autominer.section.tunnel", y)
        y += 16
        val lockLabel = lockYLabel()
        val lockBtnH = secondaryButtonHeight(lockLabel)
        addRenderableWidget(
            ModernFlatButton.builder(lockLabel, ModernButtonStyle.SECONDARY) {
                config = config.copy(lockYLevel = !config.lockYLevel)
                it.message = lockYLabel()
            }.bounds(contentLeft, y, contentButtonWidth(), lockBtnH).build()
        )
        y += lockBtnH + 4
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.targetY.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(targetY = it) }
            }
        )
        registerLabel("screen.autominer.y_level", contentLeft, y)
        y += 24
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.tunnelWidth.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(tunnelWidth = it.coerceIn(0, 2)) }
            }
        )
        registerLabel("screen.autominer.tunnel_width", contentLeft, y)
    }

    private fun buildObstaclesTab() {
        var y = contentTop
        addLabelRow("screen.autominer.section.obstacles", y)
        y += 16
        val modeLabel = obstacleModeLabel()
        val modeBtnH = secondaryButtonHeight(modeLabel)
        addRenderableWidget(
            ModernFlatButton.builder(modeLabel, ModernButtonStyle.SECONDARY) {
                config = config.copy(
                    obstacleNavigationMode = when (config.obstacleNavigationMode) {
                        ObstacleNavigationMode.FORWARD_ONLY -> ObstacleNavigationMode.SMART
                        ObstacleNavigationMode.SMART -> ObstacleNavigationMode.FORWARD_ONLY
                    }
                )
                rebuildTabWidgets()
            }.bounds(contentLeft, y, contentButtonWidth(), modeBtnH).build()
        )
        y += modeBtnH + 4
        val smartNav = config.obstacleNavigationMode == ObstacleNavigationMode.SMART
        y = addToggleButton(
            y,
            toggleLabel("screen.autominer.bypass_obstacles", config.bypassObstacles),
            smartNav,
        ) {
            config = config.copy(bypassObstacles = !config.bypassObstacles)
            it.message = toggleLabel("screen.autominer.bypass_obstacles", config.bypassObstacles)
        }
        y = addToggleButton(
            y,
            toggleLabel("screen.autominer.descend_under", config.descendUnderOverhangs),
            smartNav,
        ) {
            config = config.copy(descendUnderOverhangs = !config.descendUnderOverhangs)
            it.message = toggleLabel("screen.autominer.descend_under", config.descendUnderOverhangs)
        }
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.obstacleDetectionReach.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(obstacleDetectionReach = it.coerceIn(1, 6)) }
            }
        )
        registerLabel("screen.autominer.obstacle_reach", contentLeft, y)
        obstacleHintY = y + 24
    }

    private fun buildDisplayTab() {
        var y = contentTop
        addLabelRow("screen.autominer.section.display", y)
        y += 16
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.hudX.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(hudX = it) }
            }
        )
        registerLabel("screen.autominer.hud_x", contentLeft, y)
        y += 24
        addRenderableWidget(
            numberField(contentLeft + 120, y, config.hudY.toString()) { text ->
                text.toIntOrNull()?.let { config = config.copy(hudY = it) }
            }
        )
        registerLabel("screen.autominer.hud_y", contentLeft, y)
    }

    private data class SlotColumnLayout(
        val toolLabelY: Int,
        val toolSlotY: Int,
        val toolHintY: Int,
        val mandatoryLabelY: Int,
        val oreSlotY: Int,
        val oreHintY: Int,
    )

    private fun slotColumnLayout(): SlotColumnLayout {
        val slotLabelW = 72
        val toolLabelY = contentTop
        val toolSlotY = toolLabelY + 14
        val toolHintY = toolSlotY + 22
        val toolHintLines = hintLineCount(toolShortLabel(ToolSelection.item), slotLabelW)
        val mandatoryLabelY = toolHintY + toolHintLines * 9 + 6
        val oreSlotY = mandatoryLabelY + 14
        val oreHintY = oreSlotY + 22
        return SlotColumnLayout(toolLabelY, toolSlotY, toolHintY, mandatoryLabelY, oreSlotY, oreHintY)
    }

    private fun hintLineCount(text: String, maxWidth: Int): Int =
        font.split(Component.literal(text), maxWidth).size.coerceAtLeast(1)

    private fun buildSlotColumn() {
        val layout = slotColumnLayout()
        toolSlotX = slotColumnLeft
        toolSlotY = layout.toolSlotY
        addRenderableWidget(
            ModernFlatButton.builder(Component.empty(), ModernButtonStyle.SLOT) {
                val held = minecraft?.player?.mainHandItem
                ToolSelection.item = held?.takeUnless { it.isEmpty }?.item
                rebuildTabWidgets()
            }.bounds(toolSlotX, toolSlotY, 20, 20).build()
        )
        oreSlotX = slotColumnLeft
        oreSlotY = layout.oreSlotY
        addRenderableWidget(
            ModernFlatButton.builder(Component.empty(), ModernButtonStyle.SLOT) {
                val hit = minecraft?.hitResult as? BlockHitResult ?: return@builder
                val level = minecraft?.level ?: return@builder
                val block = level.getBlockState(hit.blockPos).block
                CustomOreSelection.block = block.takeUnless { it == Blocks.AIR }
            }.bounds(oreSlotX, oreSlotY, 20, 20).build()
        )
    }

    private fun buildFooter() {
        val btnW = 72
        val gap = 8
        val saveX = panelRight - btnW * 2 - gap - 8
        val cancelY = panelBottom - 28
        addRenderableWidget(
            ModernFlatButton.builder(Component.translatable("screen.autominer.cancel"), ModernButtonStyle.SECONDARY) { onCancel() }
                .bounds(saveX, cancelY, btnW, 20).build()
        )
        addRenderableWidget(
            ModernFlatButton.builder(Component.translatable("screen.autominer.save"), ModernButtonStyle.PRIMARY) { onSave() }
                .bounds(saveX + btnW + gap, cancelY, btnW, 20).build()
        )
    }

    private val rowLabels = ArrayList<LabelRow>()
    private var obstacleHintY = 0
    private var toolSlotX = 0
    private var toolSlotY = 0
    private var oreSlotX = 0
    private var oreSlotY = 0

    private data class LabelRow(val key: String, val x: Int, val y: Int)

    private fun registerLabel(key: String, x: Int, y: Int) {
        rowLabels.add(LabelRow(key, x, y))
    }

    private fun addLabelRow(key: String, y: Int) {
        registerLabel(key, contentLeft, y)
    }

    private fun numberField(x: Int, y: Int, initial: String, onChange: (String) -> Unit): EditBox {
        val box = EditBox(font, x, y, 56, 20, Component.empty())
        box.value = initial
        box.setResponder(onChange)
        return box
    }

    private fun addToggleButton(
        y: Int,
        message: Component,
        enabled: Boolean,
        onClick: (Button) -> Unit,
    ): Int {
        val btnH = secondaryButtonHeight(message)
        addRenderableWidget(
            ModernFlatButton.builder(message, ModernButtonStyle.SECONDARY) { onClick(it) }
                .bounds(contentLeft, y, contentButtonWidth(), btnH)
                .build()
                .also { it.active = enabled }
        )
        return y + btnH + 4
    }

    private fun toggleLabel(baseKey: String, on: Boolean): Component {
        val suffix = if (on) "screen.autominer.toggle_on" else "screen.autominer.toggle_off"
        return Component.translatable(baseKey).append(" — ").append(Component.translatable(suffix))
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        renderPanel(graphics)
        super.extractRenderState(graphics, mouseX, mouseY, delta)
        val white = 0xFFFFFFFF.toInt()
        val muted = 0xFFAAAAAA.toInt()
        graphics.blit(logo, panelLeft + 8, panelTop + 4, panelLeft + 32, panelTop + 28, 0f, 1f, 0f, 1f)
        graphics.text(font, title.string, contentLeft, panelTop + 8, white, true)
        wrapHint(
            graphics,
            Component.translatable("screen.autominer.subtitle").string,
            contentLeft,
            panelTop + headerSubtitleY,
            headerTextMaxWidth(),
            muted
        )
        for (row in rowLabels) {
            graphics.text(font, Component.translatable(row.key).string, row.x, row.y + 8, white, true)
        }
        if (tab == Tab.OBSTACLES && obstacleHintY > 0) {
            wrapHint(graphics, Component.translatable("screen.autominer.obstacle_hint").string, contentLeft, obstacleHintY, contentRight - contentLeft, 0xFF88AAFF.toInt())
        }
        renderSlotColumn(graphics, white, muted)
    }

    private fun renderPanel(graphics: GuiGraphicsExtractor) {
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xE0101018.toInt())
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFF555555.toInt())
        graphics.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFF555555.toInt())
        graphics.fill(sidebarRight, panelTop, sidebarRight + 1, panelBottom, 0xFF444444.toInt())
        graphics.fill(slotColumnLeft - 4, panelTop, slotColumnLeft - 3, panelBottom, 0xFF444444.toInt())
    }

    private fun renderSlotColumn(graphics: GuiGraphicsExtractor, white: Int, muted: Int) {
        val slotLabelW = 72
        val layout = slotColumnLayout()
        graphics.text(font, Component.translatable("screen.autominer.tool_slot").string, slotColumnLeft, layout.toolLabelY, muted, false)
        val tool = ToolSelection.item
        if (tool != null) {
            graphics.item(ItemStack(tool), toolSlotX + 2, toolSlotY + 2)
        }
        wrapHint(graphics, toolShortLabel(tool), slotColumnLeft, layout.toolHintY, slotLabelW, white)

        graphics.text(font, Component.translatable("screen.autominer.mandatory_slot").string, slotColumnLeft, layout.mandatoryLabelY, muted, false)
        val ore = CustomOreSelection.block
        if (ore != null) {
            graphics.item(ItemStack(ore), oreSlotX + 2, oreSlotY + 2)
        }
        wrapHint(graphics, blockShortLabel(ore), slotColumnLeft, layout.oreHintY, slotLabelW, white)
    }

    private fun wrapHint(graphics: GuiGraphicsExtractor, text: String, x: Int, y: Int, maxWidth: Int, color: Int) {
        val lines = font.split(Component.literal(text), maxWidth)
        var lineY = y
        for (line in lines) {
            graphics.text(font, line, x, lineY, color, false)
            lineY += 9
        }
    }

    override fun onClose() {
        onCancel()
    }

    private fun onSave() {
        val finalConfig = config.copy(
            configVersion = 2,
            toolItemId = ToolSelection.item?.let(ToolSelection::idOf),
            customBlockId = CustomOreSelection.block?.let(CustomOreSelection::idOf)
        )
        AutoMinerModConfig.save(finalConfig)
        AutoMiner.applyConfig(finalConfig)
        MiningHud.applyConfig(finalConfig)
        ToolSelection.applyFromConfig(finalConfig)
        CustomOreSelection.applyFromConfig(finalConfig)
        closeScreen()
    }

    private fun onCancel() {
        config = openedConfig
        ToolSelection.item = openedTool
        CustomOreSelection.block = openedBlock
        closeScreen()
    }

    private fun closeScreen() {
        //? if <26.2 {
        /*minecraft?.setScreen(parent)*/
        //?} else {
        minecraft?.gui?.setScreen(parent)
        //?}
    }

    private fun tabLabel(entry: Tab): Component = Component.translatable(
        when (entry) {
            Tab.GENERAL -> "screen.autominer.tab.general"
            Tab.TUNNEL -> "screen.autominer.tab.tunnel"
            Tab.OBSTACLES -> "screen.autominer.tab.obstacles"
            Tab.DISPLAY -> "screen.autominer.tab.display"
        }
    )

    private fun lockYLabel() = Component.translatable(if (config.lockYLevel) "screen.autominer.lock_y_yes" else "screen.autominer.lock_y_no")

    private fun obstacleModeLabel(): Component = when (config.obstacleNavigationMode) {
        ObstacleNavigationMode.SMART -> Component.translatable("screen.autominer.nav_mode_smart")
        ObstacleNavigationMode.FORWARD_ONLY -> Component.translatable("screen.autominer.nav_mode_forward")
    }

    private fun toolShortLabel(tool: Item?): String {
        if (tool == null) return Component.translatable("screen.autominer.tool_none").string
        return Component.translatable(tool.descriptionId).string
    }

    private fun blockShortLabel(block: Block?): String {
        if (block == null) return Component.translatable("screen.autominer.mandatory_block_none").string
        return Component.translatable(block.descriptionId).string
    }
}

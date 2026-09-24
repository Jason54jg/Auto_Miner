package com.jason54jg.miningmod

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * Creuse dans la direction regardée par le joueur (ou selon le style choisi) tant que [active] est vrai,
 * en cassant systématiquement tout minerai touchant le tunnel avant d'avancer.
 *
 * [config] permet de s'adapter à un serveur non-vanilla (portée étendue, capacités
 * de casse instantanée multiple, verrouillage du niveau Y pour du strip mining) et de
 * choisir un style de minage ([MiningPattern.STRAIGHT] tout droit, [MiningPattern.ZIGZAG]
 * de gauche à droite, [MiningPattern.CIRCLE] en spirale).
 */
object AutoMiner {
    private const val WALK_SPEED = 0.2
    private const val MAX_LOOKAHEAD = 6
    private const val ZIGZAG_SEGMENT_LENGTH = 6

    private val ORE_BLOCKS = setOf(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS
    )

    var active = false
        private set

    private var config = MiningModConfig.load()
    private var target: BlockPos? = null
    private var lockedY: Int? = null

    // État du style de minage (zigzag / cercle) : direction courante et longueur du segment en cours.
    private var baseFacing: Direction? = null
    private var currentFacing: Direction? = null
    private var segmentSteps = 0
    private var segmentLength = ZIGZAG_SEGMENT_LENGTH
    private var zigzagPhase = 0
    private var spiralLegCount = 0

    fun applyConfig(newConfig: MiningModConfig) {
        config = newConfig
    }

    fun toggle(client: Minecraft) {
        active = !active
        if (active) {
            MiningStats.startSession()
            val player = client.player
            lockedY = player?.blockPosition()?.y
            baseFacing = player?.direction
            currentFacing = baseFacing
            segmentSteps = 0
            segmentLength = if (config.pattern == MiningPattern.CIRCLE) 1 else ZIGZAG_SEGMENT_LENGTH
            zigzagPhase = 0
            spiralLegCount = 0
        } else {
            reset(client)
        }
    }

    fun reset(client: Minecraft) {
        client.gameMode?.stopDestroyBlock()
        target = null
        lockedY = null
        baseFacing = null
        currentFacing = null
    }

    fun tick(client: Minecraft) {
        if (!active) return
        val player = client.player ?: return
        val level = client.level ?: return
        val gameMode = client.gameMode ?: return

        ensureToolEquipped(player)
        repeat(config.blocksPerTick.coerceAtLeast(1)) {
            if (!mineOrAdvance(player, level, gameMode)) return
        }
    }

    /** Équipe l'outil choisi dans la configuration s'il est présent dans la hotbar. */
    private fun ensureToolEquipped(player: LocalPlayer) {
        val tool = ToolSelection.item ?: return
        val inventory = player.inventory
        if (inventory.getItem(inventory.selected).item == tool) return
        for (slot in 0 until 9) {
            if (inventory.getItem(slot).item == tool) {
                inventory.selected = slot
                return
            }
        }
    }

    /** Retourne vrai si un bloc a été cassé instantanément, permettant d'en tenter un autre le même tick. */
    private fun mineOrAdvance(player: LocalPlayer, level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val facing = resolveFacing(player)
        val base = player.blockPosition()
        val origin = if (config.lockYLevel) BlockPos(base.x, lockedY ?: base.y, base.z) else base

        val lookahead = config.reach.toInt().coerceIn(1, MAX_LOOKAHEAD)
        val path = (1..lookahead).flatMap { step ->
            val forward = origin.relative(facing, step)
            listOf(forward, forward.above())
        }
        val immediate = listOf(origin.relative(facing, 1), origin.relative(facing, 1).above())

        val ores = path
            .flatMap { pos -> Direction.values().map(pos::relative) }
            .filter { pos -> pos !in path && isOre(level.getBlockState(pos)) }
            .distinct()

        val current = target?.takeIf { isBreakable(level, it) }
            ?: (ores + path).firstOrNull { isBreakable(level, it) }

        if (current == null) {
            target = null
            advance(player, level, immediate, facing)
            return false
        }

        if (current != target) {
            gameMode.stopDestroyBlock()
            target = current
            val brokenState = level.getBlockState(current)
            if (gameMode.startDestroyBlock(current, facing.opposite)) {
                target = null
                MiningStats.recordBreak(brokenState, isOre(brokenState))
                return true
            }
            return false
        }

        val brokenState = level.getBlockState(current)
        if (gameMode.continueDestroyBlock(current, facing.opposite)) {
            gameMode.stopDestroyBlock()
            target = null
            MiningStats.recordBreak(brokenState, isOre(brokenState))
        }
        return false
    }

    private fun advance(player: LocalPlayer, level: Level, immediate: List<BlockPos>, facing: Direction) {
        if (immediate.any { !level.getBlockState(it).isAir }) return
        val motion = player.deltaMovement
        player.deltaMovement = Vec3(facing.stepX * WALK_SPEED, motion.y, facing.stepZ * WALK_SPEED)
        onAdvanced()
    }

    /** Direction de minage/déplacement effective : suit le regard en ligne droite, ou le style choisi sinon. */
    private fun resolveFacing(player: LocalPlayer): Direction {
        if (config.pattern == MiningPattern.STRAIGHT) return player.direction
        return currentFacing ?: player.direction
    }

    /** Fait tourner la direction courante une fois le segment (zigzag) ou le bras de spirale (cercle) terminé. */
    private fun onAdvanced() {
        if (config.pattern == MiningPattern.STRAIGHT) return

        segmentSteps++
        if (segmentSteps < segmentLength) return
        segmentSteps = 0

        val facing = currentFacing ?: baseFacing ?: return
        when (config.pattern) {
            MiningPattern.ZIGZAG -> {
                val forward = baseFacing ?: facing
                zigzagPhase = (zigzagPhase + 1) % 4
                currentFacing = when (zigzagPhase) {
                    1 -> forward.clockWise
                    3 -> forward.counterClockWise
                    else -> forward
                }
            }
            MiningPattern.CIRCLE -> {
                currentFacing = facing.clockWise
                spiralLegCount++
                if (spiralLegCount >= 2) {
                    spiralLegCount = 0
                    segmentLength++
                }
            }
            MiningPattern.STRAIGHT -> Unit
        }
    }

    private fun isBreakable(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        if (state.isAir || !state.fluidState.isEmpty) return false
        return state.getDestroySpeed(level, pos) >= 0f
    }

    private fun isOre(state: BlockState): Boolean {
        if (ORE_BLOCKS.contains(state.block)) return true
        return state.`is`(ConventionalBlockTags.ORES)
    }
}

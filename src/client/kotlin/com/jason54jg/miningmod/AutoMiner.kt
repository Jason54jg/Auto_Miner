package com.jason54jg.miningmod

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
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

    var active = false
        private set

    private var config = MiningModConfig.load()
    private var target: BlockPos? = null

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
        baseFacing = null
        currentFacing = null
    }

    fun tick(client: Minecraft) {
        if (!active) return
        val player = client.player ?: return
        val level = client.level ?: return
        val gameMode = client.gameMode ?: return

        ensureToolEquipped(player)

        if (config.lockYLevel && descendToTargetY(player, level, gameMode)) return

        syncViewToFacing(player)
        repeat(config.blocksPerTick.coerceAtLeast(1)) {
            if (!mineOrAdvance(player, level, gameMode)) return
        }
    }

    /**
     * Descend tout droit (casse le bloc sous les pieds, attend d'atterrir) jusqu'à la couche
     * [MiningModConfig.targetY]. Ce n'est pas du vrai pathfinding : juste une descente verticale,
     * qui s'arrête net si elle tombe sur un bloc incassable ou un liquide.
     *
     * @return vrai tant que la descente n'est pas terminée (le tunnel horizontal doit attendre).
     */
    private fun descendToTargetY(player: LocalPlayer, level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val current = player.blockPosition()
        if (current.y <= config.targetY) return false
        if (!player.onGround()) return true

        val below = current.below()
        if (!isBreakable(level, below)) return false

        breakBlock(below, Direction.DOWN, level, gameMode)
        return true
    }

    /** En zigzag/cercle, tourne la tête et le corps du joueur vers la direction pilotée par le mod. */
    private fun syncViewToFacing(player: LocalPlayer) {
        if (config.pattern == MiningPattern.STRAIGHT) return
        val yaw = yawFor(currentFacing ?: return)
        player.yRot = yaw
        player.yHeadRot = yaw
    }

    private fun yawFor(direction: Direction): Float = when (direction) {
        Direction.SOUTH -> 0f
        Direction.WEST -> 90f
        Direction.NORTH -> 180f
        Direction.EAST -> 270f
        else -> 0f
    }

    /** Équipe l'outil choisi dans la configuration s'il est présent dans la hotbar. */
    private fun ensureToolEquipped(player: LocalPlayer) {
        val tool = ToolSelection.item ?: return
        val inventory = player.inventory
        if (inventory.getItem(inventory.selectedSlot).item == tool) return
        for (slot in 0 until 9) {
            if (inventory.getItem(slot).item == tool) {
                inventory.setSelectedSlot(slot)
                return
            }
        }
    }

    /** Retourne vrai si un bloc a été cassé instantanément, permettant d'en tenter un autre le même tick. */
    private fun mineOrAdvance(player: LocalPlayer, level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val facing = resolveFacing(player)
        val base = player.blockPosition()
        val origin = if (config.lockYLevel) BlockPos(base.x, config.targetY, base.z) else base

        val lookahead = config.reach.toInt().coerceIn(1, MAX_LOOKAHEAD)
        val halfWidth = config.tunnelWidth.coerceIn(0, 2)
        val lateralOffsets = -halfWidth..halfWidth

        val path = (1..lookahead).flatMap { step ->
            val forward = origin.relative(facing, step)
            lateralOffsets.flatMap { offset ->
                val lateral = offsetPosition(forward, facing, offset)
                listOf(lateral, lateral.above())
            }
        }
        val immediateForward = origin.relative(facing, 1)
        val immediate = lateralOffsets.flatMap { offset ->
            val lateral = offsetPosition(immediateForward, facing, offset)
            listOf(lateral, lateral.above())
        }

        val ores = path
            .flatMap { pos -> Direction.values().map(pos::relative) }
            .filter { pos -> pos !in path && OreBlocks.isOre(level.getBlockState(pos)) }
            .distinct()

        val current = target?.takeIf { isBreakable(level, it) }
            ?: (ores + path).firstOrNull { isBreakable(level, it) }

        if (current == null) {
            target = null
            advance(player, level, immediate, facing)
            return false
        }

        return breakBlock(current, facing, level, gameMode)
    }

    /** Décale [pos] latéralement par rapport à [facing] : positif = côté horaire, négatif = côté antihoraire. */
    private fun offsetPosition(pos: BlockPos, facing: Direction, offset: Int): BlockPos {
        if (offset == 0) return pos
        val side = if (offset > 0) facing.clockWise else facing.counterClockWise
        return pos.relative(side, kotlin.math.abs(offset))
    }

    /**
     * Fait avancer la casse de [pos] d'un cran (démarre ou continue selon [target]).
     * Se fie à l'état réel du bloc plutôt qu'à la valeur de retour de start/continueDestroyBlock :
     * sur certaines versions, "true" ne veut pas forcément dire "détruit".
     *
     * @return vrai si le bloc est confirmé détruit.
     */
    private fun breakBlock(pos: BlockPos, facing: Direction, level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val brokenState = level.getBlockState(pos)
        if (pos != target) {
            gameMode.stopDestroyBlock()
            target = pos
            gameMode.startDestroyBlock(pos, facing.opposite)
        } else {
            gameMode.continueDestroyBlock(pos, facing.opposite)
        }

        if (!level.getBlockState(pos).isAir) return false

        gameMode.stopDestroyBlock()
        target = null
        MiningStats.recordBreak(brokenState, OreBlocks.isOre(brokenState))
        return true
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
}

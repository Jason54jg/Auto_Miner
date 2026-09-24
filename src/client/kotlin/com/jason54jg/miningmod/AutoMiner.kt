package com.jason54jg.miningmod

import com.jason54jg.miningmod.client.AlertManager
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * Creuse tout droit dans la direction figée à l'activation tant que [active] est vrai, en cassant
 * systématiquement tout minerai touchant le tunnel avant d'avancer.
 *
 * [config] permet de s'adapter à un serveur non-vanilla (portée étendue, capacités
 * de casse instantanée multiple, verrouillage du niveau Y pour du strip mining).
 */
object AutoMiner {
    private const val WALK_SPEED = 0.2
    private const val MAX_LOOKAHEAD = 6

    // Vitesse verticale d'un saut vanilla (sans effet de saut) : utilisée pour franchir un trou d'un seul bloc.
    private const val JUMP_VELOCITY = 0.42

    // Nombre de ticks d'affilée sans casser ni avancer avant de conclure à un obstacle réel
    // (mur incassable, liquide sans minerai autour...) plutôt qu'un blocage transitoire d'un tick.
    private const val STUCK_THRESHOLD = 20

    var active = false
        private set

    private var config = MiningModConfig.load()
    private var target: BlockPos? = null
    private var stuckTicks = 0

    // Direction figée à l'activation : jamais celle du regard en direct.
    private var baseFacing: Direction? = null

    fun applyConfig(newConfig: MiningModConfig) {
        config = newConfig
    }

    fun toggle(client: Minecraft) {
        active = !active
        client.player?.sendSystemMessage(
            Component.literal(if (active) "Minage automatique activé" else "Minage automatique désactivé")
        )
        if (active) {
            MiningStats.startSession()
            baseFacing = client.player?.direction
            stuckTicks = 0
        } else {
            reset(client)
        }
    }

    fun reset(client: Minecraft) {
        client.gameMode?.stopDestroyBlock()
        target = null
        baseFacing = null
        stuckTicks = 0
    }

    fun tick(client: Minecraft) {
        if (!active) return
        val player = client.player ?: return
        val level = client.level ?: return
        val gameMode = client.gameMode ?: return

        ensureToolEquipped(player)
        // Toujours synchronisée, même pendant la descente Y ou une chute : sinon la tête reste
        // figée dans la direction où le joueur regardait au moment de la chute.
        syncViewToFacing(player)

        if (config.lockYLevel && descendToTargetY(player, level, gameMode)) return

        repeat(config.blocksPerTick.coerceAtLeast(1)) {
            if (!mineOrAdvance(player, level, gameMode)) {
                reactIfStuck(client)
                return
            }
        }
    }

    /**
     * Rien à casser ni à avancer pendant [STUCK_THRESHOLD] ticks d'affilée (mur incassable, liquide
     * sans minerai autour...) : plutôt que de rester bloqué en silence indéfiniment, coupe le
     * minage et alerte le joueur (son + flash), comme pour une téléportation/mention.
     */
    private fun reactIfStuck(client: Minecraft) {
        if (stuckTicks < STUCK_THRESHOLD) return
        toggle(client)
        AlertManager.notifyStuck(client)
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

    /**
     * Le mod pilote entièrement la caméra : aucun mouvement du joueur n'est nécessaire une fois
     * le minage activé. La vue reste figée sur la direction de départ.
     */
    private fun syncViewToFacing(player: LocalPlayer) {
        val yaw = yawFor(baseFacing ?: return)
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
        val immediateFeet = lateralOffsets.map { offset -> offsetPosition(immediateForward, facing, offset) }
        val immediate = immediateFeet.flatMap { listOf(it, it.above()) }

        // Ne jamais casser le bloc qui porte le joueur : sinon un minerai/bloc obligatoire détecté
        // juste sous les pieds le fait tomber dans le vide au lieu de continuer à miner.
        val standingOn = base.below()

        val touchingOres = path
            .flatMap { pos -> Direction.values().map(pos::relative) }
            .filter { pos -> pos !in path && pos != standingOn && isVanillaOre(level.getBlockState(pos)) }
            .distinct()
        val nearbyCustomBlocks = scanForCustomBlock(level, origin, lookahead)
            .filter { it != standingOn }
            .sortedBy { it.distSqr(origin) }

        val current = target?.takeIf { isBreakable(level, it) }
            ?: (nearbyCustomBlocks + touchingOres + path).firstOrNull { isBreakable(level, it) }

        if (current == null) {
            target = null
            stuckTicks = if (advance(player, level, immediate, immediateFeet, facing)) 0 else stuckTicks + 1
            return false
        }

        stuckTicks = 0
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
        MiningStats.recordBreak(brokenState, isPriorityOre(brokenState))
        return true
    }

    private fun isVanillaOre(state: BlockState): Boolean = OreBlocks.isOre(state)

    private fun isCustomBlock(state: BlockState): Boolean = state.block == CustomOreSelection.block

    /** Minerai vanilla/tag, ou bloc custom désigné par le joueur comme "à casser impérativement". */
    private fun isPriorityOre(state: BlockState): Boolean = isVanillaOre(state) || isCustomBlock(state)

    /**
     * Balaie un cube de [radius] blocs autour de [center] à la recherche du bloc obligatoire
     * uniquement : lui doit être détecté où qu'il soit autour du joueur. Les minerais vanilla
     * restent limités à ce qui touche le tunnel (sinon ça troue les murs au hasard sur un simple
     * minerai de fer croisé au passage).
     */
    private fun scanForCustomBlock(level: Level, center: BlockPos, radius: Int): List<BlockPos> {
        if (CustomOreSelection.block == null) return emptyList()
        val range = -radius..radius
        val positions = ArrayList<BlockPos>()
        for (dx in range) for (dy in range) for (dz in range) {
            positions.add(center.offset(dx, dy, dz))
        }
        return positions.filter { pos -> isCustomBlock(level.getBlockState(pos)) }
    }

    /** @return vrai si le joueur a effectivement avancé (ou sauté), faux si un obstacle bloque le passage. */
    private fun advance(player: LocalPlayer, level: Level, immediate: List<BlockPos>, feet: List<BlockPos>, facing: Direction): Boolean {
        if (immediate.any { !level.getBlockState(it).isAir }) return false

        var verticalMotion = player.deltaMovement.y
        // Sans verrouillage de couche Y, aucune hauteur à maintenir : le bot ignore les trous et
        // laisse la gravité faire son travail au lieu de sauter/s'arrêter (le bloc devant, lui,
        // reste cassé normalement quoi qu'il arrive : voir mineOrAdvance).
        if (config.lockYLevel && feet.any { level.getBlockState(it.below()).isAir }) {
            // Trou d'un seul bloc de large : saute par-dessus plutôt que de s'arrêter. Un trou plus large
            // (pas de sol non plus deux blocs après) reste bloquant : on attend/mine plutôt que de tomber.
            val landing = feet.map { it.relative(facing, 1) }
            if (landing.any { level.getBlockState(it.below()).isAir }) return false
            if (player.onGround()) verticalMotion = JUMP_VELOCITY
        }

        player.deltaMovement = Vec3(facing.stepX * WALK_SPEED, verticalMotion, facing.stepZ * WALK_SPEED)
        return true
    }

    /** Direction de minage/déplacement effective : figée à l'activation, jamais celle du regard en direct. */
    private fun resolveFacing(player: LocalPlayer): Direction = baseFacing ?: player.direction

    private fun isBreakable(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        // state.liquid() exclut l'eau/lave elles-mêmes, mais pas un bloc solide "waterlogged"
        // (un minerai custom immergé, par ex.) : ça, c'est bien un bloc à casser.
        if (state.isAir || state.liquid()) return false
        return state.getDestroySpeed(level, pos) >= 0f
    }
}

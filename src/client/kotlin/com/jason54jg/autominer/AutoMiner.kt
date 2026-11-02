package com.jason54jg.autominer

import com.jason54jg.autominer.client.AlertManager
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil

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

    private var config = AutoMinerModConfig.load()
    private var target: BlockPos? = null
    private var targetState: BlockState? = null
    private var lastDestroyStage = -1
    private var stalledBreakTicks = 0
    private var stuckTicks = 0
    private var sessionLevel: Level? = null

    private enum class DescentResult { AT_LEVEL, IN_PROGRESS, BLOCKED }

    // Direction figée à l'activation : jamais celle du regard en direct.
    private var baseFacing: Direction? = null
    private var sidestepDirection = 1

    fun applyConfig(newConfig: AutoMinerModConfig) {
        config = newConfig
    }

    fun toggle(client: Minecraft) {
        if (!active && (client.player == null || client.level == null || client.gameMode == null)) return
        active = !active
        val messageKey = if (active) "chat.autominer.enabled" else "chat.autominer.disabled"
        val color = if (active) ChatFormatting.GREEN else ChatFormatting.RED
        client.player?.sendSystemMessage(Component.translatable(messageKey).withStyle(color))
        if (active) {
            MiningStats.startSession()
            baseFacing = client.player?.direction
            sessionLevel = client.level
            stuckTicks = 0
            sidestepDirection = 1
        } else {
            reset(client)
        }
    }

    fun reset(client: Minecraft) {
        clearTarget(client.gameMode)
        baseFacing = null
        sessionLevel = null
        stuckTicks = 0
        sidestepDirection = 1
        client.player?.let(::haltHorizontalMovement)
    }

    /** Arrête aussi la session lors d'une déconnexion ou d'un changement de monde. */
    fun stop(client: Minecraft) {
        active = false
        reset(client)
    }

    fun tick(client: Minecraft) {
        if (!active) return
        val player = client.player
        val level = client.level
        val gameMode = client.gameMode
        if (player == null || level == null || gameMode == null || level !== sessionLevel) {
            stop(client)
            return
        }

        // Au sol, n'avance que si le passage est sûr. En l'air, conserve l'élan du saut
        // pendant que la logique de descente attend le retour à la couche choisie.
        if (player.onGround()) haltHorizontalMovement(player)
        finishTargetIfDestroyed(level, gameMode)

        ensureToolEquipped(player)
        // Toujours synchronisée, même pendant la descente Y ou une chute : sinon la tête reste
        // figée dans la direction où le joueur regardait au moment de la chute.
        syncViewToFacing(player)

        if (config.lockYLevel) {
            when (descendToTargetY(player, level, gameMode)) {
                DescentResult.AT_LEVEL -> Unit
                DescentResult.IN_PROGRESS -> {
                    reactIfStuck(client)
                    return
                }
                DescentResult.BLOCKED -> {
                    toggle(client)
                    AlertManager.notifyStuck(client)
                    return
                }
            }
        }

        val blocksThisTick = config.blocksPerTick.takeIf { it > 0 } ?: 1
        repeat(blocksThisTick) {
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
     * [AutoMinerModConfig.targetY]. Ce n'est pas du vrai pathfinding : juste une descente verticale,
     * qui s'arrête net si elle tombe sur un bloc incassable ou un liquide.
     *
     * Distingue une descente bloquée d'une hauteur atteinte : aucune casse horizontale
     * ne doit commencer tant que le joueur n'a pas atteint sa couche.
     */
    private fun descendToTargetY(player: LocalPlayer, level: Level, gameMode: MultiPlayerGameMode): DescentResult {
        val current = player.blockPosition()
        if (current.y <= config.targetY) return DescentResult.AT_LEVEL
        if (!player.onGround()) return DescentResult.IN_PROGRESS

        val below = current.below()
        if (!isBreakable(level, below) || !isWithinReach(player, below)) return DescentResult.BLOCKED

        breakBlock(below, Direction.DOWN, player, level, gameMode)
        return DescentResult.IN_PROGRESS
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
        val obstacleReach = if (smartNavigation()) {
            config.obstacleDetectionReach.coerceIn(1, MAX_LOOKAHEAD)
        } else {
            0
        }

        // Tête avant pieds : le joueur regarde à hauteur des yeux (viseur au niveau de la tête), donc
        // c'est ce bloc-là qui doit visiblement se casser en premier, pas celui du sol sous le viseur.
        val path = (1..lookahead).flatMap { step ->
            val forward = origin.relative(facing, step)
            lateralOffsets.flatMap { offset ->
                val lateral = offsetPosition(forward, facing, offset)
                listOf(lateral.above(), lateral)
            }
        } + obstacleBlocks(level, origin, facing, lateralOffsets, obstacleReach)
        val immediateForward = origin.relative(facing, 1)
        val immediateFeet = lateralOffsets.map { offset -> offsetPosition(immediateForward, facing, offset) }
        val immediate = immediateFeet.flatMap { listOf(it, it.above()) }

        // Ne jamais casser le bloc qui porte le joueur : sinon un minerai/bloc obligatoire détecté
        // juste sous les pieds le fait tomber dans le vide au lieu de continuer à miner.
        val standingOn = base.below()

        val touchingOres = path
            .flatMap { pos -> listOf(pos) + Direction.values().map(pos::relative) }
            .filter { pos -> pos != standingOn && isVanillaOre(level.getBlockState(pos)) }
            .distinct()
        val nearbyCustomBlocks = scanForCustomBlock(level, origin, lookahead)
            .filter { it != standingOn }
            .sortedBy { it.distSqr(origin) }

        val current = target?.takeIf { it != standingOn && isBreakable(level, it) && isWithinReach(player, it) }
            ?: (nearbyCustomBlocks + touchingOres + path).firstOrNull { isBreakable(level, it) && isWithinReach(player, it) }

        if (current == null) {
            clearTarget(gameMode)
            val moved = advance(player, level, immediate, immediateFeet, facing)
            stuckTicks = if (moved) 0 else stuckTicks + 1
            return false
        }

        return breakBlock(current, facing, player, level, gameMode)
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
    private fun breakBlock(pos: BlockPos, facing: Direction, player: LocalPlayer, level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val brokenState = level.getBlockState(pos)
        if (pos != target) {
            clearTarget(gameMode)
            target = pos
            targetState = brokenState
            gameMode.startDestroyBlock(pos, facing.opposite)
        } else {
            gameMode.continueDestroyBlock(pos, facing.opposite)
        }

        if (finishTargetIfDestroyed(level, gameMode)) return true

        val stage = gameMode.destroyStage
        if (stage > lastDestroyStage) {
            stalledBreakTicks = 0
            lastDestroyStage = stage
        } else {
            stalledBreakTicks++
        }
        // Une pioche lente peut rester plus d'une seconde sur le même stade de fissure.
        // Le délai suit donc le temps normal de progression, sans attendre indéfiniment
        // sur un bloc refusé par le serveur ou un mode de jeu.
        val progress = brokenState.getDestroyProgress(player, level, pos).toDouble()
        val allowance = if (progress.isFinite() && progress > 0.0) {
            maxOf(STUCK_THRESHOLD.toDouble(), ceil(0.1 / progress) + STUCK_THRESHOLD)
        } else STUCK_THRESHOLD.toDouble()
        stuckTicks = if (stalledBreakTicks >= allowance) STUCK_THRESHOLD else 0
        return false
    }

    /** Prend aussi en compte une mise à jour différée et l'eau laissée par un bloc waterlogged. */
    private fun finishTargetIfDestroyed(level: Level, gameMode: MultiPlayerGameMode): Boolean {
        val pos = target ?: return false
        val before = targetState ?: return false
        val after = level.getBlockState(pos)
        if (after.block == before.block || (!after.isAir && !after.liquid())) return false
        MiningStats.recordBreak(before, isPriorityOre(before))
        clearTarget(gameMode)
        stuckTicks = 0
        return true
    }

    private fun clearTarget(gameMode: MultiPlayerGameMode?) {
        if (target != null) gameMode?.stopDestroyBlock()
        target = null
        targetState = null
        lastDestroyStage = -1
        stalledBreakTicks = 0
    }

    private fun isWithinReach(player: LocalPlayer, pos: BlockPos): Boolean {
        val reach = config.reach
        return reach.isFinite() && reach > 0.0 &&
            player.isWithinBlockInteractionRange(pos, 0.0) &&
            AABB(pos).distanceToSqr(player.eyePosition) <= reach * reach
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

    /** Plafonds / murs latéraux proches à inclure dans la file de casse (mode intelligent). */
    private fun obstacleBlocks(
        level: Level,
        origin: BlockPos,
        facing: Direction,
        lateralOffsets: IntRange,
        reach: Int
    ): List<BlockPos> {
        if (reach <= 0) return emptyList()
        val blocks = ArrayList<BlockPos>()
        for (step in 1..reach) {
            val forward = origin.relative(facing, step)
            for (offset in lateralOffsets) {
                val lateral = offsetPosition(forward, facing, offset)
                blocks.add(lateral.above(2))
            }
            val left = offsetPosition(forward, facing, -1)
            val right = offsetPosition(forward, facing, 1)
            blocks.add(left)
            blocks.add(left.above())
            blocks.add(right)
            blocks.add(right.above())
        }
        return blocks.filter { isBreakable(level, it) }.distinct()
    }

    private fun smartNavigation(): Boolean =
        config.obstacleNavigationMode == ObstacleNavigationMode.SMART

    /** @return vrai si le joueur a effectivement avancé (ou sauté), faux si un obstacle bloque le passage. */
    private fun advance(
        player: LocalPlayer,
        level: Level,
        immediate: List<BlockPos>,
        feet: List<BlockPos>,
        facing: Direction
    ): Boolean {
        if (immediate.any { !level.getBlockState(it).isAir }) {
            return tryBypass(player, level, facing)
        }

        var verticalMotion = player.deltaMovement.y
        // Sans verrouillage de couche Y, ou en vol (créatif/spectateur), aucune hauteur à
        // maintenir : le bot ignore les trous et avance tout droit sans se soucier du sol
        // (le bloc devant, lui, reste cassé normalement quoi qu'il arrive : voir mineOrAdvance).
        if (config.lockYLevel && !player.abilities.flying && feet.any { !hasSafeFloor(level, it.below()) }) {
            if (config.descendUnderOverhangs && smartNavigation() && canStepDownForward(level, feet)) {
                player.deltaMovement = Vec3(facing.stepX * WALK_SPEED, verticalMotion, facing.stepZ * WALK_SPEED)
                return true
            }
            // Trou d'un seul bloc de large : saute par-dessus plutôt que de s'arrêter. Un trou plus large
            // (pas de sol non plus deux blocs après) reste bloquant : on attend/mine plutôt que de tomber.
            val landing = feet.map { it.relative(facing, 1) }
            if (landing.any { !hasSafeFloor(level, it.below()) || !level.getBlockState(it).isAir || !level.getBlockState(it.above()).isAir }) {
                return tryBypass(player, level, facing)
            }
            if (player.onGround()) verticalMotion = JUMP_VELOCITY
        }

        player.deltaMovement = Vec3(facing.stepX * WALK_SPEED, verticalMotion, facing.stepZ * WALK_SPEED)
        return true
    }

    /** Marche d'un bloc : sol devant absent mais support un bloc plus bas (sous un surplomb / après AOE). */
    private fun canStepDownForward(level: Level, feet: List<BlockPos>): Boolean {
        return feet.all { foot ->
            level.getBlockState(foot).isAir &&
                level.getBlockState(foot.above()).isAir &&
                level.getBlockState(foot.below()).isAir &&
                hasSafeFloor(level, foot.below(2))
        }
    }

    /** Décalage latéral si le tunnel est bloqué depuis quelques ticks (pioche AOE, mur résiduel). */
    private fun tryBypass(player: LocalPlayer, level: Level, facing: Direction): Boolean {
        if (!smartNavigation() || !config.bypassObstacles || stuckTicks < 4) return false
        val side = if (sidestepDirection >= 0) facing.clockWise else facing.counterClockWise
        val sidePos = player.blockPosition().relative(side)
        val floorY = if (config.lockYLevel) config.targetY else player.blockPosition().y
        if (!isWalkableColumn(level, sidePos, floorY)) {
            sidestepDirection = -sidestepDirection
            return false
        }
        sidestepDirection = -sidestepDirection
        player.deltaMovement = Vec3(side.stepX * WALK_SPEED, player.deltaMovement.y, side.stepZ * WALK_SPEED)
        return true
    }

    private fun isWalkableColumn(level: Level, base: BlockPos, floorY: Int): Boolean {
        val feet = BlockPos(base.x, floorY, base.z)
        if (!level.getBlockState(feet).isAir) return false
        if (!level.getBlockState(feet.above()).isAir) return false
        val ground = feet.below()
        return hasSafeFloor(level, ground)
    }

    private fun hasSafeFloor(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        return state.fluidState.isEmpty && state.isFaceSturdy(level, pos, Direction.UP)
    }

    private fun haltHorizontalMovement(player: LocalPlayer) {
        player.deltaMovement = Vec3(0.0, player.deltaMovement.y, 0.0)
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

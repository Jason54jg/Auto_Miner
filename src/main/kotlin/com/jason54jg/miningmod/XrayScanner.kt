package com.jason54jg.miningmod

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block

/** Scanne périodiquement les alentours du joueur pour repérer les minerais choisis dans la config. */
object XrayScanner {
    private const val RADIUS = 16
    private const val RESCAN_INTERVAL_TICKS = 20

    private var ticksUntilRescan = 0

    var visiblePositions: List<BlockPos> = emptyList()
        private set

    fun tick(client: Minecraft, config: MiningModConfig) {
        val matchSet = when (config.xrayPreset) {
            XrayPreset.NONE -> emptySet()
            XrayPreset.VALUABLE -> OreBlocks.VALUABLE
            XrayPreset.ALL -> OreBlocks.ALL
        }
        if (matchSet.isEmpty()) {
            visiblePositions = emptyList()
            return
        }

        if (ticksUntilRescan > 0) {
            ticksUntilRescan--
            return
        }
        ticksUntilRescan = RESCAN_INTERVAL_TICKS
        scan(client, matchSet)
    }

    private fun scan(client: Minecraft, matchSet: Set<Block>) {
        val player = client.player ?: return
        val level = client.level ?: return
        val center = player.blockPosition()

        val found = mutableListOf<BlockPos>()
        for (dx in -RADIUS..RADIUS) {
            for (dy in -RADIUS..RADIUS) {
                for (dz in -RADIUS..RADIUS) {
                    val pos = center.offset(dx, dy, dz)
                    if (level.getBlockState(pos).block in matchSet) found += pos
                }
            }
        }
        visiblePositions = found
    }
}

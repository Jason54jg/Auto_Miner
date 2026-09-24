package com.jason54jg.miningmod

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

/** Ensembles de minerais partagés entre le minage automatique et le x-ray. */
object OreBlocks {
    val VALUABLE: Set<Block> = setOf(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.ANCIENT_DEBRIS
    )

    val ALL: Set<Block> = VALUABLE + setOf(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE
    )

    fun isOre(state: BlockState): Boolean {
        if (ALL.contains(state.block)) return true
        return state.`is`(ConventionalBlockTags.ORES)
    }
}

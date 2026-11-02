package net.minecraft.world.level
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
class Level {
 val states=mutableMapOf<BlockPos,BlockState>()
 fun getBlockState(p:BlockPos)=states[p]?:BlockState(Blocks.AIR)
 fun put(p:BlockPos,b:Block){states[p]=BlockState(b)}
}

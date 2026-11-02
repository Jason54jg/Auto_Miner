package net.minecraft.world.level.block.state
import net.minecraft.world.level.block.*
import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos
class BlockState(val block:Block) {
 val isAir get()=block==Blocks.AIR
 fun liquid()=block.liquid
 fun getDestroySpeed(level:Level,pos:BlockPos)=block.hardness
 fun `is`(tag:Any)=block.ore
 val fluidState get()=net.minecraft.world.level.material.FluidState(!block.fluid)
 fun isFaceSturdy(level:Level,pos:BlockPos,direction:net.minecraft.core.Direction)=block.sturdy
 fun getDestroyProgress(player:net.minecraft.client.player.LocalPlayer,level:Level,pos:BlockPos)=block.progress
}

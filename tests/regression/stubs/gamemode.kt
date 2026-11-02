package net.minecraft.client.multiplayer
import net.minecraft.core.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.phys.Vec3
class MultiPlayerGameMode(val level:Level,val player:LocalPlayer) {
 val attempts=mutableListOf<BlockPos>()
 var instant=true
 var allowMining=true
 var destroyStage=-1
  private set
 private var progress=0f
 var enforceReach=false
 var replaceWith=Blocks.AIR
 fun stopDestroyBlock(){progress=0f;destroyStage=-1}
 fun startDestroyBlock(p:BlockPos,f:Direction)=attempt(p)
 fun continueDestroyBlock(p:BlockPos,f:Direction)=attempt(p)
 private fun attempt(p:BlockPos):Boolean {
  attempts.add(p)
  val eye=Vec3(player.pos.x+0.5,player.pos.y+1.62,player.pos.z+0.5)
  if(enforceReach && eye.distanceTo(Vec3(p.x+0.5,p.y+0.5,p.z+0.5))>5.5)return false
  if(!allowMining)return false
  if(level.getBlockState(p).block.hardness>=0) {
   if(!instant) {progress+=level.getBlockState(p).block.progress;destroyStage=(progress*10).toInt()}
   if(instant || progress>=1f)level.put(p,replaceWith)
  }
  return true
 }
}

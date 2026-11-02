package net.minecraft.client.player
import net.minecraft.core.*
import net.minecraft.world.phys.Vec3
import net.minecraft.world.item.*
import net.minecraft.network.chat.Component
class Inventory {
 val slots=MutableList(9){ItemStack(Items.AIR)}
 var selectedSlot:Int=0
  private set
 fun setSelectedSlot(slot:Int){selectedSlot=slot}
 fun getItem(slot:Int)=slots[slot]
}
class Abilities {var flying=false}
class LocalPlayer {
 var interactionRange=4.5
 val eyePosition get()=Vec3(pos.x+0.5,pos.y+1.62,pos.z+0.5)
 fun isWithinBlockInteractionRange(pos:BlockPos,extra:Double)=net.minecraft.world.phys.AABB(pos).distanceToSqr(eyePosition)<(interactionRange+extra)*(interactionRange+extra)
 var pos=BlockPos(0,0,0)
 var direction=Direction.SOUTH
 var yRot=0f
 var yHeadRot=0f
 var deltaMovement=Vec3(0.0,0.0,0.0)
 var grounded=true
 val abilities=Abilities()
 val inventory=Inventory()
 val scoreboardName="Player"
 fun blockPosition()=pos
 fun onGround()=grounded
 fun position()=Vec3(pos.x+0.5,pos.y.toDouble(),pos.z+0.5)
 fun sendSystemMessage(c:Component){}
}

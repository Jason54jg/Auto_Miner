package net.minecraft.world.phys
import net.minecraft.core.BlockPos
class AABB(private val pos:BlockPos) {
 fun distanceToSqr(eye:Vec3):Double {
  val dx=eye.x-eye.x.coerceIn(pos.x.toDouble(),pos.x+1.0)
  val dy=eye.y-eye.y.coerceIn(pos.y.toDouble(),pos.y+1.0)
  val dz=eye.z-eye.z.coerceIn(pos.z.toDouble(),pos.z+1.0)
  return dx*dx+dy*dy+dz*dz
 }
}

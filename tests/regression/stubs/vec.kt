package net.minecraft.world.phys
import kotlin.math.sqrt
data class Vec3(val x:Double,val y:Double,val z:Double) {
 fun distanceTo(o:Vec3)=sqrt((x-o.x)*(x-o.x)+(y-o.y)*(y-o.y)+(z-o.z)*(z-o.z))
}

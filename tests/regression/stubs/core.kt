package net.minecraft.core
import kotlin.math.pow
enum class Direction(val stepX:Int, val stepY:Int, val stepZ:Int) {
 DOWN(0,-1,0), UP(0,1,0), NORTH(0,0,-1), SOUTH(0,0,1), WEST(-1,0,0), EAST(1,0,0);
 val opposite:Direction get()=when(this){DOWN->UP;UP->DOWN;NORTH->SOUTH;SOUTH->NORTH;WEST->EAST;EAST->WEST}
 val clockWise:Direction get()=when(this){NORTH->EAST;EAST->SOUTH;SOUTH->WEST;else->NORTH}
 val counterClockWise:Direction get()=clockWise.opposite
}
data class BlockPos(val x:Int,val y:Int,val z:Int) {
 fun relative(d:Direction,n:Int=1)=offset(d.stepX*n,d.stepY*n,d.stepZ*n)
 fun above(n:Int=1)=offset(0,n,0)
 fun below(n:Int=1)=offset(0,-n,0)
 fun offset(dx:Int,dy:Int,dz:Int)=BlockPos(x+dx,y+dy,z+dz)
 fun distSqr(p:BlockPos)=(x-p.x).toDouble().pow(2)+(y-p.y).toDouble().pow(2)+(z-p.z).toDouble().pow(2)
}

package net.minecraft.resources
data class Identifier(val namespace:String,val path:String) {
 override fun toString()="$namespace:$path"
 companion object {
  fun parse(s:String):Identifier { require(s.matches(Regex("[a-z0-9_.-]+:[a-z0-9_./-]+")));return Identifier(s.substringBefore(":"),s.substringAfter(":")) }
  fun fromNamespaceAndPath(n:String,p:String)=Identifier(n,p)
 }
}

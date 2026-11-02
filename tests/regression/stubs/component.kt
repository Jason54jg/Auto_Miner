package net.minecraft.network.chat
import net.minecraft.ChatFormatting
class Component(val string:String) {
 fun withStyle(c:ChatFormatting)=this
 companion object {fun translatable(key:String,vararg args:Any)=Component(key+args.joinToString());fun literal(text:String)=Component(text)}
}

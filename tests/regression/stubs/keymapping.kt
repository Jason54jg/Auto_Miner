package net.minecraft.client
import net.minecraft.resources.Identifier
import net.minecraft.network.chat.Component
class KeyMapping(val name:String,key:Int,category:Category) {
 var clicks=0
 fun consumeClick():Boolean {if(clicks<=0)return false;clicks--;return true}
 val translatedKeyMessage get()=Component.literal("B")
 class Category {companion object {fun register(id:Identifier)=Category()}}
}

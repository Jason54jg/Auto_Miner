package net.minecraft.world.item
class Item(val id:String) {val descriptionId get()="item."+id.replace(':','.')}
object Items {val AIR=Item("minecraft:air");val PICKAXE=Item("minecraft:diamond_pickaxe")}
class ItemStack(val item:Item) {val isEmpty get()=item==Items.AIR}

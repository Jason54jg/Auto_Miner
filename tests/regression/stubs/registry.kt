package net.minecraft.core.registries
import java.util.Optional
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.*
import net.minecraft.world.item.*
class Holder<T>(private val obj:T) {fun value()=obj}
class Registry<T:Any>(val values:List<T>,val id:(T)->String) {
 fun getKey(obj:T)=Identifier.parse(id(obj))
 fun get(key:Identifier):Optional<Holder<T>> = Optional.ofNullable(values.find{id(it)==key.toString()}?.let{Holder(it)})
}
object BuiltInRegistries {
 val BLOCK=Registry(Blocks::class.java.declaredFields.mapNotNull{f->f.isAccessible=true; f.get(null) as? Block}){it.id}
 val ITEM=Registry(listOf(Items.AIR,Items.PICKAXE)){it.id}
}

package com.jason54jg.miningmod

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/**
 * Outil choisi dans la configuration pour le minage automatique (le mod l'équipe s'il est
 * dans la hotbar). Persisté via [MiningModConfig.toolItemId] (résolu au chargement).
 */
object ToolSelection {
    var item: Item? = MiningModConfig.load().toolItemId?.let(::resolve)

    fun idOf(item: Item): String = BuiltInRegistries.ITEM.getKey(item).toString()

    private fun resolve(id: String): Item? {
        val identifier = runCatching { Identifier.parse(id) }.getOrNull() ?: return null
        val item = BuiltInRegistries.ITEM.get(identifier)?.orElse(null)?.value()
        return item?.takeUnless { it == Items.AIR }
    }
}

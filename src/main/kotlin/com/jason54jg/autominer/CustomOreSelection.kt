package com.jason54jg.autominer

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

/**
 * Bloc supplémentaire désigné par le joueur comme "à casser impérativement", en plus
 * de la liste de minerais vanilla (utile pour un bloc custom d'un serveur moddé).
 * Persisté via [AutoMinerModConfig.customBlockId] (résolu au chargement).
 */
object CustomOreSelection {
    var block: Block? = AutoMinerModConfig.load().customBlockId?.let(::resolve)

    fun idOf(block: Block): String = BuiltInRegistries.BLOCK.getKey(block).toString()

    private fun resolve(id: String): Block? {
        val identifier = runCatching { Identifier.parse(id) }.getOrNull() ?: return null
        val block = BuiltInRegistries.BLOCK.get(identifier)?.orElse(null)?.value()
        return block?.takeUnless { it == Blocks.AIR }
    }
}

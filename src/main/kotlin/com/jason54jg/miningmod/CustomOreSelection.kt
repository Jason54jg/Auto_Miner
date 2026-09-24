package com.jason54jg.miningmod

import net.minecraft.world.level.block.Block

/**
 * Bloc supplémentaire désigné par le joueur comme "à casser impérativement", en plus
 * de la liste de minerais vanilla (utile pour un bloc custom d'un serveur moddé).
 * Capturé depuis la main du joueur dans l'écran de configuration, non persisté.
 */
object CustomOreSelection {
    var block: Block? = null
}

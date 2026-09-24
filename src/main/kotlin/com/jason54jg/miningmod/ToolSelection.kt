package com.jason54jg.miningmod

import net.minecraft.world.item.Item

/**
 * Outil choisi dans la configuration pour le minage automatique (le mod l'équipe s'il est
 * dans la hotbar). Volontairement non persisté entre les sessions du jeu : capturé depuis
 * la main du joueur au moment du clic dans la "slot" de la configuration.
 */
object ToolSelection {
    var item: Item? = null
}

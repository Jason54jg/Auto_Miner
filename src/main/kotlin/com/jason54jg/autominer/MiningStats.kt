package com.jason54jg.autominer

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.BlockState

/** Statistiques de la session de minage en cours (remises à zéro à chaque activation). */
object MiningStats {
    var blocksBroken = 0
        private set

    private val oreCounts = linkedMapOf<String, Int>()
    val ores: Map<String, Int> get() = oreCounts

    private var sessionStartMs = 0L

    fun startSession() {
        blocksBroken = 0
        oreCounts.clear()
        sessionStartMs = System.currentTimeMillis()
    }

    fun elapsedMs(): Long = if (sessionStartMs == 0L) 0L else System.currentTimeMillis() - sessionStartMs

    fun recordBreak(state: BlockState, wasOre: Boolean) {
        blocksBroken++
        if (!wasOre) return
        val label = oreLabel(state)
        oreCounts[label] = (oreCounts[label] ?: 0) + 1
    }

    /**
     * Fusionne les variantes deepslate avec le minerai de base (ex: minerai de fer normal/deepslate
     * comptés ensemble). Retourne la clé de traduction (`descriptionId`) du bloc de base, résolue à
     * l'affichage : le nom reste dans la langue du client au lieu d'être figé en anglais.
     */
    private fun oreLabel(state: BlockState): String {
        val id = BuiltInRegistries.BLOCK.getKey(state.block)
        val baseId = Identifier.fromNamespaceAndPath(id.namespace, id.path.removePrefix("deepslate_"))
        val baseBlock = BuiltInRegistries.BLOCK.get(baseId)?.orElse(null)?.value() ?: state.block
        return baseBlock.descriptionId
    }
}

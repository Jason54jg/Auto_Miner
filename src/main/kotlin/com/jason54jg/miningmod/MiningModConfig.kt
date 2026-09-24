package com.jason54jg.miningmod

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

/**
 * Réglages persistés du mod, pour s'adapter aux serveurs non-vanilla
 * (portée étendue, capacités de casse multiple, etc.).
 */
data class MiningModConfig(
    var reach: Double = 4.5,
    var blocksPerTick: Int = 1,
    var lockYLevel: Boolean = false,
    var targetY: Int = -59,
    /** Rayon latéral du tunnel : 0 = 1 colonne (par défaut), 1 = 3 colonnes, 2 = 5 colonnes. */
    var tunnelWidth: Int = 0,
    /** Position du HUD de minage (coin haut-gauche du panneau), en pixels depuis le bord de l'écran. */
    var hudX: Int = 6,
    var hudY: Int = 6,
    /** Identifiant ("namespace:path") de l'outil/du bloc obligatoire choisis, pour survivre à un redémarrage. */
    var toolItemId: String? = null,
    var customBlockId: String? = null
) {
    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private val PATH = FabricLoader.getInstance().configDir.resolve("miningmod.json")

        fun load(): MiningModConfig {
            if (!Files.exists(PATH)) {
                return MiningModConfig().also(::save)
            }
            return runCatching {
                Files.newBufferedReader(PATH).use { GSON.fromJson(it, MiningModConfig::class.java) }
            }.getOrElse { MiningModConfig() }
        }

        fun save(config: MiningModConfig) {
            Files.createDirectories(PATH.parent)
            Files.newBufferedWriter(PATH).use { GSON.toJson(config, it) }
        }
    }
}

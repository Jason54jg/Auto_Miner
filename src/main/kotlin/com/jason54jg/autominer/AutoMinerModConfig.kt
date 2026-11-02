package com.jason54jg.autominer

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

/**
 * Réglages persistés du mod, pour s'adapter aux serveurs non-vanilla
 * (portée étendue, capacités de casse multiple, etc.).
 */
data class AutoMinerModConfig(
    var configVersion: Int = 2,
    var reach: Double = 4.5,
    var blocksPerTick: Int = 0,
    var lockYLevel: Boolean = false,
    var targetY: Int = -59,
    /** Rayon latéral du tunnel : 0 = 1 colonne (par défaut), 1 = 3 colonnes, 2 = 5 colonnes. */
    var tunnelWidth: Int = 0,
    /** Position du HUD de minage (coin haut-gauche du panneau), en pixels depuis le bord de l'écran. */
    var hudX: Int = 6,
    var hudY: Int = 6,
    /** Identifiant ("namespace:path") de l'outil/du bloc obligatoire choisis, pour survivre à un redémarrage. */
    var toolItemId: String? = null,
    var customBlockId: String? = null,
    /** Décalage latéral d'un bloc si le tunnel est bloqué devant (mode intelligent). */
    var bypassObstacles: Boolean = true,
    /** Autorise de marcher dans une marche d'un bloc vers le bas (trou / sous un surplomb bas). */
    var descendUnderOverhangs: Boolean = true,
    var obstacleNavigationMode: ObstacleNavigationMode = ObstacleNavigationMode.SMART,
    /** Rayon (en blocs) pour détecter plafond / murs latéraux à casser ou contourner. */
    var obstacleDetectionReach: Int = 4
) {
    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private val PATH = FabricLoader.getInstance().configDir.resolve("autominer.json")

        fun load(): AutoMinerModConfig {
            if (!Files.exists(PATH)) {
                return AutoMinerModConfig().also(::save)
            }
            val loaded = runCatching {
                Files.newBufferedReader(PATH).use { GSON.fromJson(it, AutoMinerModConfig::class.java) }
            }.getOrNull() ?: AutoMinerModConfig()
            val resolved = loaded.migrateIfNeeded().normalized()
            if (resolved != loaded) save(resolved)
            return resolved
        }

        private fun AutoMinerModConfig.migrateIfNeeded(): AutoMinerModConfig {
            if (configVersion >= 2) return this
            val defaults = AutoMinerModConfig()
            return copy(
                configVersion = 2,
                bypassObstacles = defaults.bypassObstacles,
                descendUnderOverhangs = defaults.descendUnderOverhangs,
                obstacleNavigationMode = defaults.obstacleNavigationMode,
                obstacleDetectionReach = defaults.obstacleDetectionReach
            )
        }

        private fun AutoMinerModConfig.normalized(): AutoMinerModConfig {
            val defaults = AutoMinerModConfig()
            return copy(
                configVersion = 2,
                reach = if (reach >= 1.0) reach else defaults.reach,
                blocksPerTick = blocksPerTick.coerceAtLeast(0),
                tunnelWidth = tunnelWidth.coerceIn(0, 2),
                obstacleDetectionReach = obstacleDetectionReach.coerceIn(1, 6)
            )
        }

        fun save(config: AutoMinerModConfig) {
            Files.createDirectories(PATH.parent)
            Files.newBufferedWriter(PATH).use { GSON.toJson(config.normalized(), it) }
        }
    }
}

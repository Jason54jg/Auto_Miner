package com.jason54jg.miningmod

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

enum class MiningPattern {
    STRAIGHT,
    ZIGZAG,
    CIRCLE
}

/** Quels minerais afficher à travers les murs. */
enum class XrayPreset {
    NONE,
    VALUABLE,
    ALL
}

/**
 * Réglages persistés du mod, pour s'adapter aux serveurs non-vanilla
 * (portée étendue, capacités de casse multiple, etc.).
 */
data class MiningModConfig(
    var reach: Double = 4.5,
    var blocksPerTick: Int = 1,
    var lockYLevel: Boolean = false,
    var pattern: MiningPattern = MiningPattern.STRAIGHT,
    var xrayPreset: XrayPreset = XrayPreset.NONE
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

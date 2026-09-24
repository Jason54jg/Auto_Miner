package com.jason54jg.miningmod

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MiningMod : ModInitializer {
    const val MOD_ID = "miningmod"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Mining Mod chargé")
    }
}

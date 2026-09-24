package com.jason54jg.autominer

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AutoMinerMod : ModInitializer {
    const val MOD_ID = "autominer"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Auto Miner chargé")
    }
}

package com.jason54jg.miningmod.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

/**
 * Alerte (son + flash d'écran) quand le joueur est téléporté (déplacement anormal en un tick)
 * ou quand son nom apparaît dans un message de chat.
 */
object AlertManager {
    private const val TELEPORT_THRESHOLD = 8.0

    private var lastPosition: Vec3? = null

    fun checkTeleport(player: LocalPlayer) {
        val previous = lastPosition
        val current = player.position()
        lastPosition = current
        if (previous != null && previous.distanceTo(current) >= TELEPORT_THRESHOLD) {
            trigger(Minecraft.getInstance())
        }
    }

    fun checkMention(client: Minecraft, message: Component) {
        val name = client.player?.scoreboardName ?: return
        if (message.string.contains(name, ignoreCase = true)) {
            trigger(client)
        }
    }

    private fun trigger(client: Minecraft) {
        client.player?.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1f)
        ScreenFlash.start()
    }
}

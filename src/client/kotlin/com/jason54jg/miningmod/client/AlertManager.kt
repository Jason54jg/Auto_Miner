package com.jason54jg.miningmod.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3

/**
 * Alerte (son + flash d'écran) quand le joueur est téléporté (déplacement anormal en un tick)
 * ou quand son nom apparaît dans un message de chat.
 */
object AlertManager {
    private const val TELEPORT_THRESHOLD = 8.0

    private var lastPosition: Vec3? = null

    /** À appeler à l'activation du minage : évite de comparer à une position d'un autre monde/dimension. */
    fun resetTeleportTracking() {
        lastPosition = null
    }

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

    /** Le minage vient de s'arrêter tout seul faute de pouvoir avancer (obstacle incassable, coincé...). */
    fun notifyStuck(client: Minecraft) {
        trigger(client)
    }

    private fun trigger(client: Minecraft) {
        client.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f))
        ScreenFlash.start()
    }
}

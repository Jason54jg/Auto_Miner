package com.jason54jg.autominer.client

import com.jason54jg.autominer.AutoMiner
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object AutoMinerModClient : ClientModInitializer {
    private var mouseUngrabbed = false

    override fun onInitializeClient() {
        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("autominer", "main"))
        val toggleKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.autominer.toggle", GLFW.GLFW_KEY_B, category)
        )
        val ungrabKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.autominer.ungrab", GLFW.GLFW_KEY_N, category)
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleKey.consumeClick()) {
                AutoMiner.toggle(client)
                if (AutoMiner.active) AlertManager.resetTeleportTracking()
            }
            while (ungrabKey.consumeClick()) toggleMouseGrab(client)
            AutoMiner.tick(client)
            if (AutoMiner.active) client.player?.let(AlertManager::checkTeleport)
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("autominer").executes {
                    // Le ChatScreen ferme l'écran courant (setScreen(null)) juste après l'envoi de la
                    // commande, dans le même appel : ouvrir l'écran ici serait aussitôt écrasé. On le
                    // reporte à la tâche suivante de la boucle client, une fois le chat déjà fermé.
                    Minecraft.getInstance().execute {
                        Minecraft.getInstance().gui.setScreen(AutoMinerModConfigScreen(null))
                    }
                    1
                }
            )
        }

        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            if (AutoMiner.active) AlertManager.checkMention(Minecraft.getInstance(), message)
        }

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("autominer", "mining_hud")) { graphics, _ ->
            MiningHud.render(graphics)
        }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("autominer", "alert_flash")) { graphics, _ ->
            ScreenFlash.render(graphics)
        }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("autominer", "mouse_ungrab_hud")) { graphics, _ ->
            MouseUngrabHud.render(graphics, mouseUngrabbed, ungrabKey)
        }
    }

    /** Libère/reprend la souris pour éviter que les mouvements de souris ne perturbent le minage automatique. */
    private fun toggleMouseGrab(client: Minecraft) {
        mouseUngrabbed = !mouseUngrabbed
        val messageKey = if (mouseUngrabbed) "chat.autominer.mouse_released" else "chat.autominer.mouse_grabbed"
        val color = if (mouseUngrabbed) ChatFormatting.YELLOW else ChatFormatting.GRAY
        client.player?.sendSystemMessage(Component.translatable(messageKey).withStyle(color))
        if (mouseUngrabbed) {
            client.mouseHandler.releaseMouse()
        } else {
            client.mouseHandler.grabMouse()
        }
    }
}

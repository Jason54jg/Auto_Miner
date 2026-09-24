package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.AutoMiner
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object MiningModClient : ClientModInitializer {
    private var mouseUngrabbed = false

    override fun onInitializeClient() {
        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("miningmod", "main"))
        val toggleKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.miningmod.toggle", GLFW.GLFW_KEY_B, category)
        )
        val ungrabKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.miningmod.ungrab", GLFW.GLFW_KEY_N, category)
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleKey.consumeClick()) AutoMiner.toggle(client)
            while (ungrabKey.consumeClick()) toggleMouseGrab(client)
            AutoMiner.tick(client)
            client.player?.let(AlertManager::checkTeleport)
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("miningbot").executes {
                    Minecraft.getInstance().gui.setScreen(MiningModConfigScreen(null))
                    1
                }
            )
        }

        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            AlertManager.checkMention(Minecraft.getInstance(), message)
        }

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("miningmod", "mining_hud")) { graphics, _ ->
            MiningHud.render(graphics)
        }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("miningmod", "alert_flash")) { graphics, _ ->
            ScreenFlash.render(graphics)
        }
    }

    /** Libère/reprend la souris pour éviter que les mouvements de souris ne perturbent le minage automatique. */
    private fun toggleMouseGrab(client: Minecraft) {
        mouseUngrabbed = !mouseUngrabbed
        if (mouseUngrabbed) {
            client.mouseHandler.releaseMouse()
        } else {
            client.mouseHandler.grabMouse()
        }
    }
}

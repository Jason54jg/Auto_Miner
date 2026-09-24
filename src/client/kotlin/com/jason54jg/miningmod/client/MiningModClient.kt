package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.AutoMiner
import com.jason54jg.miningmod.XrayScanner
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object MiningModClient : ClientModInitializer {
    override fun onInitializeClient() {
        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("miningmod", "main"))
        val toggleKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.miningmod.toggle", GLFW.GLFW_KEY_B, category)
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleKey.consumeClick()) AutoMiner.toggle(client)
            AutoMiner.tick(client)
            client.player?.let(AlertManager::checkTeleport)
            XrayScanner.tick(client, AutoMiner.currentConfig())
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("miningmod")
                    .then(
                        ClientCommandManager.literal("config").executes {
                            Minecraft.getInstance().gui.setScreen(MiningModConfigScreen(null))
                            1
                        }
                    )
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

        XrayRenderer.register()
    }
}

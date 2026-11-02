package com.jason54jg.autominer.client

import com.jason54jg.autominer.AutoMiner
import com.jason54jg.autominer.AutoMinerModConfig
import com.jason54jg.autominer.CustomOreSelection
import com.jason54jg.autominer.ToolSelection
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
//? if >=26.3 {
/*import com.mojang.blaze3d.platform.InputConstants*/
//?} else {
import org.lwjgl.glfw.GLFW
//?}

object AutoMinerModClient : ClientModInitializer {
    private var mouseUngrabbed = false

    override fun onInitializeClient() {
        val config = AutoMinerModConfig.load()
        AutoMiner.applyConfig(config)
        MiningHud.applyConfig(config)
        ToolSelection.applyFromConfig(config)
        CustomOreSelection.applyFromConfig(config)

        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("autominer", "main"))
        val toggleKey = KeyMappingHelper.registerKeyMapping(
            //? if >=26.3 {
            /*KeyMapping("key.autominer.toggle", InputConstants.KEY_B, category)*/
            //?} else {
            KeyMapping("key.autominer.toggle", GLFW.GLFW_KEY_B, category)
            //?}
        )
        val ungrabKey = KeyMappingHelper.registerKeyMapping(
            //? if >=26.3 {
            /*KeyMapping("key.autominer.ungrab", InputConstants.KEY_N, category)*/
            //?} else {
            KeyMapping("key.autominer.ungrab", GLFW.GLFW_KEY_N, category)
            //?}
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
                        //? if <26.2 {
                        /*Minecraft.getInstance().setScreen(AutoMinerModConfigScreen(null))*/
                        //?} else {
                        Minecraft.getInstance().gui.setScreen(AutoMinerModConfigScreen(null))
                        //?}
                    }
                    1
                }
            )
        }

        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            if (AutoMiner.active) AlertManager.checkMention(Minecraft.getInstance(), message)
        }
        // Les annonces serveur (qui contiennent aussi le pseudo) passent par GAME, pas CHAT.
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            if (AutoMiner.active) AlertManager.checkMention(Minecraft.getInstance(), message)
        }

        // Un changement de monde/dimension déplace le joueur sans téléportation : repart d'une
        // position saine au lieu de déclencher une fausse alerte au premier tick actif.
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            AlertManager.resetTeleportTracking()
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

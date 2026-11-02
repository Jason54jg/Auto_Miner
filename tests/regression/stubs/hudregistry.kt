package net.fabricmc.fabric.api.client.rendering.v1.hud
import net.minecraft.resources.Identifier
import net.minecraft.client.gui.GuiGraphicsExtractor
object HudElementRegistry {fun addLast(id:Identifier,callback:(GuiGraphicsExtractor,Any)->Unit){}}

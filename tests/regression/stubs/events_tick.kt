package net.fabricmc.fabric.api.client.event.lifecycle.v1
import audit.Event
import net.minecraft.client.Minecraft
object ClientTickEvents {val END_CLIENT_TICK=Event<(Minecraft)->Unit>()}

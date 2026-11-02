package net.fabricmc.fabric.api.client.networking.v1
import audit.Event
import net.minecraft.client.Minecraft
object ClientPlayConnectionEvents {val JOIN=Event<(Any,Any,Minecraft)->Unit>();val DISCONNECT=Event<(Any,Minecraft)->Unit>()}

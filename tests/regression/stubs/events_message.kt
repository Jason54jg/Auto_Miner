package net.fabricmc.fabric.api.client.message.v1
import audit.Event
import net.minecraft.network.chat.Component
object ClientReceiveMessageEvents {val CHAT=Event<(Component,Any,Any,Any,Any)->Unit>();val GAME=Event<(Component,Boolean)->Unit>()}

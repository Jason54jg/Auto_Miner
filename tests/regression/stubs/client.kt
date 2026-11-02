package net.minecraft.client
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.world.level.Level
class SoundManager {var played=0;fun play(sound:Any){played++}}
class Window {val guiScaledWidth=320; val guiScaledHeight=240}
class Gui {fun setScreen(screen:Any?){}}
class MouseHandler {fun releaseMouse(){};fun grabMouse(){}}
class Minecraft {
 val gui=Gui()
 val mouseHandler=MouseHandler()
 fun execute(r:()->Unit){r()}

 var player:LocalPlayer?=null
 var level:Level?=null
 var gameMode:MultiPlayerGameMode?=null
 val soundManager=SoundManager()
 val window=Window()
 companion object {var current=Minecraft();fun getInstance()=current}
}

package net.fabricmc.fabric.api.client.command.v2
import audit.Event
class Command {fun executes(callback:(Any)->Int)=this}
class Dispatcher {fun register(command:Command){}}
object ClientCommands {fun literal(name:String)=Command()}
object ClientCommandRegistrationCallback {val EVENT=Event<(Dispatcher,Any)->Unit>()}

import com.jason54jg.autominer.*
import com.jason54jg.autominer.client.AlertManager
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.*
import net.minecraft.world.level.*
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.file.Files

var passed=0
var failed=0
fun scenario(name:String,body:()->Unit) {
 try { body(); println("PASS $name"); passed++ }
 catch(e:Throwable) {println("FAIL $name: ${e::class.simpleName}: ${e.message}");failed++}
}
fun expect(value:Boolean,message:String){check(value){message}}
fun fixture(config:AutoMinerModConfig=AutoMinerModConfig()):Minecraft {
 val c=Minecraft()
 c.player=LocalPlayer();c.level=Level();c.gameMode=MultiPlayerGameMode(c.level!!,c.player!!)
 Minecraft.current=c
 if(AutoMiner.active)AutoMiner.toggle(c)
 AutoMiner.reset(c)
 AutoMiner.applyConfig(config)
 ToolSelection.item=null;CustomOreSelection.block=null
 c.level!!.put(BlockPos(0,-1,0),Blocks.STONE)
 return c
}
fun enable(c:Minecraft){AutoMiner.toggle(c)}
fun tick(c:Minecraft,n:Int=1){repeat(n){AutoMiner.tick(c)}}
fun main(){
 val configPath=FabricLoader.getInstance().configDir.resolve("autominer.json")
 Files.createDirectories(configPath.parent)
 scenario("missing config creates defaults") {
  Files.deleteIfExists(configPath)
  expect(AutoMinerModConfig.load().reach==4.5,"unexpected defaults")
 }
 scenario("config saves and reloads selected IDs") {
  AutoMinerModConfig.save(AutoMinerModConfig(reach=5.0,toolItemId="minecraft:diamond_pickaxe",customBlockId="minecraft:diamond_ore"))
  val cfg=AutoMinerModConfig.load()
  expect(cfg.reach==5.0 && cfg.toolItemId=="minecraft:diamond_pickaxe" && cfg.customBlockId=="minecraft:diamond_ore","roundtrip lost fields")
 }
 scenario("malformed JSON falls back to defaults") {
  Files.writeString(configPath,"{broken json")
  expect(AutoMinerModConfig.load().blocksPerTick==1,"missing fallback")
 }
 scenario("JSON null falls back safely") {
  Files.writeString(configPath,"null")
  expect(AutoMinerModConfig.load().reach==4.5,"missing fallback")
 }
 scenario("empty config falls back safely") {
  Files.writeString(configPath,"")
  expect(AutoMinerModConfig.load().reach==4.5,"missing fallback")
 }
 AutoMinerModConfig.save(AutoMinerModConfig())
 scenario("inactive miner leaves world unchanged") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.STONE);tick(c)
  expect(c.gameMode!!.attempts.isEmpty(),"mining while inactive")
 }
 scenario("head block mined before feet") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.STONE);c.level!!.put(BlockPos(0,0,1),Blocks.STONE)
  enable(c);tick(c)
  expect(c.gameMode!!.attempts.first()==BlockPos(0,1,1),"wrong order")
 }
 scenario("instant block budget and counts") {
  val c=fixture(AutoMinerModConfig(blocksPerTick=2));c.level!!.put(BlockPos(0,1,1),Blocks.STONE);c.level!!.put(BlockPos(0,0,1),Blocks.STONE)
  enable(c);tick(c)
  expect(MiningStats.blocksBroken==2,"count=${MiningStats.blocksBroken}")
 }
 scenario("touching ore prioritized over tunnel stone") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.STONE);c.level!!.put(BlockPos(1,1,1),Blocks.DIAMOND_ORE)
  enable(c);tick(c)
  expect(c.gameMode!!.attempts.first()==BlockPos(1,1,1),"ore not first")
 }
 scenario("selected tool equipped from hotbar") {
  val c=fixture();ToolSelection.item=Items.PICKAXE;c.player!!.inventory.slots[4]=ItemStack(Items.PICKAXE)
  enable(c);tick(c);expect(c.player!!.inventory.selectedSlot==4,"tool not equipped")
 }
 scenario("bedrock wall disables mining after 20 ticks") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.BEDROCK)
  enable(c);tick(c,20);expect(!AutoMiner.active,"remained active")
  expect(c.soundManager.played==1,"no alert")
 }
 scenario("custom ore directly under player is preserved") {
  val c=fixture();CustomOreSelection.block=Blocks.DIAMOND_ORE;c.level!!.put(BlockPos(0,-1,0),Blocks.DIAMOND_ORE)
  enable(c);tick(c);expect(c.level!!.getBlockState(BlockPos(0,-1,0)).block==Blocks.DIAMOND_ORE,"floor destroyed")
 }
 scenario("two block gap halts movement when Y is locked") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0))
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"walked into gap")
 }
 scenario("single block gap triggers jump") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,2),Blocks.STONE)
  enable(c);tick(c);expect(c.player!!.deltaMovement.y==0.42,"did not jump")
 }
 scenario("solid forward floor permits walking") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,1),Blocks.STONE)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.2,"did not move")
 }
 scenario("normal descent mines block beneath player") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=-5))
  enable(c);tick(c);expect(c.gameMode!!.attempts.first()==BlockPos(0,-1,0),"did not descend")
 }
 scenario("blocked descent stops instead of mining horizontally") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=-1));c.level!!.put(BlockPos(0,-1,0),Blocks.BEDROCK);c.level!!.put(BlockPos(0,-1,1),Blocks.STONE)
  enable(c);tick(c,25)
  expect(c.gameMode!!.attempts.isEmpty() && !AutoMiner.active,"horizontal mining attempts=${c.gameMode!!.attempts.size}; active=${AutoMiner.active}")
 }
 scenario("out of reach ore does not permanently block reachable tunnel") {
  val c=fixture();c.gameMode!!.enforceReach=true
  CustomOreSelection.block=Blocks.DIAMOND_ORE
  c.level!!.put(BlockPos(0,1,1),Blocks.STONE);c.level!!.put(BlockPos(4,4,4),Blocks.DIAMOND_ORE)
  enable(c);tick(c,60)
  expect(c.level!!.getBlockState(BlockPos(0,1,1)).isAir || !AutoMiner.active,"60 ticks retrying ${c.gameMode!!.attempts.distinct()}; no movement or stop")
 }
 scenario("liquid floor is not accepted as safe ground") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,1),Blocks.LAVA);c.level!!.put(BlockPos(0,-1,2),Blocks.LAVA)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"forward movement=${c.player!!.deltaMovement.z} above lava")
 }
 scenario("waterlogged block replaced by water is counted as broken") {
  val c=fixture();val ore=Block("test:waterlogged_ore",ore=true);CustomOreSelection.block=ore;c.level!!.put(BlockPos(0,1,1),ore);c.gameMode!!.replaceWith=Blocks.WATER
  enable(c);tick(c);expect(MiningStats.blocksBroken==1,"count=${MiningStats.blocksBroken} after replacement by water")
 }
 scenario("deepslate and normal ores share one stats entry") {
  MiningStats.startSession();MiningStats.recordBreak(BlockState(Blocks.DIAMOND_ORE),true);MiningStats.recordBreak(BlockState(Blocks.DEEPSLATE_DIAMOND_ORE),true)
  expect(MiningStats.ores.size==1 && MiningStats.ores.values.single()==2,"not merged")
 }
 scenario("teleport threshold triggers one sound") {
  val c=fixture();AlertManager.resetTeleportTracking();AlertManager.checkTeleport(c.player!!);c.player!!.pos=BlockPos(0,0,9);AlertManager.checkTeleport(c.player!!)
  expect(c.soundManager.played==1,"teleport alert not triggered")
 }
 scenario("case insensitive mention triggers sound") {
  val c=fixture();AlertManager.checkMention(c,Component.literal("Hey PLAYER"));expect(c.soundManager.played==1,"mention missed")
 }
 scenario("unrelated chat does not trigger sound") {
  val c=fixture();AlertManager.checkMention(c,Component.literal("hello everyone"));expect(c.soundManager.played==0,"false positive")
 }
 com.jason54jg.autominer.client.AutoMinerModClient.onInitializeClient()
 scenario("disconnect disables active miner") {
  val c=fixture();enable(c)
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.callbacks.forEach {it(Any(),c)}
  c.player=null;c.level=null;c.gameMode=null
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.callbacks.forEach {it(c)}
  expect(!AutoMiner.active,"active flag survives disconnection")
 }
 scenario("new server does not resume mining without user activation") {
  val c=fixture();enable(c)
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.callbacks.forEach {it(Any(),c)}
  c.player=LocalPlayer();c.level=Level();c.gameMode=MultiPlayerGameMode(c.level!!,c.player!!)
  c.level!!.put(BlockPos(0,1,1),Blocks.STONE)
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.callbacks.forEach {it(Any(),Any(),c)}
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.callbacks.forEach {it(c)}
  expect(c.gameMode!!.attempts.isEmpty(),"mined on another server without a new key press")
 }
 scenario("server refused block stops instead of retrying forever") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.STONE);c.gameMode!!.allowMining=false
  enable(c);tick(c,60)
  expect(!AutoMiner.active && c.soundManager.played==1,"refused target remained active")
 }
 scenario("slow legitimate mining is not stopped after one second") {
  val c=fixture();val slow=Block("test:slow",progress=0.001f);c.level!!.put(BlockPos(0,1,1),slow);c.gameMode!!.instant=false
  enable(c);tick(c,300)
  expect(AutoMiner.active,"slow mining stopped prematurely")
  tick(c,710);expect(MiningStats.blocksBroken==1,"slow block never completed")
 }
 scenario("delayed water replacement is counted once") {
  val c=fixture();val ore=Block("test:waterlogged",ore=true,progress=0.1f,fluid=true);c.level!!.put(BlockPos(0,1,1),ore);c.gameMode!!.instant=false
  enable(c);tick(c)
  expect(MiningStats.blocksBroken==0,"block was already destroyed before deferred update")
  c.level!!.put(BlockPos(0,1,1),Blocks.WATER)
  tick(c,2);expect(MiningStats.blocksBroken==1,"delayed replacement lost or double counted")
 }
 scenario("configured reach cannot exceed actual player reach") {
  val c=fixture(AutoMinerModConfig(reach=6.0));c.player!!.interactionRange=2.0
  CustomOreSelection.block=Blocks.DIAMOND_ORE;c.level!!.put(BlockPos(0,1,4),Blocks.DIAMOND_ORE);c.level!!.put(BlockPos(0,1,1),Blocks.STONE)
  enable(c);tick(c)
  expect(c.gameMode!!.attempts==listOf(BlockPos(0,1,1)),"attempted an unreachable target")
 }
 scenario("extended player reach still permits nearby custom ores") {
  val c=fixture(AutoMinerModConfig(reach=6.0));c.player!!.interactionRange=6.0
  CustomOreSelection.block=Blocks.DIAMOND_ORE;c.level!!.put(BlockPos(0,1,5),Blocks.DIAMOND_ORE)
  enable(c);tick(c);expect(MiningStats.blocksBroken==1,"extended reach ignored")
 }
 scenario("liquid blocks downward descent without lateral mining") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=-1));c.level!!.put(BlockPos(0,-1,0),Blocks.WATER);c.level!!.put(BlockPos(0,-1,1),Blocks.STONE)
  enable(c);tick(c)
  expect(!AutoMiner.active && c.gameMode!!.attempts.isEmpty(),"liquid descent continued horizontally")
 }
 scenario("blocked downward mining also times out") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=-1));c.gameMode!!.allowMining=false
  enable(c);tick(c,60);expect(!AutoMiner.active,"downward mining never timed out")
 }
 scenario("unsafe ground removes existing horizontal momentum") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,1),Blocks.LAVA);c.level!!.put(BlockPos(0,-1,2),Blocks.LAVA)
  enable(c);c.player!!.deltaMovement=net.minecraft.world.phys.Vec3(0.0,0.0,0.2)
  tick(c);expect(c.player!!.deltaMovement.z==0.0,"retained forward momentum")
 }
 scenario("noncolliding floor is unsafe even when it is not air") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));val plant=Block("test:plant",sturdy=false)
  c.level!!.put(BlockPos(0,-1,1),plant);c.level!!.put(BlockPos(0,-1,2),plant)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"walked onto noncolliding floor")
 }
 scenario("jump requires unobstructed landing space") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,2),Blocks.STONE);c.level!!.put(BlockPos(0,1,2),Blocks.BEDROCK)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"jumped toward blocked landing")
 }
 scenario("dimension change stops mining without waiting for network disconnect") {
  val c=fixture();enable(c);c.level=Level();c.gameMode=MultiPlayerGameMode(c.level!!,c.player!!)
  c.level!!.put(BlockPos(0,1,1),Blocks.STONE)
  tick(c);expect(!AutoMiner.active && c.gameMode!!.attempts.isEmpty(),"mining resumed across dimensions")
 }
 scenario("menu key presses cannot activate mining") {
  val c=fixture();c.player=null;c.level=null;c.gameMode=null
  AutoMiner.toggle(c);expect(!AutoMiner.active,"activated without a world")
 }
 scenario("jump keeps horizontal momentum while above the locked Y level") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-1,2),Blocks.STONE)
  enable(c);tick(c)
  expect(c.player!!.deltaMovement.y==0.42,"did not start the jump")
  c.player!!.grounded=false;c.player!!.pos=BlockPos(0,1,0)
  tick(c)
  expect(c.player!!.deltaMovement.z==0.2,"lost forward jump momentum while waiting to land")
 }
 scenario("smart navigation still mines nearby overhanging blocks") {
  val c=fixture();c.level!!.put(BlockPos(0,2,1),Blocks.STONE)
  enable(c);tick(c)
  expect(c.gameMode!!.attempts==listOf(BlockPos(0,2,1)),"smart ceiling detection lost during merge")
 }
 scenario("forward only mode does not mine nearby ceilings") {
  val c=fixture(AutoMinerModConfig(obstacleNavigationMode=ObstacleNavigationMode.FORWARD_ONLY));c.level!!.put(BlockPos(0,2,1),Blocks.STONE)
  enable(c);tick(c);expect(c.gameMode!!.attempts.isEmpty(),"legacy mode mined an overhead block")
 }
 scenario("smart navigation retains sidestepping around an unbreakable wall") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.BEDROCK)
  c.level!!.put(BlockPos(1,-1,0),Blocks.STONE);c.level!!.put(BlockPos(-1,-1,0),Blocks.STONE)
  enable(c);tick(c,5)
  expect(AutoMiner.active && kotlin.math.abs(c.player!!.deltaMovement.x)==0.2,"sidestep functionality was lost")
 }
 scenario("smart sidestepping rejects liquid floors") {
  val c=fixture();c.level!!.put(BlockPos(0,1,1),Blocks.BEDROCK)
  c.level!!.put(BlockPos(1,-1,0),Blocks.LAVA);c.level!!.put(BlockPos(-1,-1,0),Blocks.LAVA)
  enable(c);tick(c,5)
  expect(c.player!!.deltaMovement.x==0.0,"sidestepped over lava")
 }
 scenario("smart navigation permits a single step down onto solid support") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-2,1),Blocks.STONE)
  enable(c);tick(c)
  expect(c.player!!.deltaMovement.z==0.2 && c.player!!.deltaMovement.y==0.0,"single safe step down was lost")
 }
 scenario("smart step down rejects liquid lower support") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0));c.level!!.put(BlockPos(0,-2,1),Blocks.LAVA)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"stepped down onto lava")
 }
 scenario("legacy mode does not enable step down navigation") {
  val c=fixture(AutoMinerModConfig(lockYLevel=true,targetY=0,obstacleNavigationMode=ObstacleNavigationMode.FORWARD_ONLY));c.level!!.put(BlockPos(0,-2,1),Blocks.STONE)
  enable(c);tick(c);expect(c.player!!.deltaMovement.z==0.0,"legacy mode stepped down")
 }
 scenario("master configuration migration and normalization are retained") {
  Files.writeString(configPath,"""{"configVersion":1,"reach":3.5,"blocksPerTick":-2,"tunnelWidth":8}""")
  val loaded=AutoMinerModConfig.load()
  expect(loaded.configVersion==2 && loaded.reach==3.5 && loaded.blocksPerTick==1 && loaded.tunnelWidth==2,"normalization or existing settings lost")
  expect(loaded.obstacleNavigationMode==ObstacleNavigationMode.SMART && loaded.bypassObstacles,"migration defaults lost")
 }
 println("RESULT passed=$passed failed=$failed total=${passed+failed}")
 if(failed>0) kotlin.system.exitProcess(1)
}

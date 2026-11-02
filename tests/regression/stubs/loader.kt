package net.fabricmc.loader.api
import java.nio.file.Path
class FabricLoader {
 val configDir:Path=Path.of(System.getProperty("autominer.test.configDir"))
 companion object {private val loader=FabricLoader();fun getInstance()=loader}
}

package net.fabricmc.fabric.api.client.keymapping.v1
import net.minecraft.client.KeyMapping
object KeyMappingHelper {val keys=mutableListOf<KeyMapping>();fun registerKeyMapping(key:KeyMapping):KeyMapping {keys.add(key);return key}}

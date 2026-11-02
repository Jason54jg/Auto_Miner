# Changelog

*English | [Français](CHANGELOG.fr.md)*

Each new request bumps the version by `0.0.1`.

## 0.0.49
- Fix GitHub, Modrinth and Pages links after renaming the repository to `Auto_Miner`, including homepage metadata in all three JARs.
- Use the address reported by GitHub Pages when generating site metadata and check the actual published content. Use GitHub Actions publishing so a legacy root Jekyll build cannot overwrite the site.

## 0.0.48
- **Config screen layout**: dynamic spacing for the tool/mandatory-block labels so wrapped text no longer overlaps; subtitle stays in the main panel with word wrap instead of crossing the sidebar divider.
- **Tab buttons**: wider sidebar, readable labels (selected tab no longer uses the disabled Minecraft style); tab label shortened to `Tun.` where needed.
- **Modern flat buttons** on the config screen (tabs, toggles, Save/Cancel, item slots) instead of vanilla stone textures — primary/secondary/tab/slot styles with hover and selection states.
- **Blocks/tick** default is now `0` (saved/normalized floor); `0` still means one mining action per tick (vanilla pace), higher values enable multi-break per tick.

## 0.0.47
- New visual identity: cyan/copper pickaxe logo and banner, icon metadata in all three JARs and a logo in the configuration screen. Draw the panel before its widgets so it does not cover them.
- Bilingual responsive information site with no build dependencies, deployed from master through GitHub Pages Actions. Versions and dependencies come from Gradle.
- Rewritten English/French Modrinth descriptions and a manual workflow to sync the description, links and icon using a Modrinth secret.

## 0.0.46
- Integrate fixes with the tabbed interface and navigation from 0.0.45; preserve ore priority in smart mode, check safe support for sidesteps and downward steps, and fix incompatible configuration screen rendering calls.
- Fix all six audit findings: filter targets by actual interaction reach and stop stalled mining, stop on disconnect/world changes, prevent horizontal mining when Y descent is blocked, check solid support and fluids before walking or jumping, fall back to defaults for empty/`null` configuration files, and count destroyed blocks that leave water, including deferred updates.
- Add regression scenarios runnable with `bash tests/regression/run.sh`; these supplement the three version builds and do not replace testing in Minecraft.

## 0.0.45
- Audit / consistency: single client config bootstrap (AutoMiner, HUD, tool/block slots), value normalization on load/save, sidestep floor height with Y lock, Obstacles toggles disabled in forward-only mode, README aligned with actual persistence.

## 0.0.44
- **Redesigned config screen**: tabbed panel (General, Tunnel, Obstacles, Display), tool/mandatory-block column, Save / Cancel buttons.
- **Smart navigation** (Obstacles tab, enabled by default on migrated configs): breaks nearby ceilings/walls, steps down one block under overhangs, sidesteps when an AOE pick leaves the tunnel blocked. "Forward only" mode restores legacy behavior.

## 0.0.43
- Multi-version support via Stonecutter: separate jars for Minecraft **26.1.x** (26.1, 26.1.1, 26.1.2), **26.2** and **26.3** (`autominer-<version>+<mc>.jar` — see README for the matching Fabric API / Mod Menu versions). The 26.1 code path keeps the pre-26.2 APIs (`KeyBinding`, `ClientCommandManager`, `GuiGraphics`, `displayClientMessage`...).
- Fix: mention alerts only listened to player chat (`CHAT`) — server announcements (`GAME`), which can also contain the player's name, now trigger the alert too.
- Fix: joining another world/dimension while mining no longer triggers a false teleport alert (position tracking resets on join).

## 0.0.42
- Fix: the on-screen mouse-release reminder hardcoded "N" as the key name, which became wrong if the player rebinds `key.autominer.ungrab` in the controls options. Now shows the key's actual current binding (`KeyMapping.translatedKeyMessage`).

## 0.0.41
- Fix: the keybind category showed as raw untranslated text (`key.category.autominer.main`) in Options > Controls. The new `KeyMapping.Category` system on 26.2 generates keys as `key.category.<namespace>.<path>` (singular), not the old `key.categories.<name>` (plural) — lang files updated to match.
- Chat message when releasing/grabbing the mouse back (`N`), same as the mining toggle.
- Persistent on-screen reminder while the mouse stays released — easy to forget it's still on, unlike mining which already has its own HUD.

## 0.0.40
- In-game UI actually localized (not just the docs from 0.0.39): the config screen, HUD and chat activation messages were hardcoded French strings, ignoring the client's game language entirely. They now use Minecraft's translation system (`Component.translatable`, `en_us.json`/`fr_fr.json`), matching what the two keybind names already did. Tool/mandatory-block names now show Minecraft's own localized item/block name instead of a raw `snake_case` registry path.

## 0.0.39
- Bilingual documentation: `README.md`/`CHANGELOG.md` in English (base language), `README.fr.md`/`CHANGELOG.fr.md` in French.

## 0.0.38
- **Full rename**: `Mining Mod` → `Auto Miner`. Technical id `miningmod` → `autominer`, package `com.jason54jg.miningmod` → `com.jason54jg.autominer`, command `/miningbot` → `/autominer`, config file `miningmod.json` → `autominer.json`, translation keys `key.miningmod.*` → `key.autominer.*`. Old settings (`config/miningmod.json`) are not carried over automatically — fresh default config on first launch.

## 0.0.37
- Fix: the mod broke the block at feet level before the one at head height, even though the crosshair (camera held level) points at the head — gave the impression of breaking "below" instead of "in front". The head is now broken first.

## 0.0.36
- Colored activation/deactivation chat message (green/red) instead of white.
- Movable mining HUD: `HUD position X`/`HUD position Y` in `/miningbot` (position in pixels, top-left corner of the panel, saved in the config).

## 0.0.35
- While flying (creative/spectator), the bot no longer waits for a floor underfoot to advance: hole avoidance (reserved for `Lock Y level`) is ignored while flying, since a flying player doesn't fall.

## 0.0.34
- Removed the Zigzag and Circle styles: the mod now only mines in a straight line (direction locked at activation, as before). The `pattern` field disappears from the config, as does the "Style" button on the `/miningbot` screen.

## 0.0.33
- Compile fix: `Player.displayClientMessage(Component, Boolean)` was removed in 26.2, split into `sendSystemMessage(Component)` (chat) and `sendOverlayMessage(Component)` (action bar). The activation/deactivation message now uses `sendSystemMessage`.

## 0.0.32
- Chat message on every press of the activation key ("Auto-mining enabled"/"disabled") — also shown when mining shuts itself off on an obstacle (0.0.30).

## 0.0.31
- Hole avoidance (jump or stop) now only applies if `Lock Y level` is active. Without the lock, there's no layer to maintain: the bot ignores holes and falls into them while continuing to advance, while still breaking the block in front of it normally if there is one.

## 0.0.30
- Fix: facing a real obstacle (unbreakable block, liquid with no ore around, hole too wide with no ore), the mod stayed active but did nothing at all, silently, indefinitely. After 20 ticks (~1s) without breaking or advancing, mining now shuts off automatically and alerts the player (sound + flash), like for a teleport/mention.

## 0.0.29
- Important fix: the mod could break the block holding the player up (ore/mandatory block detected right underfoot), making them fall into the void/a cave — especially from the mandatory block's wide scan. This block is now systematically excluded from targets.
- Camera fix: the head no longer followed the locked direction during a Y descent (or a fall) — `syncViewToFacing` now runs every tick, not only once the descent is finished.

## 0.0.28
- The wide scan (cube around the player, all directions) is now reserved for the **mandatory block**. Vanilla/tag ores go back to the old "touches the tunnel" behavior — otherwise an ore crossed at a distance in a wall would randomly punch a hole through the tunnel. No scan at all if no mandatory block is chosen (avoids the cube's cost for nothing).

## 0.0.27
- Full autonomy: the mining/movement direction (all styles, including "Straight") is now locked at activation and never read from the live look direction again. The camera follows automatically (fixed in a straight line, clamped to ±30° in zigzag/circle): no player movement or aiming is needed anymore once mining starts.
- Priority block detection (ores + mandatory block): moves from a scan limited to blocks touching the tunnel ahead, to a cube scan around the player (radius = `Reach`), in every direction — an ore is broken first as soon as it's detected in range, even to the side, above, below or behind.

## 0.0.26
- Compile fix: `BuiltInRegistries.BLOCK.get(Identifier)` / `.ITEM.get(Identifier)` now return `Optional<Holder.Reference<T>>?` instead of `T?` directly. `ToolSelection.resolve`/`CustomOreSelection.resolve` unwrap via `.orElse(null)?.value()`.

## 0.0.25
- Possible fix: `isBreakable` excluded any block reporting a non-empty `fluidState`, which also excludes a solid **waterlogged** block (a submerged custom ore, for example), not just water/lava themselves. Now uses `state.liquid()` (true only for a fluid block) so a mandatory block/ore is no longer blocked just because it's underwater.

## 0.0.24
- A single-block-wide hole ahead: the bot jumps over it instead of stopping dead. A wider hole (no floor at the landing spot either) stays blocking as before (waits/mines rather than falling).

## 0.0.23
- Fix `/miningbot`: the command never opened the config screen (no error anywhere). Cause: `ChatScreen` closes the current screen (`setScreen(null)`) right after sending the command, in the same call — that immediately overwrote the screen just opened from `executes`. Opening it is now deferred to the next client task (`Minecraft.execute { ... }`), once the chat is already closed.

## 0.0.22
- Camera in zigzag/circle: never turns more than 30° from the starting direction anymore, even when the tunnel takes a 90° turn (movement/mining keeps its right angles on the block grid, but the view stays within the player's original field of view). Known limitation: in circle mode, once the spiral is well underway, the actual direction can drift away from the displayed view (capped at ±30°) — no perfect camera tracking through a full turn.
- Fix: the bot no longer walks into thin air (hole, ravine, open-pit mine) — checks there's a floor before advancing, otherwise waits/mines instead of falling.

## 0.0.21
- The tool and mandatory block now survive a game restart (`toolItemId`/`customBlockId` in `config/miningmod.json`, resolved on load). Fix along the way: "Done" was overwriting the selection just made in the slots with the old config loaded when the screen opened — saving now includes the slots' current selection.

## 0.0.20
- "Mandatory block" slot: now captures the **targeted** block (looked at just before opening the config) rather than the held item. Aim at the desired block, then open `/miningbot` and click the slot.

## 0.0.19
- Fix teleport/mention alerts: they ran constantly, even without mining active. A world/dimension change triggered a false "teleport" alert (the previous position, in another world, has nothing to do with the new one). Alerts now only trigger while mining is active, and position tracking resets on every activation.

## 0.0.18
- Config: `Reach`, `Blocks/tick`, `Y level` and `Tunnel width` are now free-text fields (any value), instead of buttons that only cycled through a few presets.
- New: "Mandatory block" slot in the config — click it while holding the block to designate a custom block (in addition to vanilla ores) that must imperatively be broken if it touches the tunnel. Useful on a modded server with its own resource blocks. Not persisted (like the tool slot).
- New `key.miningmod.ungrab` key (`N` by default): releases/regrabs the mouse, to prevent an unintentional mouse movement from turning the camera (and therefore the mining direction) while the bot is working.
- Reworked zigzag: long straight lines (12 blocks) interspersed with short lateral jogs (2 blocks) instead of symmetrically alternating 6 blocks straight / 6 blocks at 90°. Much smoother movement, without ever going fully left or right.
- Reworked circle: spiral arms are longer from the start and grow more slowly, for a spiral that opens up progressively instead of turning very tight at the beginning.

## 0.0.17
- `Lock Y level` now has a real associated value: `Y level` in the config (presets: -59, -54, -32, 0, 11, 15, 63), instead of just freezing the height the player was at when activating.
- Automatic descent to the chosen layer before digging horizontally: breaks the block underfoot, waits to land, repeats — until reaching the configured layer. Stops dead on an unbreakable block or a liquid. **This is not real pathfinding** (no obstacle avoidance), just a controlled vertical descent.
- `Tunnel width` in the config (1/3/5 blocks): widens the mined area sideways, useful with a tool that has its own area effect (e.g. a server-side "Excavation" enchantment) or simply to dig wider.
- Internal refactor: the logic for breaking a block (start/continue/confirm) is now shared between the horizontal tunnel and the vertical descent (`AutoMiner.breakBlock`).

## 0.0.16
- Mining fix: mining only relied on the return value of `startDestroyBlock`/`continueDestroyBlock` to know whether a block was broken (HUD counter incremented, arm animation, but nothing actually broke). Now checks the real block state (`isAir`) after each call, regardless of the exact semantics of the return value.
- Command: `/miningmod config` → `/miningbot` (direct command, no more `config` subcommand).
- Zigzag/circle: the player's head and body now turn to follow the direction driven by the mod (before, only movement and mining turned, not the camera).

## 0.0.15
- **X-ray fully removed** (files, config, screen, calls): `XrayRenderer.kt`, `XrayScanner.kt` deleted; `XrayPreset` and the `xrayPreset` field removed from the config; button and label removed from the config screen; `OreBlocks.VALUABLE` removed (was only used by x-ray, `OreBlocks.ALL` is still used by auto-mining).

## 0.0.14
- **X-ray abandoned.** Building a custom `RenderPipeline` on 26.2 requires writing your own shaders (`.withVertexShader`/`.withFragmentShader`) and declaring a `BindGroupLayout` (projection matrices, etc.) — no more ready-made snippet for simple position+color. Too deep to be reliable without a compiler on hand. `XrayRenderer.kt` is now a stub that draws nothing.
- `XrayScanner` (ore scanning in the config) and the "See through walls" setting stay in place, inert, ready for a real implementation in the future if someone has a concrete, working 26.2 pipeline reference.

## 0.0.13
- Build fix (x-ray): `RenderType` moved from `net.minecraft.client.renderer` to `net.minecraft.client.renderer.rendertype` on 26.2. Confirmed by cross-checking (NeoForge primer). Very likely other issues remain in `XrayRenderer.kt`: the `RenderType` rework goes well beyond a simple package move (`RenderType#draw` replaced by `PreparedRenderType#drawFromBuffer`, `RenderType#create` not yet verified for 26.2).

## 0.0.12
- Build fix: Fabric API module renamed `fabric-key-binding-api-v1` → `fabric-key-mapping-api-v1` on 26.2 (follows the `KeyBinding`→`KeyMapping` rename on the Minecraft side). `KeyBindingHelper.registerKeyBinding` → `net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping`. Confirmed by looking at Fabric API's source on the 26.2 branch.

## 0.0.11
- Build fix: `ClientCommandManager` was removed from Fabric API on 26.2, replaced by `ClientCommands` (same `literal(...)` method). Confirmed by looking directly at Fabric API's source on the 26.2 branch (the `ClientCommandManager.java` file no longer exists).

## 0.0.10
- Build fix: `Inventory.selectedSlot` is a property (real getter `getSelectedSlot()`), not a "bare" method like `mainCamera()`/`gameRenderState()`. Removed the extra parentheses (`inventory.selectedSlot()` → `inventory.selectedSlot`). The `setSelectedSlot(Int)` setter is still called explicitly.

## 0.0.9
Fixes following a real local build (Kotlin `compileClientKotlin`) — Minecraft 26.2 renamed/removed a lot more than expected:
- `Identifier.of(...)` → `Identifier.fromNamespaceAndPath(...)` (the real name, confirmed).
- Key categories are no longer plain strings: `KeyMapping.Category.register(Identifier)` must be registered, then passed to the `KeyMapping` constructor.
- `Inventory.selected` (public field) becomes `selectedSlot()` / `setSelectedSlot(int)` (methods).
- `Player.playNotifySound(...)` no longer exists → sound played via `Minecraft.soundManager.play(SimpleSoundInstance.forUI(...))`.
- `GuiGraphicsExtractor.renderItem(...)` → `item(...)` (loses the `render`/`draw` prefix, like `text`/`centeredText`).
- **X-ray (`XrayRenderer.kt`) rewritten a second time**: `Tesselator` is purely and simply removed in 26.2 (no more immediate-mode rendering). Replaced by the new "submit" system: `LevelRenderEvents.COLLECT_SUBMITS` + `OrderedSubmitNodeCollector#submitShapeOutline(...)`, where the engine itself draws the shape (`Shapes.block()`) instead of building vertices by hand. Still the most fragile part of the mod — the `submitNodeCollector() as? OrderedSubmitNodeCollector` cast isn't guaranteed, the `RenderPipeline.builder(...)` chain isn't 100% verified.

## 0.0.8
- Build fix: `AutoMiner.kt` and `XrayScanner.kt` moved from `src/main/kotlin` to `src/client/kotlin`. They use client-only classes (`Minecraft`, `LocalPlayer`, `MultiPlayerGameMode`) that don't exist on the `main` source set's classpath with `splitEnvironmentSourceSets()` — confirmed by a real local compile error (`Unresolved reference`). Package unchanged (`com.jason54jg.miningmod`), so no imports to change elsewhere.

## 0.0.7
- Build fix: added TerraformersMC's Maven repository (`https://maven.terraformersmc.com/releases/`) to `build.gradle`. Mod Menu is neither on Fabric's Maven nor on Maven Central, `compileOnly "com.terraformersmc:modmenu:..."` couldn't resolve without this repository. Confirmed by a real local build (everything else — Loom, Fabric API, Fabric Language Kotlin, Java 25 — already resolved fine).

## 0.0.6
- Ore x-ray: red outlines visible through walls, within a 16-block radius, rescanned every second. List of relevant ores configurable in the config (`See through walls`): off / valuable (diamond, emerald, ancient debris) / all.
- Refactor: ore sets/detection shared between auto-mining and x-ray (`OreBlocks.kt`), instead of being duplicated.
- **Experimental**: the 3D rendering (`XrayRenderer.kt`) uses Minecraft 26.2's low-level rendering API (RenderPipeline/RenderType), entirely rewritten in this version and impossible to compile in the dev sandbox to verify. This is the file most likely to need a manual fix.

## 0.0.5
- Config screen: tool "slot" (clickable box with icon) — click it while holding a tool to designate it as the mining tool; the mod automatically equips it from the hotbar while mining. Not persisted between game restarts (in-memory choice only).
- Modern HUD while mining (top-left corner): elapsed time, number of blocks broken, breakdown by ore type (deepslate variants merged with the base ore).

## 0.0.4
- New `pattern` option (config + screen): mining style.
  - `STRAIGHT` (default): straight ahead, as before.
  - `ZIGZAG`: alternates straight / right / straight / left every 6 blocks advanced (sawtooth tunnel, "left to right").
  - `CIRCLE`: turns right after each arm, with increasingly long arms (spiral).
- In zigzag/circle, the mining direction is driven by the mod (independent of the look direction); in `STRAIGHT`, it still follows the look direction as before.

## 0.0.3
- Teleport detection: alerts if the player moves abnormally in one tick (≥ 8 blocks).
- Mention detection: alerts if the player's name appears in a chat message.
- Alert = sound (`experience_orb.pickup`) + flashing screen (semi-transparent red background) for 3 seconds.
- Internal fix: Minecraft 26.2 moved `Minecraft#setScreen` to `Minecraft#gui#setScreen` (Gui/Hud rework) — updated in the config command and the config screen.

## 0.0.2
- Persisted configuration (`config/miningmod.json`): `reach` (tunnel look-ahead/anticipation), `blocksPerTick` (blocks breakable in a single tick, for non-vanilla servers with multi-instant-break abilities) and `lockYLevel` (stays at the Y level where mining was activated, for layer strip mining).
- `/miningmod config` command to open the config screen.
- Mod Menu integration (optional, no hard dependency): config button in the mod list.

## 0.0.1
- Initial mod creation.
- Key (`B` by default) to toggle straight-line automatic mining: the player advances and breaks the blocks in front of them (feet + head).
- Ore exception: any ore block touching the tunnel (vanilla + `c:ores` tag) is broken before continuing.

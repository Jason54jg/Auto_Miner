![Auto Miner — One key. Keep mining.](https://raw.githubusercontent.com/Jason54jg/MiningMod/master/site/assets/banner.png)

**Point forward. Press B. Keep mining.**

Auto Miner is a **client-side Fabric mod** that digs a tunnel and moves you forward, giving nearby ores priority. It follows normal survival breaking mechanics: your tool, block hardness, drops and XP still matter.

[Installation & compatibility](https://jason54jg.github.io/MiningMod/#install) · [Guide en français](https://jason54jg.github.io/MiningMod/fr/) · [Source & issues](https://github.com/Jason54jg/MiningMod)

## Less repetition. More control.

- **Ore priority.** Break ores touching the tunnel before moving on. Choose an additional mandatory block to look for around you, within interaction reach.
- **Your tunnel.** Adjust width, reach and target Y. Choose forward-only or smart navigation for nearby obstacles, safe sidesteps and single downward steps.
- **Your tool.** Select a tool and the mod equips it from your hotbar when available.
- **Your session.** A movable HUD tracks elapsed time, blocks broken and ore counts. Sound and screen alerts flag mentions, large position changes and stalled mining.
- **Your settings.** A tabbed screen with Save / Cancel, persistent settings and English/French text.

## Get started

1. Use **Java 25** and **Fabric Loader 0.19.5+** for Minecraft **26.1–26.3**.
2. Download the Auto Miner JAR matching your Minecraft version. Add **[Fabric API](https://modrinth.com/mod/fabric-api)** and **[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)** for the same version. [Mod Menu](https://modrinth.com/mod/modmenu) is optional.
3. Put the files in your instance's `mods` folder and launch the game.

| Control | Action |
| --- | --- |
| **B** | Start / stop mining |
| **N** | Release / grab the mouse |
| **`/autominer`** | Open configuration |

B and N can be rebound in Minecraft's controls. The mod is installed on the **client**, with no server-side Auto Miner component required.

## Before a long run

Auto Miner checks interaction reach, stops stalled mining and ends the session on disconnect or world change. Y lock adds ground checks before movement. Smart navigation handles **nearby obstacles**, not general pathfinding; it cannot guarantee safety in every situation or grant server permissions.

On a server, **check that its rules allow mining automation** before using the mod.

[Read the full guide](https://jason54jg.github.io/MiningMod/) · [Report a problem](https://github.com/Jason54jg/MiningMod/issues)

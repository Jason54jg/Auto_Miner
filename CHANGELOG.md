# Changelog

Chaque nouvelle demande incrémente la version de `0.0.1`.

## 0.0.15
- **X-ray entièrement supprimé** (fichiers, config, écran, appels) : `XrayRenderer.kt`, `XrayScanner.kt` supprimés ; `XrayPreset` et le champ `xrayPreset` retirés de la config ; bouton et libellé retirés de l'écran de configuration ; `OreBlocks.VALUABLE` retiré (n'était utilisé que par le x-ray, `OreBlocks.ALL` reste utilisé par le minage auto).

## 0.0.14
- **X-ray abandonné.** Construire un `RenderPipeline` personnalisé en 26.2 demande d'écrire ses propres shaders (`.withVertexShader`/`.withFragmentShader`) et de déclarer des `BindGroupLayout` (matrices de projection, etc.) — plus aucun snippet tout fait pour du simple position+couleur. Trop profond pour être fiable sans compilateur sous la main. `XrayRenderer.kt` est maintenant un stub qui ne dessine rien.
- `XrayScanner` (scan des minerais en config) et le réglage "Voir à travers les murs" restent en place, inertes, prêts pour une vraie implémentation future si quelqu'un a une référence concrète de pipeline 26.2 fonctionnel.

## 0.0.13
- Fix build (x-ray) : `RenderType` a déménagé de `net.minecraft.client.renderer` vers `net.minecraft.client.renderer.rendertype` en 26.2. Confirmé par recherche croisée (primer NeoForge). Reste très probablement d'autres soucis dans `XrayRenderer.kt` : la refonte de `RenderType` va bien plus loin qu'un simple déplacement de package (`RenderType#draw` remplacé par `PreparedRenderType#drawFromBuffer`, `RenderType#create` pas encore vérifié pour 26.2).

## 0.0.12
- Fix build : module Fabric API renommé `fabric-key-binding-api-v1` → `fabric-key-mapping-api-v1` sur 26.2 (suit le renommage `KeyBinding`→`KeyMapping` côté Minecraft). `KeyBindingHelper.registerKeyBinding` → `net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping`. Confirmé en regardant le code source de Fabric API sur la branche 26.2.

## 0.0.11
- Fix build : `ClientCommandManager` a été supprimé de Fabric API sur 26.2, remplacé par `ClientCommands` (même méthode `literal(...)`). Confirmé en regardant directement le code source de Fabric API sur la branche 26.2 (le fichier `ClientCommandManager.java` n'existe plus).

## 0.0.10
- Fix build : `Inventory.selectedSlot` est une propriété (getter réel `getSelectedSlot()`), pas une méthode "nue" comme `mainCamera()`/`gameRenderState()`. Retiré les parenthèses en trop (`inventory.selectedSlot()` → `inventory.selectedSlot`). Le setter `setSelectedSlot(Int)` reste appelé explicitement.

## 0.0.9
Corrections suite à un vrai build local (Kotlin `compileClientKotlin`) — Minecraft 26.2 a renommé/supprimé beaucoup plus de choses que prévu :
- `Identifier.of(...)` → `Identifier.fromNamespaceAndPath(...)` (le vrai nom, confirmé).
- Les catégories de touches ne sont plus de simples chaînes : `KeyMapping.Category.register(Identifier)` doit être enregistrée puis passée au constructeur `KeyMapping`.
- `Inventory.selected` (champ public) devient `selectedSlot()` / `setSelectedSlot(int)` (méthodes).
- `Player.playNotifySound(...)` n'existe plus → son joué via `Minecraft.soundManager.play(SimpleSoundInstance.forUI(...))`.
- `GuiGraphicsExtractor.renderItem(...)` → `item(...)` (perte du préfixe `render`/`draw`, comme `text`/`centeredText`).
- **X-ray (`XrayRenderer.kt`) réécrit une deuxième fois** : `Tesselator` est purement et simplement supprimé en 26.2 (plus de rendu immédiat). Remplacé par le nouveau système "submit" : `LevelRenderEvents.COLLECT_SUBMITS` + `OrderedSubmitNodeCollector#submitShapeOutline(...)`, où c'est le moteur qui dessine la forme (`Shapes.block()`) au lieu de construire les sommets à la main. Toujours le point le plus fragile du mod — cast `submitNodeCollector() as? OrderedSubmitNodeCollector` non garanti, chaîne du `RenderPipeline.builder(...)` pas vérifiée à 100 %.

## 0.0.8
- Fix build : `AutoMiner.kt` et `XrayScanner.kt` déplacés de `src/main/kotlin` vers `src/client/kotlin`. Ils utilisent des classes client-only (`Minecraft`, `LocalPlayer`, `MultiPlayerGameMode`) qui n'existent pas sur le classpath du source set `main` avec `splitEnvironmentSourceSets()` — confirmé par une vraie erreur de compilation locale (`Unresolved reference`). Package inchangé (`com.jason54jg.miningmod`), donc aucun import à modifier ailleurs.

## 0.0.7
- Fix build : ajout du repository Maven de TerraformersMC (`https://maven.terraformersmc.com/releases/`) dans `build.gradle`. Mod Menu n'est ni sur le Maven de Fabric ni sur Maven Central, `compileOnly "com.terraformersmc:modmenu:..."` ne pouvait pas se résoudre sans ce repository. Confirmé par un vrai build local (le reste — Loom, Fabric API, Fabric Language Kotlin, Java 25 — résolvait déjà correctement).

## 0.0.6
- X-ray minerais : contours rouges visibles à travers les murs, dans un rayon de 16 blocs, rescan toutes les secondes. Liste des minerais concernés réglable dans la config (`Voir à travers les murs`) : désactivé / précieux (diamant, émeraude, débris antique) / tous.
- Refactor : ensembles/détection de minerais partagés entre le minage auto et le x-ray (`OreBlocks.kt`), au lieu d'être dupliqués.
- **Expérimental** : le rendu 3D (`XrayRenderer.kt`) utilise l'API de rendu bas niveau de Minecraft 26.2 (RenderPipeline/RenderType), entièrement réécrite dans cette version et impossible à compiler dans le sandbox de développement pour vérifier. C'est le fichier le plus susceptible de demander une correction manuelle.

## 0.0.5
- Écran de config : "slot" outil (case cliquable avec icône) — clique dessus en tenant un outil en main pour le désigner comme outil de minage ; le mod l'équipe automatiquement depuis la hotbar pendant qu'il mine. Non persisté entre les redémarrages du jeu (choix en mémoire uniquement).
- HUD moderne pendant le minage (coin haut-gauche) : temps écoulé, nombre de blocs cassés, détail par type de minerai (variantes deepslate fusionnées avec le minerai de base).

## 0.0.4
- Nouvelle option `pattern` (config + écran) : style de minage.
  - `STRAIGHT` (par défaut) : tout droit, comme avant.
  - `ZIGZAG` : alterne tout droit / droite / tout droit / gauche tous les 6 blocs avancés (tunnel en dents de scie, "de gauche à droite").
  - `CIRCLE` : tourne à droite après chaque bras, avec des bras de plus en plus longs (spirale).
- En zigzag/cercle, la direction de minage est pilotée par le mod (indépendante du regard) ; en `STRAIGHT`, elle suit toujours le regard comme avant.

## 0.0.3
- Détection de téléportation : alerte si le joueur se déplace anormalement en un tick (≥ 8 blocs).
- Détection de mention : alerte si le pseudo du joueur apparaît dans un message de chat.
- Alerte = son (`experience_orb.pickup`) + écran qui clignote (fond rouge semi-transparent) pendant 3 secondes.
- Correctif interne : Minecraft 26.2 a déplacé `Minecraft#setScreen` vers `Minecraft#gui#setScreen` (refonte Gui/Hud) — mis à jour dans la commande de config et l'écran de config.

## 0.0.2
- Configuration persistée (`config/miningmod.json`) : `reach` (portée/anticipation du tunnel), `blocksPerTick` (blocs cassables en un seul tick, pour serveurs non-vanilla avec capacités de casse instantanée multiple) et `lockYLevel` (reste au niveau Y où le minage a été activé, pour du strip mining en couche).
- Commande `/miningmod config` pour ouvrir l'écran de configuration.
- Intégration Mod Menu (optionnelle, sans dépendance dure) : bouton de config dans la liste des mods.

## 0.0.1
- Création initiale du mod.
- Touche (`B` par défaut) pour activer/désactiver le minage automatique en ligne droite : le joueur avance et casse les blocs devant lui (pieds + tête).
- Exception minerai : tout bloc de minerai touchant le tunnel (vanilla + tag `c:ores`) est cassé avant de continuer.

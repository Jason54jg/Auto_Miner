# Changelog

Chaque nouvelle demande incrémente la version de `0.0.1`.

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

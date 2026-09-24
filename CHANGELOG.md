# Changelog

Chaque nouvelle demande incrémente la version de `0.0.1`.

## 0.0.28
- Le scan large (cube autour du joueur, toutes directions) est maintenant réservé au **bloc obligatoire**. Les minerais vanilla/tag reviennent à l'ancien comportement "touche le tunnel" — sinon un minerai croisé dans un mur à distance trouait le tunnel au hasard. Pas de scan du tout si aucun bloc obligatoire n'est choisi (évite le coût du cube pour rien).

## 0.0.27
- Autonomie complète : la direction de minage/déplacement (tous styles, y compris "Tout droit") est maintenant figée à l'activation et n'est plus jamais lue depuis le regard en direct. La caméra suit automatiquement (fixe en ligne droite, clampée à ±30° en zigzag/cercle) : plus aucun mouvement ni visée du joueur n'est nécessaire une fois le minage lancé.
- Détection des blocs prioritaires (minerais + bloc obligatoire) : passe d'un scan limité aux blocs touchant le tunnel devant, à un scan en cube autour du joueur (portée = `Portée`), dans toutes les directions — un minerai est cassé en premier dès qu'il est détecté à portée, même sur le côté, au-dessus, en dessous ou derrière.

## 0.0.26
- Fix compile : `BuiltInRegistries.BLOCK.get(Identifier)` / `.ITEM.get(Identifier)` renvoient maintenant `Optional<Holder.Reference<T>>?` au lieu de `T?` directement. `ToolSelection.resolve`/`CustomOreSelection.resolve` déballent via `.orElse(null)?.value()`.

## 0.0.25
- Fix possible : `isBreakable` excluait tout bloc renvoyant un `fluidState` non vide, ce qui exclut aussi un bloc solide **waterlogged** (un minerai custom immergé, par ex.), pas seulement l'eau/la lave elles-mêmes. Utilise maintenant `state.liquid()` (vrai uniquement pour un bloc de fluide) pour ne plus bloquer un bloc obligatoire/minerai simplement parce qu'il est sous l'eau.

## 0.0.24
- Trou d'un seul bloc de large devant lui : le bot saute par-dessus au lieu de s'arrêter net. Un trou plus large (pas de sol non plus à l'atterrissage) reste bloquant comme avant (attend/mine plutôt que de tomber).

## 0.0.23
- Fix `/miningbot` : la commande n'ouvrait jamais l'écran de config (aucune erreur nulle part). Cause : `ChatScreen` ferme l'écran courant (`setScreen(null)`) juste après l'envoi de la commande, dans le même appel — ça écrasait aussitôt l'écran qu'on venait d'ouvrir depuis `executes`. L'ouverture est maintenant reportée à la tâche client suivante (`Minecraft.execute { ... }`), une fois le chat déjà fermé.

## 0.0.22
- Caméra en zigzag/cercle : ne tourne plus jamais de plus de 30° par rapport à la direction de départ, même quand le tunnel fait un cran à 90° (le déplacement/minage garde ses angles droits sur la grille de blocs, mais la vue reste dans le champ de vision d'origine). Limite connue : en cercle, une fois la spirale bien engagée, la direction réelle peut s'éloigner de la vue affichée (plafonnée à ±30°) — pas de suivi caméra parfait sur un virage complet.
- Fix : le bot ne marche plus dans le vide (trou, ravin, mine à ciel ouvert) — vérifie qu'il y a un sol avant d'avancer, sinon il attend/mine au lieu de tomber.

## 0.0.21
- L'outil et le bloc obligatoire survivent maintenant à un redémarrage du jeu (`toolItemId`/`customBlockId` dans `config/miningmod.json`, résolus au chargement). Fix au passage : "Terminé" écrasait la sélection tout juste faite dans les slots avec l'ancienne config chargée à l'ouverture de l'écran — la sauvegarde inclut maintenant la sélection actuelle des slots.

## 0.0.20
- Slot "Bloc obligatoire" : capture maintenant le bloc **visé** (regardé juste avant d'ouvrir la config) plutôt que l'objet en main. Vise le bloc voulu, puis ouvre `/miningbot` et clique sur le slot.

## 0.0.19
- Fix alertes téléportation/mention : elles tournaient en permanence, même sans minage actif. Un changement de monde/dimension déclenchait une fausse alerte "téléportation" (la position d'avant, dans un autre monde, n'a aucun rapport avec la nouvelle). Les alertes ne se déclenchent maintenant que pendant que le minage est actif, et le suivi de position repart à zéro à chaque activation.

## 0.0.18
- Config : `Portée`, `Blocs/tick`, `Couche Y` et `Largeur du tunnel` sont maintenant des champs de texte libres (n'importe quelle valeur), au lieu de boutons qui ne faisaient que cycler entre quelques présélections.
- Nouveau : slot "Bloc obligatoire" dans la config — clique dessus en tenant le bloc en main pour désigner un bloc custom (en plus des minerais vanilla) qui doit impérativement être cassé s'il touche le tunnel. Utile sur un serveur moddé avec ses propres blocs de ressource. Non persisté (comme le slot outil).
- Nouvelle touche `key.miningmod.ungrab` (par défaut `N`) : libère/reprend la souris, pour éviter qu'un mouvement de souris involontaire ne fasse tourner la caméra (et donc la direction de minage) pendant que le bot travaille.
- Zigzag retravaillé : lignes droites longues (12 blocs) entrecoupées de petits crans latéraux courts (2 blocs) au lieu d'alterner symétriquement 6 blocs tout droit / 6 blocs à 90°. Mouvement bien plus doux, sans partir complètement à droite ou à gauche.
- Cercle retravaillé : bras de spirale plus longs dès le départ et qui grandissent plus lentement, pour une spirale qui s'ouvre progressivement au lieu de tourner sur elle-même très vite au début.

## 0.0.17
- `Verrouiller le niveau Y` a maintenant une vraie valeur associée : `Couche Y` dans la config (présélections : -59, -54, -32, 0, 11, 15, 63), au lieu de juste figer la hauteur où le joueur se trouvait au moment de l'activation.
- Descente automatique vers la couche choisie avant de creuser à l'horizontale : casse le bloc sous les pieds, attend d'avoir atterri, recommence — jusqu'à atteindre la couche configurée. S'arrête net sur un bloc incassable ou un liquide. **Ce n'est pas du vrai pathfinding** (pas de contournement d'obstacles), juste une descente verticale contrôlée.
- `Largeur du tunnel` dans la config (1/3/5 blocs) : élargit la zone minée latéralement, utile avec un outil qui a son propre effet de zone (ex. un enchantement "Excavation" côté serveur) ou simplement pour creuser plus large.
- Refactor interne : la logique de casse d'un bloc (démarrer/continuer/confirmer) est maintenant partagée entre le tunnel horizontal et la descente verticale (`AutoMiner.breakBlock`).

## 0.0.16
- Fix minage : le minage ne se fiait qu'à la valeur de retour de `startDestroyBlock`/`continueDestroyBlock` pour savoir si un bloc était cassé (compteur HUD incrémenté, animation de bras, mais rien ne cassait réellement). Vérifie maintenant l'état réel du bloc (`isAir`) après chaque appel, quelle que soit la sémantique exacte du retour.
- Commande : `/miningmod config` → `/miningbot` (commande directe, plus de sous-commande `config`).
- Zigzag/cercle : le joueur tourne maintenant la tête et le corps pour suivre la direction pilotée par le mod (avant, seuls le déplacement et le minage tournaient, pas la caméra).

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

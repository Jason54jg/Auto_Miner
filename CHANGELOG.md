# Changelog

Chaque nouvelle demande incrémente la version de `0.0.1`.

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

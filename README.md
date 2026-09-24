# Mining Mod

Mod Fabric pour Minecraft **26.2** qui creuse un tunnel tout droit automatiquement : une touche active le minage, le joueur avance en cassant les blocs devant lui, et casse systématiquement tout minerai qui touche le tunnel.

Version actuelle : **0.0.8** (voir [CHANGELOG.md](CHANGELOG.md))

## Fonctionnalités

- Touche **B** (rebindable dans les options du jeu) pour activer/désactiver le minage automatique.
- Une fois activé, le mod :
  - casse les blocs devant le joueur (pieds + tête, dans la direction regardée) ;
  - fait avancer le joueur dans le tunnel une fois celui-ci dégagé ;
  - **exception minerai** : si un minerai (vanilla ou tag `c:ores` pour la compatibilité avec d'autres mods) touche le tunnel, il est cassé avant de continuer, même s'il n'est pas exactement dans l'axe de percement.
- Le minage respecte les règles normales de casse (outil, dureté du bloc, drops, XP) : ce n'est pas de la casse instantanée en survie.
- S'arrête automatiquement sur un bloc incassable (bedrock, etc.) ou un liquide.
- **Configurable pour serveurs non-vanilla** (`/miningmod config` ou bouton Mod Menu) :
  - `Portée` : distance d'anticipation du tunnel et de détection des minerais (utile si le joueur a une portée étendue par un autre mod/une capacité) ;
  - `Blocs/tick` : nombre de blocs pouvant être cassés en un seul tick (utile avec haste/efficacité ou une capacité de casse instantanée multiple) ;
  - `Verrouiller le niveau Y` : le tunnel reste à la hauteur où le minage a été activé (strip mining en couche).
  - Réglages sauvegardés dans `config/miningmod.json`.
- **Alertes téléportation/mention** : un son (ding) et un flash rouge de l'écran pendant 3 secondes se déclenchent si :
  - le joueur se déplace de façon anormale en un seul tick (téléportation, TP admin, etc.) ;
  - le pseudo du joueur apparaît dans un message de chat reçu (mention).
- **Style de minage** (`pattern` dans la config) :
  - `Tout droit` (par défaut) : suit le regard, comme avant.
  - `Zigzag` : alterne tout droit / droite / tout droit / gauche tous les 6 blocs (tunnel en dents de scie).
  - `Cercle` : tourne à droite après chaque bras, longueur de bras croissante (spirale).
  - En zigzag/cercle, la direction de minage est pilotée par le mod, indépendamment de là où le joueur regarde.
- **Slot outil** (écran de config) : case cliquable qui capture l'objet actuellement en main du joueur comme outil de minage ; le mod l'équipe automatiquement depuis la hotbar avant de miner. Choix en mémoire seulement (non sauvegardé entre deux lancements du jeu).
- **HUD de minage** (coin haut-gauche, visible pendant que le minage est actif) :
  - temps écoulé depuis l'activation ;
  - nombre total de blocs cassés ;
  - détail par type de minerai cassé (les variantes deepslate sont fusionnées avec le minerai de base).
- **X-ray minerais** (`Voir à travers les murs` dans la config) : contours rouges visibles à travers les murs pour les minerais choisis, dans un rayon de 16 blocs autour du joueur (rescan chaque seconde).
  - Désactivé (par défaut) / Précieux (diamant, émeraude, débris antique) / Tous les minerais.
  - ⚠️ **Expérimental** : cette fonctionnalité utilise l'API de rendu 3D bas niveau de Minecraft 26.2 (`RenderPipeline`/`RenderType`), entièrement réécrite dans cette version. Impossible de la compiler dans l'environnement de développement de ce projet pour la vérifier (pas d'accès réseau à Mojang/Fabric, pas de JDK 25) — c'est le point du mod le plus susceptible de demander une correction manuelle (voir `XrayRenderer.kt`).

## Prérequis

- Minecraft **26.2**
- [Fabric Loader](https://fabricmc.net/) `>= 0.19.5`
- [Fabric API](https://modrinth.com/mod/fabric-api) `0.161.0+26.2`
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) `1.13.12+kotlin.2.4.0`
- [Mod Menu](https://modrinth.com/mod/modmenu) `20.0.1` *(optionnel, pour le bouton de configuration dans la liste des mods)*
- Java **25**

## Build

```bash
./gradlew build
```

Le `.jar` généré se trouve dans `build/libs/`.

## Installation

1. Installer Fabric Loader pour Minecraft 26.2.
2. Placer dans le dossier `mods/` :
   - `miningmod-<version>.jar`
   - `fabric-api-<version>.jar`
   - `fabric-language-kotlin-<version>.jar`

## Structure du projet

```
src/
├── main/kotlin/...    # Données/logique communes (config, stats, mod initializer) — pas de classes client-only
├── client/kotlin/...  # AutoMiner, x-ray, keybind, HUD, écrans (tout ce qui touche Minecraft/LocalPlayer/rendu)
└── main/resources/    # fabric.mod.json, lang
```

## Versioning

Chaque nouvelle demande de fonctionnalité/modification incrémente la version de `0.0.1` (`0.0.1` → `0.0.2` → `0.0.3`, ...). Le détail de chaque version est dans [CHANGELOG.md](CHANGELOG.md).

## Licence

[MIT](LICENSE)

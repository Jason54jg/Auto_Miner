# Auto Miner

*[English](README.md) | Français*

Mod Fabric pour Minecraft **26.1 – 26.3** qui creuse un tunnel tout droit automatiquement : une touche active le minage, le joueur avance en cassant les blocs devant lui, et casse systématiquement tout minerai qui touche le tunnel.

Version actuelle : **0.0.43** (voir [CHANGELOG.fr.md](CHANGELOG.fr.md))

## Fonctionnalités

- Touche **B** (rebindable dans les options du jeu) pour activer/désactiver le minage automatique — un message coloré dans le chat confirme à chaque fois ("Minage automatique activé" en vert, "désactivé" en rouge).
- Une fois activé, le mod :
  - casse les blocs devant le joueur (tête d'abord, puis pieds — le viseur pointe la tête), dans la direction figée au moment de l'activation ;
  - fait avancer le joueur dans le tunnel une fois celui-ci dégagé ;
  - **exception minerai** : un minerai (vanilla ou tag `c:ores` pour la compatibilité avec d'autres mods) qui touche le tunnel est cassé avant de continuer, même s'il n'est pas exactement dans l'axe de percement. Le **bloc obligatoire**, lui, est détecté à portée tout autour du joueur (devant, derrière, sur les côtés, au-dessus, en dessous), pas seulement s'il touche le tunnel.
  - le joueur n'a plus rien à faire une fois activé : le mod pilote entièrement le déplacement **et** la caméra, aucun mouvement ni visée n'est nécessaire.
- Le minage respecte les règles normales de casse (outil, dureté du bloc, drops, XP) : ce n'est pas de la casse instantanée en survie.
- S'arrête automatiquement sur un bloc incassable (bedrock, etc.) ou un liquide : après ~1s sans casser ni avancer, le minage se coupe tout seul et alerte le joueur (son + flash), au lieu de rester bloqué en silence.
- **Configurable pour serveurs non-vanilla** (`/autominer` ou bouton Mod Menu) :
  - `Portée`, `Blocs/tick`, `Couche Y` et `Largeur du tunnel` sont des champs de texte libres (n'importe quelle valeur, pas juste des présélections) ;
  - `Portée` : distance d'anticipation du tunnel et de détection des minerais (utile si le joueur a une portée étendue par un autre mod/une capacité) ;
  - `Blocs/tick` : nombre de blocs pouvant être cassés en un seul tick (utile avec haste/efficacité ou une capacité de casse instantanée multiple) ;
  - `Verrouiller le niveau Y` + `Couche Y` : le tunnel reste à la hauteur choisie plutôt qu'à celle où le minage a été activé. Le mod descend automatiquement (casse le bloc sous les pieds, attend d'atterrir, recommence) jusqu'à cette couche avant de creuser à l'horizontale — pas un vrai pathfinding, juste une descente verticale, qui s'arrête net sur un bloc incassable ou un liquide.
  - `Largeur du tunnel` : élargit la zone minée latéralement (utile avec un outil qui a son propre effet de zone, ou juste pour creuser plus large).
  - **Slot "Bloc obligatoire"** : vise le bloc voulu dans le monde, puis ouvre `/autominer` et clique sur le slot — désigne ce bloc custom comme à casser impérativement dès qu'il est détecté à portée, où qu'il soit autour du joueur (contrairement aux minerais vanilla, limités à ce qui touche le tunnel). Utile sur un serveur moddé avec ses propres blocs de ressource. Choix en mémoire seulement (comme le slot outil).
  - `Position HUD X`/`Position HUD Y` : déplace le panneau du HUD de minage (coin haut-gauche par défaut) où tu veux à l'écran, en pixels.
  - Réglages sauvegardés dans `config/autominer.json` (sauf le slot outil et le bloc obligatoire, volontairement non persistés).
- **Alertes téléportation/mention** (actives uniquement pendant que le minage tourne) : un son (ding) et un flash rouge de l'écran pendant 3 secondes se déclenchent si :
  - le joueur se déplace de façon anormale en un seul tick (téléportation, TP admin, etc.) ;
  - le pseudo du joueur apparaît dans un message de chat reçu (mention).
- Minage tout droit uniquement : la direction est figée dès l'activation (plus jamais liée au regard en direct) et la caméra reste fixe dessus tant que le minage tourne.
- Le bot ne marche pas dans le vide **si `Verrouiller le niveau Y` est actif** (et que le joueur ne vole pas) : sans sol devant (trou, ravin), il saute par-dessus si le trou fait 1 bloc de large, sinon il s'arrête au lieu de tomber. Sans verrouillage, ou en vol créatif/spectateur, aucune hauteur à maintenir : il ignore les trous et avance tout droit sans se soucier du sol — le bloc devant lui, s'il y en a un, est cassé normalement dans tous les cas.
- Le bloc qui porte le joueur n'est jamais cassé, même s'il correspond à un minerai/bloc obligatoire détecté juste sous les pieds — évite de tomber dans le vide en minant son propre plancher.
- **Touche "libérer la souris"** (`key.autominer.ungrab`, par défaut `N`) : évite qu'un mouvement de souris involontaire ne fasse tourner la caméra (et donc la direction de minage) pendant que le bot travaille. Un message dans le chat confirme à chaque fois, et un rappel reste affiché à l'écran tant que la souris est libérée.
- **Slot outil** (écran de config) : case cliquable qui capture l'objet actuellement en main du joueur comme outil de minage ; le mod l'équipe automatiquement depuis la hotbar avant de miner. Choix en mémoire seulement (non sauvegardé entre deux lancements du jeu).
- **HUD de minage** (coin haut-gauche, visible pendant que le minage est actif) :
  - temps écoulé depuis l'activation ;
  - nombre total de blocs cassés ;
  - détail par type de minerai cassé (les variantes deepslate sont fusionnées avec le minerai de base).
- **Interface traduite** : l'écran de config, le HUD et les messages chat suivent la langue du jeu du client (anglais par défaut, français fourni) via le système de traduction de Minecraft — comme les noms des touches. Le nom de l'outil/du bloc obligatoire utilise le vrai nom localisé fourni par Minecraft.

## Prérequis

- Minecraft **26.1 – 26.3** (un jar par version, compilé avec [Stonecutter](https://stonecutter.kikugie.dev/))
- [Fabric Loader](https://fabricmc.net/) `>= 0.19.5`
- [Fabric API](https://modrinth.com/mod/fabric-api) et [Mod Menu](https://modrinth.com/mod/modmenu) *(optionnel, pour le bouton de configuration dans la liste des mods)* correspondant à ta version de Minecraft :

  | Minecraft | Fabric API | Mod Menu |
  | --------- | ---------- | -------- |
  | 26.1 – 26.1.2 | `0.155.3+26.1.2` | `18.0.2` |
  | 26.2 | `0.161.0+26.2` | `20.0.1` |
  | 26.3 | `0.162.0+26.3` | `21.0.0` |

- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) : `1.13.12+kotlin.2.4.0` (26.1/26.2) ou `1.14.2+kotlin.2.4.21` (26.3)
- Java **25**

## Build

```bash
./gradlew :26.1.2:build   # Minecraft 26.1 – 26.1.2
./gradlew :26.2:build     # Minecraft 26.2
./gradlew :26.3:build     # Minecraft 26.3
```

Les `.jar` générés (`autominer-<version>+<mc>.jar`) se trouvent dans `versions/<mc>/build/libs/`.

## Installation

1. Installer Fabric Loader pour ta version de Minecraft (26.1, 26.2 ou 26.3).
2. Placer dans le dossier `mods/` les fichiers correspondants :
   - `autominer-<version>+<mc>.jar`
   - `fabric-api-<version>.jar`
   - `fabric-language-kotlin-<version>.jar`

## Structure du projet

```
src/
├── main/kotlin/...    # Données/logique communes (config, stats, mod initializer) — pas de classes client-only
├── client/kotlin/...  # AutoMiner, keybind, HUD, écrans (tout ce qui touche Minecraft/LocalPlayer/rendu)
└── main/resources/    # fichiers de lang (partagés)
versions/
├── 26.1.2/            # gradle.properties + fabric.mod.json (~26.1) pour Minecraft 26.1 – 26.1.2
├── 26.2/              # gradle.properties + fabric.mod.json (~26.2)
└── 26.3/              # gradle.properties + fabric.mod.json (~26.3)
```

Les (rares) différences d'API selon la version (constantes de touches SDL en 26.3, `setScreen` en 26.1) sont gérées avec des conditions Stonecutter `//?` directement dans les sources partagées.

## Versioning

Chaque nouvelle demande de fonctionnalité/modification incrémente la version de `0.0.1` (`0.0.1` → `0.0.2` → `0.0.3`, ...). Le détail de chaque version est dans [CHANGELOG.fr.md](CHANGELOG.fr.md).

## Licence

[MIT](LICENSE)

![Auto Miner — Une touche, le minage continue.](https://raw.githubusercontent.com/Jason54jg/MiningMod/master/site/assets/banner.png)

**Choisissez une direction. Appuyez sur B. Le minage continue.**

Auto Miner est un **mod client Fabric** qui creuse un tunnel et vous fait avancer en donnant la priorité aux minerais proches. Les règles de survie restent les mêmes : l’outil, la dureté du bloc, les objets lâchés et l’XP comptent toujours.

[Installation et compatibilité](https://jason54jg.github.io/MiningMod/fr/#install) · [English guide](https://jason54jg.github.io/MiningMod/) · [Code et problèmes](https://github.com/Jason54jg/MiningMod)

## Moins de répétition. Plus de contrôle.

- **Priorité aux minerais.** Cassez ceux qui touchent le tunnel avant d’avancer. Désignez aussi un bloc obligatoire à chercher autour de vous, dans votre portée d’interaction.
- **Votre tunnel.** Réglez largeur, portée et couche Y. Choisissez le mode tout droit ou la navigation intelligente pour les obstacles proches, les décalages sûrs et les marches descendantes.
- **Votre outil.** Sélectionnez un outil : le mod l’équipe depuis la barre rapide s’il est disponible.
- **Votre session.** Un HUD déplaçable suit le temps, les blocs cassés et les minerais. Des alertes signalent les mentions, les grands déplacements et le minage bloqué.
- **Vos réglages.** Une interface à onglets avec Enregistrer / Annuler, des réglages persistants et les textes en français et en anglais.

## Installation

1. Utilisez **Java 25** et **Fabric Loader 0.19.5+** pour Minecraft **26.1–26.3**.
2. Téléchargez le JAR Auto Miner correspondant à votre version, puis **[Fabric API](https://modrinth.com/mod/fabric-api)** et **[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)** pour cette même version. [Mod Menu](https://modrinth.com/mod/modmenu) est facultatif.
3. Placez les fichiers dans le dossier `mods` de votre instance et lancez le jeu.

| Commande | Action |
| --- | --- |
| **B** | Activer / arrêter le minage |
| **N** | Libérer / reprendre la souris |
| **`/autominer`** | Ouvrir la configuration |

B et N sont réassignables dans les commandes de Minecraft. Le mod s’installe **côté client** : aucun composant Auto Miner n’est requis sur le serveur.

## Avant une longue session

Auto Miner vérifie la portée, arrête un minage qui ne progresse plus et termine la session à la déconnexion ou au changement de monde. Le verrouillage Y ajoute des contrôles du sol. La navigation intelligente gère les **obstacles proches**, pas un pathfinding général ; elle ne garantit pas la sécurité dans toutes les situations et ne donne pas de permissions serveur.

Sur un serveur, **vérifiez que le règlement autorise l’automatisation du minage** avant de l’utiliser.

[Guide complet](https://jason54jg.github.io/MiningMod/fr/) · [Signaler un problème](https://github.com/Jason54jg/MiningMod/issues)

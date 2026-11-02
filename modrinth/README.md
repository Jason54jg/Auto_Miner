# Présentation Modrinth

- `description.en.md` : description anglaise complète, prête à coller.
- `description.fr.md` : version française.
- `project.json` : titre, identifiant du projet et descriptions courtes.
- Icône à téléverser : `site/assets/logo.webp` (moins de 256 Kio, limite Modrinth).
- Bannière : `site/assets/banner.png`, référencée dans les descriptions via GitHub.

Le projet connu est `https://modrinth.com/mod/auto-miners`. Un projet encore en modération peut
renvoyer « Project not found » aux requêtes publiques ; une requête authentifiée
par un membre autorisé est alors nécessaire. Aucun changement de statut, de visibilité ou de version du mod
n’est demandé par le script.

## Aperçu local

```bash
python3 scripts/publish_modrinth.py --language en --dry-run
python3 scripts/publish_modrinth.py --language fr --dry-run
```

## Synchronisation depuis GitHub Actions

1. Ajouter un secret de dépôt **MODRINTH_TOKEN** dans **Settings → Secrets and
   variables → Actions**. Utiliser un jeton Modrinth autorisé à modifier ce projet
   (y compris sa description et son icône). Ne jamais committer la valeur.
2. Les modifications de présentation poussées sur `master` déclenchent la
   synchronisation anglaise. On peut aussi lancer **Update Modrinth presentation**,
   choisir la langue et laisser `dry_run` désactivé pour appliquer la mise à jour.
3. Le script relit les champs et vérifie la présence d’une icône après l’envoi.

GitHub ne permet le lancement manuel que lorsque le workflow existe sur la
branche par défaut. Si celle-ci n’est pas `master`, on peut soit désigner `master`
comme branche par défaut dans les paramètres, soit exécuter le script localement
avec `MODRINTH_TOKEN` fourni par un gestionnaire de secrets.

Sans jeton Modrinth, le workflow signale explicitement que la synchronisation est
ignorée : le texte et les images sont prêts dans le dépôt, mais la page publique
n’est pas modifiée. Une synchronisation réussie ne publie pas de JAR et
ne termine pas la modération Modrinth. Attendre aussi la publication du site avant
que ses liens de documentation puissent être visités.

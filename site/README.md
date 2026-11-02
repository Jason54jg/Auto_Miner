# Site d’information Auto Miner

Site statique bilingue, sans npm ni dépendance de build. Le contenu est dans
`content.json`, la structure dans `template.html`, les styles/scripts dans `assets/`.

```bash
python3 scripts/build_site.py
python3 scripts/check_site.py
python3 -m http.server 8000 --directory _site
```

Le build produit `_site/index.html` et `_site/fr/index.html`. La version du mod et
les trois jeux de dépendances viennent directement des fichiers Gradle. Le logo
PNG est copié depuis la ressource réellement embarquée dans le mod. Les WebP
sont des exports compressés des mêmes visuels pour limiter le poids des pages.

Le workflow `.github/workflows/pages.yml` valide le site sur les pull requests
et déploie les changements de `master`. Adresse prévue :
`https://jason54jg.github.io/Auto_Miner/`.

## Activation de GitHub Pages

Dans le dépôt GitHub, ouvrir **Settings → Pages → Build and deployment → Source**
et sélectionner **GitHub Actions**. Si l’environnement `github-pages` restreint
les branches autorisées, y autoriser `master`. L’administration de Pages requiert
une autorisation GitHub spécifique. Un push des fichiers ne prouve pas que cette
activation a été faite ni qu’un déploiement a réussi.

Les pushes sur `master` déclenchent le workflow Pages ; GitHub n’affiche le déclenchement manuel
`workflow_dispatch` que si le workflow est aussi présent sur la branche par défaut.
Il est recommandé de choisir `master` comme branche par défaut si elle devient
la branche de publication habituelle, sans écraser le contenu d’autres branches.

Pour vérifier après activation : consulter le workflow **Build and deploy information
site**, attendre le succès du job `deploy`, puis contrôler les versions anglaise
et française. Les contrôles locaux ne constituent pas une preuve de déploiement.

## Une page README/Jekyll s'affiche à la place du site

Ne pas utiliser **Deploy from a branch → master / (root)** : cela publie les
fichiers du dépôt et peut écraser le site généré par le workflow. La source doit
être **GitHub Actions**. Le workflow lit l'URL réelle de Pages, génère `_site`,
puis vérifie les deux langues, les ressources et la version réellement servie.
Le job signale un mode de publication concurrent dans son résumé. La modification
de ce réglage nécessite un administrateur ; le jeton de déploiement ne peut pas
le faire, même avec `pages: write`. Les règles de protection de l'environnement
`github-pages` sont conservées.

```bash
python3 scripts/check_deployed_site.py https://jason54jg.github.io/Auto_Miner/
```

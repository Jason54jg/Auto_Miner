#!/usr/bin/env python3
"""Build the bilingual static site from repository version pins; no packages required."""
import argparse
import html
import json
from pathlib import Path
import shutil
from string import Template
from urllib.parse import urlencode

ROOT = Path(__file__).resolve().parents[1]
SITE_URL = 'https://jason54jg.github.io/Auto_Miner'
MODRINTH_URL = 'https://modrinth.com/mod/auto-miners'
GITHUB_URL = 'https://github.com/Jason54jg/Auto_Miner'


def properties(path):
    return dict(line.split('=', 1) for raw in path.read_text().splitlines()
                if (line := raw.strip()) and not line.startswith('#') and '=' in line)


def release_data():
    versions = []
    for path in sorted((ROOT / 'versions').glob('*/gradle.properties')):
        props = properties(path)
        versions.append({'version': props['minecraft_version'],
                         'fabric_api': props['fabric_api_version'],
                         'fabric_kotlin': props['fabric_language_kotlin_version'],
                         'modmenu': props['modmenu_version']})
    return {'version': properties(ROOT / 'gradle.properties')['version'],
            'modrinth_url': MODRINTH_URL, 'minecraft': versions}


def build(output, site_url=SITE_URL):
    output = output.resolve()
    if output == ROOT or output in ROOT.parents:
        raise ValueError('Choose a dedicated site output directory')
    output.mkdir(parents=True, exist_ok=True)
    shutil.copytree(ROOT / 'site/assets', output / 'assets', dirs_exist_ok=True)
    shutil.copyfile(ROOT / 'src/main/resources/assets/autominer/textures/logo.png', output / 'assets/logo.png')
    (output / '.nojekyll').touch()
    release = release_data()
    release['site_url'] = site_url.rstrip('/') + '/'
    latest = release['minecraft'][-1]
    content = json.loads((ROOT / 'site/content.json').read_text())
    template = Template((ROOT / 'site/template.html').read_text())
    for lang, strings in content.items():
        page_dir = output if lang == 'en' else output / lang
        page_dir.mkdir(parents=True, exist_ok=True)
        base = '.' if lang == 'en' else '..'
        values = {key: html.escape(value, quote=True) for key, value in strings.items()}
        en_link, fr_link = ('./', './fr/') if lang == 'en' else ('../', './')
        language_links = ''.join(
            f'<a href="{link}" lang="{code}" hreflang="{code}"'
            f'{" aria-current=\"page\"" if code == lang else ""}>{code.upper()}</a>'
            for code, link in [('en', en_link), ('fr', fr_link)])
        values.update({'base': base, 'site_url': site_url.rstrip('/'),
                       'canonical': site_url.rstrip('/') + ('/' if lang == 'en' else '/fr/'),
                       'version': html.escape(release['version']), 'github_url': GITHUB_URL,
                       'modrinth_url': MODRINTH_URL, 'language_links': language_links,
                       'readme_url': GITHUB_URL + '/blob/master/' + ('README.fr.md' if lang == 'fr' else 'README.md'),
                       'version_options': ''.join(f'<option value="{v["version"]}"'
                                                f'{" selected" if v == latest else ""}>{v["version"]}</option>'
                                                for v in release['minecraft']),
                       'fabric_version': latest['fabric_api'], 'kotlin_version': latest['fabric_kotlin'],
                       'modmenu_version': latest['modmenu'],
                       'version_download': html.escape(MODRINTH_URL + '/versions?' + urlencode({'g': latest['version'], 'l': 'fabric'})),
                       'release_data': json.dumps(release, ensure_ascii=False).replace('<', '\\u003c')})
        (page_dir / 'index.html').write_text(template.substitute(values))
    (output / 'release.json').write_text(json.dumps(release, indent=2) + '\n')
    (output / 'robots.txt').write_text('User-agent: *\nAllow: /\nSitemap: ' + site_url.rstrip('/') + '/sitemap.xml\n')
    (output / 'sitemap.xml').write_text('<?xml version="1.0" encoding="UTF-8"?>\n'
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">'
        f'<url><loc>{html.escape(site_url.rstrip("/"))}/</loc></url>'
        f'<url><loc>{html.escape(site_url.rstrip("/"))}/fr/</loc></url></urlset>\n')
    print(f'Built EN/FR site for Auto Miner {release["version"]}: {output}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / '_site')
    parser.add_argument('--site-url', default=SITE_URL)
    args = parser.parse_args()
    build(args.output, args.site_url)

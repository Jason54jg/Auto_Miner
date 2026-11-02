#!/usr/bin/env python3
"""Sync the reviewed project presentation and icon without changing releases or moderation status."""
import argparse
import json
import os
from pathlib import Path
import sys
from urllib.error import HTTPError
from urllib.parse import quote
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
API = 'https://api.modrinth.com/v2'
ICON = ROOT / 'site/assets/logo.webp'
SITE = 'https://jason54jg.github.io/Auto_Miner/'
GITHUB = 'https://github.com/Jason54jg/Auto_Miner'


def api_request(method, path, token, body=None, content_type='application/json'):
    request = Request(API + path, data=body, method=method, headers={
        'Authorization': token,
        'User-Agent': 'Jason54jg/Auto_Miner-branding (github.com/Jason54jg/Auto_Miner)',
        'Content-Type': content_type,
    })
    try:
        with urlopen(request, timeout=30) as response:
            data = response.read()
            return json.loads(data) if data else None
    except HTTPError as error:
        # Do not log request headers, tokens or private project response bodies.
        raise RuntimeError(f'Modrinth returned HTTP {error.code} for {method} {path}; '
                           'check the project identifier and the token project-edit permissions.') from None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--language', choices=['en', 'fr'], default='en')
    parser.add_argument('--dry-run', action='store_true')
    args = parser.parse_args()
    project = json.loads((ROOT / 'modrinth/project.json').read_text())
    payload = {'title': project['title'], 'description': project['description'][args.language],
               'body': (ROOT / f'modrinth/description.{args.language}.md').read_text(),
               'wiki_url': SITE + ('fr/' if args.language == 'fr' else ''),
               'source_url': GITHUB, 'issues_url': GITHUB + '/issues'}
    if len(payload['description']) > 255:
        raise ValueError('The short description must not exceed 255 characters')
    if not ICON.is_file() or ICON.stat().st_size > 256 * 1024:
        raise ValueError('Modrinth requires an icon no larger than 256 KiB')
    if args.dry_run:
        print(json.dumps({'project': project['slug'], 'payload': payload,
                          'icon': str(ICON.relative_to(ROOT)), 'icon_bytes': ICON.stat().st_size},
                         ensure_ascii=False, indent=2))
        return
    token = os.environ.get('MODRINTH_TOKEN')
    if not token:
        raise ValueError('Add MODRINTH_TOKEN as a GitHub Actions repository secret with project-edit permission. '
                         'Never put its value in source files or chat.')
    slug = quote(project['slug'], safe='')
    current = api_request('GET', '/project/' + slug, token)
    project_id = quote(current['id'], safe='')
    api_request('PATCH', '/project/' + project_id, token, json.dumps(payload).encode())
    api_request('PATCH', '/project/' + project_id + '/icon?ext=webp', token, ICON.read_bytes(), 'image/webp')
    updated = api_request('GET', '/project/' + project_id, token)
    for key, expected in payload.items():
        if updated.get(key) != expected:
            raise RuntimeError(f'The update could not be verified for field {key}')
    if not updated.get('icon_url'):
        raise RuntimeError('Modrinth did not return an icon URL after upload')
    print(f'Updated and verified presentation and icon for {project["slug"]} ({args.language}).')


if __name__ == '__main__':
    try:
        main()
    except (ValueError, RuntimeError, OSError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)

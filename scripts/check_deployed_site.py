#!/usr/bin/env python3
"""Verify that Pages serves the generated site rather than the repository's README."""
import argparse
import json
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import urljoin
from urllib.request import Request, urlopen

from build_site import release_data
from check_site import Page


def fetch(url, method='GET'):
    separator = '&' if '?' in url else '?'
    request = Request(url + separator + 'verify=' + str(time.time_ns()), method=method,
                      headers={'Cache-Control': 'no-cache', 'User-Agent': 'AutoMiner-Pages-check'})
    with urlopen(request, timeout=15) as response:
        return response.read() if method == 'GET' else b''


def check(site_url, expected_version):
    site_url = site_url.rstrip('/') + '/'
    assets = set()
    for suffix in ['', 'fr/']:
        url = urljoin(site_url, suffix)
        page = Page(fetch(url).decode('utf-8'))
        if page.generator != 'Auto Miner static site' or 'hero-title' not in page.ids:
            raise ValueError('Pages is not serving the generated site. Select GitHub Actions '
                             'as the publishing source; a legacy root build can overwrite it.')
        if page.canonical != url:
            raise ValueError(f'Wrong canonical address: expected {url}, got {page.canonical}')
        for asset in page.images + page.links:
            if '/assets/' in asset:
                assets.add(urljoin(url, asset))
    release = json.loads(fetch(urljoin(site_url, 'release.json')))
    if release['version'] != expected_version:
        raise ValueError(f'Pages still serves {release["version"]}; expected {expected_version}')
    if release['site_url'] != site_url:
        raise ValueError('Published release metadata uses the wrong Pages address')
    for asset in sorted(assets):
        fetch(asset, method='HEAD')
    print(f'PASS live EN/FR site, canonical addresses, {len(assets)} assets and version {expected_version}')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('site_url')
    parser.add_argument('--attempts', type=int, default=1)
    args = parser.parse_args()
    if args.attempts < 1:
        parser.error('--attempts must be positive')
    expected = release_data()['version']
    for attempt in range(1, args.attempts + 1):
        try:
            check(args.site_url, expected)
            return
        except (ValueError, KeyError, HTTPError, URLError, TimeoutError) as error:
            print(f'Check {attempt}/{args.attempts}: {error}', file=sys.stderr)
            if attempt == args.attempts:
                sys.exit(1)
            time.sleep(5)


if __name__ == '__main__':
    main()

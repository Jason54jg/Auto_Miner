#!/usr/bin/env python3
"""Check generated pages, local links, brand assets and release metadata."""
from html.parser import HTMLParser
from pathlib import Path
import hashlib
import json
import sys
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]

class Page(HTMLParser):
    def __init__(self, text):
        super().__init__()
        self.ids, self.links, self.images = set(), [], []
        self.h1 = 0
        self.feed(text)
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if 'id' in attrs:
            assert attrs['id'] not in self.ids, f'Duplicate id: {attrs["id"]}'
            self.ids.add(attrs['id'])
        if tag == 'h1': self.h1 += 1
        if tag == 'a' and 'href' in attrs: self.links.append(attrs['href'])
        if tag == 'img':
            assert 'alt' in attrs, 'Missing image alt'
            self.images.append(attrs['src'])
        if tag in ('script', 'link'):
            value = attrs.get('src') or attrs.get('href')
            if value and not urlsplit(value).scheme: self.links.append(value)


def check(output):
    output = output.resolve()
    for relative in ['index.html', 'fr/index.html']:
        path = output / relative
        text = path.read_text()
        page = Page(text)
        assert page.h1 == 1, f'{relative}: expected one main heading'
        assert '$heading' not in text and '$base' not in text, 'Unresolved template'
        for url in page.links + page.images:
            parsed = urlsplit(url)
            if parsed.scheme or parsed.netloc: continue
            target = (path.parent / unquote(parsed.path)).resolve() if parsed.path else path
            assert target == output or output in target.parents, f'Link escapes site: {url}'
            if target.is_dir(): target /= 'index.html'
            assert target.is_file(), f'{relative}: missing {url}'
            if parsed.fragment and target.suffix == '.html':
                assert parsed.fragment in Page(target.read_text()).ids, f'Broken anchor: {url}'
        print(f'PASS {relative}: headings, alt text, local links and anchors')
    canonical = ROOT / 'src/main/resources/assets/autominer/textures/logo.png'
    assert hashlib.sha256(canonical.read_bytes()).digest() == hashlib.sha256((output / 'assets/logo.png').read_bytes()).digest()
    release = json.loads((output / 'release.json').read_text())
    assert len(release['minecraft']) == 3
    for row in release['minecraft']:
        assert all(row[key] for key in ['version', 'fabric_api', 'fabric_kotlin', 'modmenu'])
    print(f'PASS shared logo and three dependency sets; source version {release["version"]}')


if __name__ == '__main__':
    check(Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / '_site')

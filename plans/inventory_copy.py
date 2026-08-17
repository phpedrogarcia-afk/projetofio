#!/usr/bin/env python3
"""Inventário de copy hardcoded no FioApp.kt — para COPY-CANON e red team."""
import re

SRC = "mobile/src/main/java/com/projetofio/app/ui/FioApp.kt"
pat = re.compile(r'(\d+):\s*(?:Text\(|modifier = Modifier[^.]*\.contentDescription\s*=\s*)"([^"]*)"')
pat2 = re.compile(r'(\d+):\s*"[^"]*{[^}]*}[^"]*"')

lines = open(SRC, encoding="utf-8").read().splitlines()

# Seções aproximadas por linha
sections = [
    ("Editor/Home", 1, 600),
    ("Arquivo", 600, 830),
    ("Devolver agora?", 830, 880),
    ("Configurações", 880, 1100),
    ("ReturnScreen", 1100, 1180),
    ("Export/Import/Confirmations", 1180, 1400),
]

for name, lo, hi in sections:
    hits = [l for l in lines[lo-1:hi] if ('Text("' in l or 'contentDescription =' in l) and '"' in l]
    if not hits:
        continue
    print(f"\n== {name} (linhas {lo}-{hi})")
    for h in hits:
        h = h.strip()[:140]
        print("  ", h)

#!/usr/bin/env python3
"""Inventário de superfícies, estados e copy do FioApp para o UX Observatory (Missão 3)."""
import re

SRC = "/home/ubuntu/projetofio/app/mobile/src/main/java/com/projetofio/app/ui/FioApp.kt"
VM = "/home/ubuntu/projetofio/app/mobile/src/main/java/com/projetofio/app/ui/FioViewModel.kt"
lines = open(SRC).readlines()

print("=" * 70)
print("SURFACE INVENTORY — FioApp.kt")
print("=" * 70)

# Composables principais com linhas
composables = []
for i, l in enumerate(lines, 1):
    m = re.search(r"private fun (\w+)\(|fun (\w+)\(", l)
    if m and i > 100:
        name = m.group(1) or m.group(2)
        composables.append((i, name))

for ln, name in composables:
    print(f"\n## {name} (linha {ln})")
    # imprimir ~18 linhas após a assinatura
    for j in range(ln, min(ln + 18, len(lines))):
        print(f"  {lines[j].rstrip()}")

# Copy literal por seção
print("\n" + "=" * 70)
print("COPY LITERALS — todos os literais com linha")
print("=" * 70)
for i, l in enumerate(lines, 1):
    for m in re.finditer(r'"([^"]{2,})"', l):
        print(f"  L{i}: {m.group(1)[:90]!r}")

# FioUiState fields
print("\n" + "=" * 70)
print("STATE — FioUiState (FioViewModel.kt)")
print("=" * 70)
vm = open(VM).readlines()
for i, l in enumerate(vm, 1):
    if "data class FioUiState" in l:
        for j in range(i - 1, min(i + 60, len(vm))):
            print(f"  {vm[j].rstrip()}")
        break

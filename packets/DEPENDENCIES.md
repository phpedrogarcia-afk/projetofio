# PACKET DEPENDENCIES — grafo do que trava o quê

**Leitura:** `A → B` significa "A precisa estar pronto antes de B poder começar". Blocos em paralelo são separados por `||`. Círculos: F = founder decision, H = hardware/aparelho, D = decisão de produto.

## 1. Grafo textual

```
FIO-PQ-01 ──→ FIO-PQ-02
        ──→ FIO-PQ-03 ──→ FIO-P11 (D) │ FIO-P15 (D) │ FIO-P16 (D)
        ──→ FIO-PQ-04 (H-fundador)

FIO-P01 (H) ──→ FIO-P03 ──→ FIO-P02 (H, parcialmente)
FIO-P04 (H) ──→ FIO-P09 (H)
FIO-P05 (H) ──→ engine-release (milestone)

FIO-P06 ──→ FIO-P12 (H) ──→ FIO-P13 ──→ FIO-P15 (D, opcional via sealed-vetorial)
FIO-P07 (independente)
FIO-P08 (D) ──→ schema-4 (ADR) ──→ FIO-P10 (calibragem migration, pode ser adiantada)
FIO-P10 (independente da migration real)
FIO-P14 (D, independente dos demais)

FIO-PILOT-01 (H-fundador) ──→ FIO-PILOT-02 ──→ K-06 mitigation report ──→ FIO-P08 (alimenta)
```

## 2. Caminho crítico

`FIO-PQ-03 (decisões escritas) → FIO-P12 (device gate semântica, precisa da D-3) → FIO-P13 → FIO-P15` e, em paralelo, `FIO-P01 → FIO-P03 (crypto review)`. O caminho mais arriscado é o cripto: **nenhum packet de produto deveria depender de crypto review não executado** — hoje todos dependem implicitamente; P01/P03 existem para fechar essa dívida.

## 3. Colisões conhecidas (executar com cuidado)

1. P15 (sealed V1, schema 4) e P10/P08 (migration) mexem no mesmo arquivo (`Entities.kt`, `FioDatabase.kt`, migrations): nunca em paralelo; um packet de migration por vez.
2. P13 (índice vetorial) e P06 (scale lexical) mexem no `search/`: em paralelo é possível, mas o Atlas deve ser relido antes.
3. P02 (privacy surfaces) e P09 (FLAG_SECURE) tocam as mesmas superfícies UI: sequenciar, P09 depois de P02.
4. Qualquer packet + pilot ativo: o piloto congela feature changes na superfície Return durante a janela de coleta (ADR-016: within-participant).

## 4. Regras de paralelismo

Máximo 2 packets de código simultâneos (branches de integração distintas); packets device/H sempre exclusivos (o aparelho é único recurso); packets de decisão (FIO-PQ-03) não consomem recursos técnicos e podem correr com qualquer outro.

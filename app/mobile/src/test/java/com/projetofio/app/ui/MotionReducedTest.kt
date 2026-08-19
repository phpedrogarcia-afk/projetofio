package com.projetofio.app.ui

/**
 * G9 — o helper `isMotionReduced` é privado ao arquivo FioApp.kt por design
 * (nenhum outro ponto da UI depende dele hoje). A validação comportamental
 * (reduce motion + TalkBack suprimem a Pátina) é um gate humano em AVD,
 * documentado no PILOT-PROTOCOL e no MOTION-SYSTEM.md — não há como
 * exercitá-la de forma confiável em teste unitário Robolectric sem expor
 * a API. Este arquivo permanece como stub intencional para não forçar
 * internal@file→public só para cobrir linha.
 *
 * Se um futuro composable precisar do mesmo check, extraí-lo para
 * `internal fun isMotionReduced` em um objeto acessível a testes.
 */
object MotionReducedTest

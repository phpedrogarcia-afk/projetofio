package com.projetofio.app.ui

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regressão FIO-PV-02: Material DatePicker codifica o dia civil como meia-noite
 * UTC. Interpretar esse valor no fuso do aparelho pode fazer o cabeçalho e o
 * valor confirmado mostrarem o dia anterior ao que permanece selecionado.
 */
class DatePickerZoneTest {

    @Test
    fun `picker contract round-trips the selected day independently of device zone`() {
        val chosenDate = LocalDate.of(2026, 8, 20)
        val pickerMillis = datePickerMillis(chosenDate)

        assertEquals(chosenDate, datePickerDate(pickerMillis))
        assertEquals(
            chosenDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            pickerMillis,
        )
    }

    @Test
    fun `old local-zone decoding reproduces the previous-day disagreement`() {
        val chosenDate = LocalDate.of(2027, 3, 15)
        val pickerMillis = datePickerMillis(chosenDate)
        val saoPaulo = ZoneId.of("America/Sao_Paulo")

        val oldHeaderDate = java.time.Instant.ofEpochMilli(pickerMillis)
            .atZone(saoPaulo)
            .toLocalDate()

        assertEquals(LocalDate.of(2027, 3, 14), oldHeaderDate)
        assertEquals(chosenDate, datePickerDate(pickerMillis))
    }
}

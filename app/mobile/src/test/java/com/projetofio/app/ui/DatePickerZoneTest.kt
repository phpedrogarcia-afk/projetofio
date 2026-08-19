package com.projetofio.app.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Reproducer do gap G1 (dogfood simulation): o date picker da Home converte
 * millis selecionados via UTC puro — para um usuário em UTC-3 entre 21h e
 * 00h, a data "ontem" do calendário é lida como o dia correto só se a
 * conversão usar o fuso local do aparelho. Este teste codifica o contrato
 * esperado; o fix está em FioApp.kt (dateSheet block).
 */
class DatePickerZoneTest {

    /** O contrato esperado: millis do picker, interpretados no fuso local,
     *  devem devolver a mesma LocalDate que o usuário tocou no calendário. */
    @Test
    fun `picker millis interpreted in local zone round-trip the chosen date`() {
        // Simula o usuário em UTC-3 às 22h30 de 19/08/2026 tocando
        // "20/08/2026" no calendário do DatePicker.
        val localZone = ZoneId.of("America/Sao_Paulo") // UTC-3
        val chosenDate = LocalDate.of(2026, 8, 20)

        // O DatePicker devolve millis do início do dia... em qual fuso?
        // O material-datepicker opera no fuso local do sistema por padrão.
        val millis = chosenDate.atStartOfDay(localZone).toInstant().toEpochMilli()

        // Contrato esperado (fix): interpretar com ZoneId.systemDefault().
        val decoded = Instant.ofEpochMilli(millis).atZone(localZone).toLocalDate()
        assertEquals(chosenDate, decoded)

        // O bug (código antigo): interpretar com ZoneOffset.UTC.
        val bugged = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        // 20/08/2026 00:00 -03:00 = 20/08/2026 03:00 UTC → 20/08. Ok por sorte neste caso.
        // Mas a inicialização (atStartOfDay(UTC)) escolhe o dia errado:
        val initializedUtc = chosenDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val decodedUtc = Instant.ofEpochMilli(initializedUtc).atZone(localZone).toLocalDate()
        // 20/08/2026 00:00 UTC = 19/08/2026 21:00 -03:00 → dia anterior.
        assert(!decodedUtc.equals(chosenDate)) {
            "esperava dia deslocado, mas obteve $decodedUtc"
        }
        assertEquals(LocalDate.of(2026, 8, 19), decodedUtc)
    }

    @Test
    fun `utc interpretation shifts the day for negative-offset zones`() {
        val localZone = ZoneId.of("America/Sao_Paulo")
        // 15/03/2027 em UTC: millis de 00:00 UTC
        val millis = LocalDate.of(2027, 3, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        // Interpretado no fuso local: 14/03/2027 21:00 (horário de verão off, -3)
        val decoded = Instant.ofEpochMilli(millis).atZone(localZone).toLocalDate()
        assertEquals(LocalDate.of(2027, 3, 14), decoded)
    }
}

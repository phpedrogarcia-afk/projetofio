package com.projetofio.app.domain

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HypothesisCheckTest {
    @Test
    fun `verify UTF-8 semantics of Kotlin strings with surrogate halves and ZW chars`() {
        val half = "start\uD800end"
        val bytes = half.toByteArray(StandardCharsets.UTF_8)
        val back = String(bytes, StandardCharsets.UTF_8)
        println("SURROGATE roundtrip equal: ${half == back}")
        println("SURROGATE bytes: ${bytes.joinToString(" ") { "%02x".format(it) }}")
        println("SURROGATE back string: $back")
        org.junit.Assert.assertFalse("ZWSP is empty: ${"\u200B".isEmpty()}", "\u200B".isEmpty())
        org.junit.Assert.assertFalse("ZWSP isWhitespace: ${'\u200B'.isWhitespace()}", '\u200B'.isWhitespace())
        println("ZWSP isWhitespace: ${'\u200B'.isWhitespace()}")
        println("ZWSP isBlank string: ${"\u200B\u200E".isBlank()}")
        println("ZWSP+NBSP isBlank: ${"\u200B\u200E\u00A0".isBlank()}")
        // String.codePointCount vs length
        println("half length=${half.length} codePointCount=${half.codePointCount(0, half.length)}")
    }
}

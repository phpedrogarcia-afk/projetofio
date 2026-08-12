package com.projetofio.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectContractTest {
    @Test
    fun approvedApplicationIdIsStable() {
        assertEquals("com.projetofio.app", MainActivity::class.java.packageName)
    }
}

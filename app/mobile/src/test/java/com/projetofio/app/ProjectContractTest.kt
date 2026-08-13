package com.projetofio.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectContractTest {
    @Test
    fun approvedApplicationIdIsStable() {
        assertEquals("com.projetofio.app", MainActivity::class.java.packageName)
    }

    @Test
    fun timeReturnsActivationIsRestrictedToValidationBuild() {
        if (BuildConfig.BUILD_TYPE == "validation") {
            assertTrue(BuildConfig.TIME_RETURNS_ENGINEERING_ENABLED)
        } else {
            assertFalse(BuildConfig.TIME_RETURNS_ENGINEERING_ENABLED)
        }
    }

    @Test
    fun localImportActivationIsRestrictedToValidationBuild() {
        if (BuildConfig.BUILD_TYPE == "validation") {
            assertTrue(BuildConfig.LOCAL_IMPORT_ENGINEERING_ENABLED)
        } else {
            assertFalse(BuildConfig.LOCAL_IMPORT_ENGINEERING_ENABLED)
        }
    }
}

package com.nmm.objectdetectionapp

import com.nmm.objectdetectionapp.model.MyModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MyModelTest {

    @Test
    fun `test MyModel properties`() {
        val model = MyModel(label = "TestLabel", confidence = 0.9f)

        assertEquals("TestLabel", model.label)
        assertEquals(0.9f, model.confidence, 0.0f)
    }
}

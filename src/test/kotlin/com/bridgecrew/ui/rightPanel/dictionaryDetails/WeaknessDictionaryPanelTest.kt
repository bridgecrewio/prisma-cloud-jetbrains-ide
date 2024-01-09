package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.fixtures.*
import com.bridgecrew.fixtures.metadataTaintModeJson
import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.SystemIndependent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WeaknessDictionaryPanelTest {
    private val rootDisposable = Disposer.newDisposable()
    private val project: MockProject = createProject("/tmp/foo")

    private fun createProject(basePath: String): MockProject {
        return object : MockProject(null, rootDisposable) {
            override fun getBasePath(): @SystemIndependent String? {
                return basePath
            }
        }
    }

    @Nested
    inner class ExtractDataFlow{
        @Test
        fun `should return empty string WHEN metadata is not provided`() {
            val checkovResult = createWeaknessCheckovResult(metadataAbsent)
            val panel = WeaknessDictionaryPanel(checkovResult, project)

            assertEquals(panel.fieldsMap["Data flow"], null)
        }

        @Test
        fun `should return empty string WHEN metadata is empty object`() {
            val checkovResult = createWeaknessCheckovResult(metadataEmptyJson)
            val panel = WeaknessDictionaryPanel(checkovResult, project)

            assertEquals(panel.fieldsMap["Data flow"], null)
        }

        @Test
        fun `should return Data flow WHEN taint_mode is provided `() {
            val checkovResult = createWeaknessCheckovResult(metadataTaintModeJson)
            val panel = WeaknessDictionaryPanel(checkovResult, project)

            assertEquals(panel.fieldsMap["Data flow"], "2 steps in 1 file(s)")
        }

        @Test
        fun `should return Data flow WHEN code_locations is provided `() {
            val checkovResult = createWeaknessCheckovResult(metadataCodeCollectionJson)
            val panel = WeaknessDictionaryPanel(checkovResult, project)

            assertEquals(panel.fieldsMap["Data flow"], "2 steps in 2 file(s)")
        }
    }

}

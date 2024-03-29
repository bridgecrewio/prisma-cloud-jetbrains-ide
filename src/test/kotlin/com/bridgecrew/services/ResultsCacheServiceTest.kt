package com.bridgecrew.services

import com.bridgecrew.fixtures.createSastCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.CheckType
import com.bridgecrew.fixtures.*
import com.bridgecrew.results.Severity
import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.SystemIndependent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ResultsCacheServiceTest {
    private val rootDisposable = Disposer.newDisposable()
    private val project: MockProject = createProject("/tmp/foo")

    private fun createProject(basePath: String): MockProject {
        return object : MockProject(null, rootDisposable) {
            override fun getBasePath(): @SystemIndependent String? {
                return basePath
            }
        }
    }

    @Test
    fun `setCheckovResultsFromResultsList should set WeaknessCheckovResult`() {
        val resultsCacheService = ResultsCacheService(project)

        val checkovResult = createSastCheckovResult()
        resultsCacheService.setCheckovResultsFromResultsList(listOf(checkovResult));

        assertEquals(resultsCacheService.checkovResults.size, 1)
        assertEquals(resultsCacheService.checkovResults[0].checkType, CheckType.SAST)
        assertEquals(resultsCacheService.checkovResults[0].category, Category.WEAKNESSES)
        assertEquals(resultsCacheService.checkovResults[0].severity, Severity.MEDIUM)
    }

    @Test
    fun `setCheckovResultsFromResultsList should set WeaknessCheckovResult cdk framework`() {
        val resultsCacheService = ResultsCacheService(project)

        val checkovResult = createCdkCheckovResult()
        resultsCacheService.setCheckovResultsFromResultsList(listOf(checkovResult));

        assertEquals(resultsCacheService.checkovResults.size, 1)
        assertEquals(resultsCacheService.checkovResults[0].checkType, CheckType.CDK)
        assertEquals(resultsCacheService.checkovResults[0].category, Category.WEAKNESSES)
        assertEquals(resultsCacheService.checkovResults[0].severity, Severity.INFO)
    }

    @Test
    fun `mapCheckovCheckTypeToScanType should return proper values`() {
        val resultsCacheService = ResultsCacheService(project)
        val method = resultsCacheService::class.java.getDeclaredMethod("mapCheckovCheckTypeToScanType", String::class.java, String::class.java)

        method.setAccessible(true)

        assertEquals(Category.IAC, method.invoke(resultsCacheService, "ansible", ""))
        assertEquals(Category.SECRETS, method.invoke(resultsCacheService, "secrets", ""))
        assertEquals(Category.LICENSES, method.invoke(resultsCacheService, "sca_package", "BC_LIC"))
        assertEquals(Category.VULNERABILITIES, method.invoke(resultsCacheService, "sca_package", "BC_VUL"))
        assertEquals(Category.WEAKNESSES, method.invoke(resultsCacheService, "sast_java", "BC_VUL"))
        assertEquals(Category.WEAKNESSES, method.invoke(resultsCacheService, "cdk_python", "BC_VUL"))
    }

    @Test
    fun `getCheckType should return proper values`() {
        val resultsCacheService = ResultsCacheService(project)
        val method = resultsCacheService::class.java.getDeclaredMethod("getCheckType", String::class.java)

        method.setAccessible(true)

        assertEquals(CheckType.SAST, method.invoke(resultsCacheService, "sast_java"))
        assertEquals(CheckType.CDK, method.invoke(resultsCacheService, "cdk_python"))
        assertEquals(CheckType.SCA_IMAGE, method.invoke(resultsCacheService, "sca_image"))
        assertEquals(CheckType.DOCKERFILE, method.invoke(resultsCacheService, "dockerfile"))
    }
}
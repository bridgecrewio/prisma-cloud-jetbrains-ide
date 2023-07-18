package com.bridgecrew.analytics

import com.bridgecrew.api.ApiClient
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.services.scan.ScanTaskResult
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import com.bridgecrew.settings.PrismaSettingsState
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Service
@OptIn(ExperimentalSerializationApi::class)
class AnalyticsService(val project: Project) {

    private val LOG = logger<AnalyticsService>()
    private var apiClient: ApiClient? = null

    private var fullScanData: FullScanAnalyticsData? = null
    private var fullScanNumber = 0

    private var analyticsEventData: MutableList<AnalyticsData> = arrayListOf()

    var wereFullScanResultsDisplayed = false
    var wereSingleFileScanResultsDisplayed = false
    val wereResultsDisplayed
        get() = (wereSingleFileScanResultsDisplayed || wereFullScanResultsDisplayed)

    fun fullScanButtonWasPressed() {
        val fullScanButtonWasPressedDate = Date()
        fullScanNumber += 1
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan button was pressed")
        fullScanData = FullScanAnalyticsData(fullScanNumber)
        fullScanData!!.buttonPressedTime = fullScanButtonWasPressedDate
    }

    fun fullScanStarted() {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan started")
        fullScanData!!.scanStartedTime = Date()
    }

    fun fullScanByFrameworkStarted(framework: String) {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan started for framework $framework")
        fullScanData!!.frameworksScanTime[framework] = FullScanFrameworkScanTimeData(Date())
    }

    fun fullScanByFrameworkFinished(framework: String) {
        fullScanData!!.frameworksScanTime[framework]!!.endTime = Date()
        fullScanData!!.frameworksScanTime[framework]!!.totalTimeSeconds = (fullScanData!!.frameworksScanTime[framework]!!.endTime.time - fullScanData!!.frameworksScanTime[framework]!!.startTime.time) / 1000
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan finished for framework $framework and took ${fullScanData!!.frameworksScanTime[framework]!!.totalTimeSeconds} ms")
    }

    fun fullScanFinished() {
        fullScanData!!.scanFinishedTime = Date()
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan finished")
        buildFullScanAnalyticsData()
        releaseStatistics()
    }

    private fun buildFullScanAnalyticsData(){
        val analyticsData = AnalyticsData()
        analyticsData.eventData = fullScanData!!.frameworksScanTime
        analyticsData.eventTime = fullScanData!!.buttonPressedTime
        analyticsData.eventType = EventTypeEnum.ON_FULL_SCAN
        analyticsEventData.add(analyticsData)
    }

    fun fullScanFrameworkFinishedNoErrors(framework: String) {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - framework $framework finished with no errors")
    }

    fun fullScanResultsWereFullyDisplayed() {
        if (fullScanData!!.isFullScanFinished()) {
            fullScanData!!.resultsWereFullyDisplayedTime = Date()
            LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan results are fully displayed")
            logFullScanAnalytics()
            wereFullScanResultsDisplayed = true
        }
    }

    fun singleFileScanResultsWereFullyDisplayed() {
        wereSingleFileScanResultsDisplayed = true
    }

    fun fullScanFrameworkError(framework: String) {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - error while scanning framework $framework")
    }

    fun fullScanFrameworkDetectedVulnerabilities(framework: String, numberOfVulnerabilities: Int) {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - $numberOfVulnerabilities security issues were detected while scanning framework $framework")
    }

    fun fullScanParsingError(framework: String, failedFilesSize: Int) {
        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - parsing error while scanning framework $framework in $failedFilesSize files}")
    }

    private fun logFullScanAnalytics() {
        var maximumScanFramework = 0L
        var minimumScanFramework = 0L
        var maximumFramework = ""
        var minimumFramework = ""
        fullScanData!!.frameworksScanTime.forEach { framework ->
            if (framework.value.totalTimeSeconds >= maximumScanFramework) {
                maximumScanFramework = framework.value.totalTimeSeconds
                maximumFramework = framework.key
                if (minimumFramework.isEmpty()) {
                    minimumScanFramework = framework.value.totalTimeSeconds
                    minimumFramework = framework.key
                }
            }

            if (framework.value.totalTimeSeconds <= minimumScanFramework) {
                minimumScanFramework = framework.value.totalTimeSeconds
                minimumFramework = framework.key
            }
        }

        val dateFormatter = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        val frameworkScansFinishedWithErrors: MutableMap<String, ScanTaskResult> = project.service<FullScanStateService>().frameworkScansFinishedWithErrors

        LOG.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan analytics:\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.buttonPressedTime, fullScanData!!.resultsWereFullyDisplayedTime)} minutes from pressing on the scan button to fully display the results\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.scanStartedTime, fullScanData!!.scanFinishedTime)} minutes from starting Prisma Cloud scans and finishing Prisma Cloud scans for all frameworks\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.buttonPressedTime, fullScanData!!.scanStartedTime)} minutes from pressing on the scan button to starting Prisma Cloud scan\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.scanFinishedTime, fullScanData!!.resultsWereFullyDisplayedTime)} minutes from finishing Prisma Cloud scans for all frameworks to fully display the results\n" +
                "framework scan $maximumFramework took the most - ${formatTimeAsString(fullScanData!!.frameworksScanTime[maximumFramework]!!.startTime, fullScanData!!.frameworksScanTime[maximumFramework]!!.endTime)} minutes\n" +
                "framework scan $minimumFramework took the least - ${formatTimeAsString(fullScanData!!.frameworksScanTime[minimumFramework]!!.startTime, fullScanData!!.frameworksScanTime[minimumFramework]!!.endTime)} minutes\n" +
                "${frameworkScansFinishedWithErrors.size} frameworks was finished with errors: ${frameworkScansFinishedWithErrors.keys}\n" +
                "frameworks scans:\n" +
                "${fullScanData!!.frameworksScanTime.map { (framework, scanResults) ->
                        "framework $framework took ${formatTimeAsString(scanResults.startTime, scanResults.endTime)} minutes to be scanned\n" }
                }\n" +
                "full scan button pressed on ${dateFormatter.format(fullScanData!!.buttonPressedTime)}\n" +
                "full scan started on ${dateFormatter.format(fullScanData!!.scanStartedTime)}\n" +
                "full scan finished on ${dateFormatter.format(fullScanData!!.scanFinishedTime)}\n" +
                "full scan results displayed on ${dateFormatter.format(fullScanData!!.resultsWereFullyDisplayedTime)}\n"
        )
    }

    private fun formatTimeAsString(startTime: Date, endTime: Date): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(endTime.time - startTime.time)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(endTime.time - startTime.time) - (minutes * 60)
        val secondsString = if (seconds < 10) {
            "0${seconds}"
        } else "$seconds"
        return "${minutes}:${secondsString}"
    }

    private fun releaseStatistics() {
        val apiClient = getApiClient()
        //todo maybe extends with pluginInstallationId
        val isReleased = apiClient.putDataAnalytics(Json.encodeToString(analyticsEventData))
        if(isReleased){
            analyticsEventData.clear()
        }
    }

    private fun getApiClient(): ApiClient {
        if (this.apiClient != null) {
            return this.apiClient!!
        }

        val settings = PrismaSettingsState().getInstance()
        if (settings?.accessKey!!.isNotEmpty() && settings.secretKey.isNotEmpty() && settings.prismaURL.isNotEmpty()) {
            this.apiClient = ApiClient(settings.accessKey, settings.secretKey, settings.prismaURL)
            return this.apiClient!!
        }

        throw Exception("Prisma could parameters: accessKey, secretKey, prismaURL have not been initialized!")
    }

    @Serializable
    data class AnalyticsData(@EncodeDefault val pluginName: String = "jetbrains"){
        //todo implement - get real installationId after plugin will be installed
        @EncodeDefault
        val installationId: String = "installationId"

        lateinit var eventType: String

        @Serializable(with = DateSerializer::class)
        lateinit var eventTime: Date

        lateinit var eventData: MutableMap<String, FullScanFrameworkScanTimeData>
    }

    @Serializable
    data class FullScanAnalyticsData(val scanNumber: Int) {
        @Serializable(with = DateSerializer::class)
        lateinit var buttonPressedTime: Date

        @Serializable(with = DateSerializer::class)
        @Transient
        lateinit var scanStartedTime: Date
        val frameworksScanTime: MutableMap<String, FullScanFrameworkScanTimeData> = mutableMapOf()

        @Serializable(with = DateSerializer::class)
        @Transient
        lateinit var scanFinishedTime: Date

        @Serializable(with = DateSerializer::class)
        @Transient
        lateinit var resultsWereFullyDisplayedTime: Date

        fun isFullScanFinished() = ::scanFinishedTime.isInitialized
        fun isFullScanStarted() = ::scanStartedTime.isInitialized
    }

    @Serializable
    data class FullScanFrameworkScanTimeData(
            @Serializable(with = DateSerializer::class)
            val startTime: Date
    ) {
        @Serializable(with = DateSerializer::class)
        var endTime: Date = Date()
        var totalTimeSeconds = 0L
    }
}
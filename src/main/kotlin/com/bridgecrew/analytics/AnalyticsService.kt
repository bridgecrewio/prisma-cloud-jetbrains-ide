package com.bridgecrew.analytics

import com.bridgecrew.api.PrismaApiClient
import com.bridgecrew.cache.CacheDataAnalytics
import com.bridgecrew.scheduler.IntervalRunner
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.services.scan.ScanTaskResult
import com.bridgecrew.utils.ApplicationServiceUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// TODO:
//  This service should be an application level service, but it requires a major refactor with how it handles
//  FullScanStateService and CacheDataAnalytics on a project level
@Service(Service.Level.PROJECT)
class AnalyticsService(val project: Project) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val analyticsReleaseTask = IntervalRunner("Analytics")

    private var fullScanData: FullScanAnalyticsData? = null
    private var fullScanNumber = 0

    private var analyticsEventData: MutableList<AnalyticsData> = mutableListOf()

    var wereFullScanResultsDisplayed = false
    var wereSingleFileScanResultsDisplayed = false
    val wereResultsDisplayed
        get() = (wereSingleFileScanResultsDisplayed || wereFullScanResultsDisplayed)

    fun fullScanButtonWasPressed() {
        val fullScanButtonWasPressedDate = Date()
        fullScanNumber += 1
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan button was pressed")
        fullScanData = FullScanAnalyticsData(fullScanNumber)
        fullScanData!!.buttonPressedTime = fullScanButtonWasPressedDate
    }

    fun fullScanStarted() {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan started")
        fullScanData!!.scanStartedTime = Date()
    }

    fun fullScanByFrameworkStarted(framework: String) {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan started for framework $framework")
        fullScanData!!.frameworksScanTime[framework] = FullScanFrameworkScanTimeData()
    }

    fun fullScanByFrameworkFinished(framework: String) {
        fullScanData!!.frameworksScanTime[framework]!!.endTime = Date()
        fullScanData!!.frameworksScanTime[framework]!!.totalTimeSeconds = (fullScanData!!.frameworksScanTime[framework]!!.endTime.time - fullScanData!!.frameworksScanTime[framework]!!.startTime.time) / 1000
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan finished for framework $framework and took ${fullScanData!!.frameworksScanTime[framework]!!.totalTimeSeconds} ms")
    }

    fun fullScanFinished() {
        fullScanData!!.scanFinishedTime = Date()
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan finished")
        buildFullScanAnalyticsData()
    }

    fun fullScanFrameworkFinishedNoErrors(framework: String) {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - framework $framework finished with no errors")
    }

    fun fullScanResultsWereFullyDisplayed() {
        if (fullScanData!!.isFullScanFinished()) {
            fullScanData!!.resultsWereFullyDisplayedTime = Date()
            logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan results are fully displayed")
            logFullScanAnalytics()
            wereFullScanResultsDisplayed = true
        }
    }

    fun singleFileScanResultsWereFullyDisplayed() {
        wereSingleFileScanResultsDisplayed = true
    }

    fun fullScanFrameworkError(framework: String) {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - error while scanning framework $framework")
    }

    fun fullScanFrameworkDetectedVulnerabilities(framework: String, numberOfVulnerabilities: Int) {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - $numberOfVulnerabilities security issues were detected while scanning framework $framework")
    }

    fun fullScanParsingError(framework: String, failedFilesSize: Int) {
        logger.info("Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - parsing error while scanning framework $framework in $failedFilesSize files}")
    }

    fun pluginInstalled(){
        buildPluginInstalledAnalyticsData()
    }

    fun pluginUninstalled(){
        buildPluginUninstalledAnalyticsData()
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

        logger.info(
            "Prisma Cloud Plugin Analytics - scan #${fullScanNumber} - full scan analytics:\n" +
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

    fun stopAnalyticsService() {
        logger.info("Analytics service for project ${project.name} is stopping, releasing all analytics")
        analyticsReleaseTask.stop()
        releaseAnalytics()
    }

    private fun releaseAnalytics() {
        val apiClient = ApplicationServiceUtil.getService(PrismaApiClient::class.java) ?: return
        if (analyticsEventData.isEmpty()) {
            return
        }
        val isReleased = apiClient.putDataAnalytics(analyticsEventData)
        if (isReleased) {
            analyticsEventData.clear()
        }

        CacheDataAnalytics(project).stash(analyticsEventData)
    }

    fun startSchedulerReleasingAnalytics(){
        val apiClient = ApplicationServiceUtil.getService(PrismaApiClient::class.java) ?: return
        val config = apiClient.getConfig()
        CacheDataAnalytics(project).load(analyticsEventData)
        analyticsReleaseTask.scheduleWithTimer({ releaseAnalytics() }, config?.reportingInterval ?: 300)
    }

    private fun buildFullScanAnalyticsData(){
        fullScanData!!.eventData = fullScanData!!.frameworksScanTime
        fullScanData!!.eventTime = fullScanData!!.buttonPressedTime
        fullScanData!!.eventType = EventTypeEnum.ON_FULL_SCAN
        analyticsEventData.add(fullScanData!!)
    }

     private fun buildPluginInstalledAnalyticsData(){
        val analyticsData = AnalyticsData()
        analyticsData.eventTime = Date()
        analyticsData.eventType = EventTypeEnum.ON_PLUGIN_INSTALL
        analyticsEventData.add(analyticsData)
    }

    private fun buildPluginUninstalledAnalyticsData(){
        val analyticsData = AnalyticsData()
        analyticsData.eventTime = Date()
        analyticsData.eventType = EventTypeEnum.ON_PLUGIN_UNINSTALL
        analyticsEventData.add(analyticsData)
    }
}

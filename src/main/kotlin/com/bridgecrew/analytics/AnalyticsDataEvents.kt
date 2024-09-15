package com.bridgecrew.analytics

import com.bridgecrew.cache.InMemCache
import com.bridgecrew.settings.PLUGIN_NAME
import com.bridgecrew.settings.PrismaSettingsState
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId
import java.util.*

open class AnalyticsData {

    val pluginName: String = PLUGIN_NAME

    val installationId: String = PrismaSettingsState().getInstance()!!.installationId

    val pluginVersion: String? = PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.prismacloud"))?.version

    val ideVersion: String = ApplicationInfo.getInstance().fullApplicationName + " / " + ApplicationInfo.getInstance().build

    val operatingSystem: String = System.getProperty("os.name") + " " + System.getProperty("os.version")

    val checkovVersion: String? = InMemCache.get("checkovVersion")

    lateinit var eventType: String

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ", locale = "en")
    lateinit var eventTime: Date

    open var eventData: MutableMap<String, *> = mutableMapOf<String, Any>()

}

data class FullScanAnalyticsData(@JsonIgnore val scanNumber: Int = 0): AnalyticsData() {

    @JsonIgnore
    lateinit var buttonPressedTime: Date

    @JsonIgnore
    lateinit var scanStartedTime: Date

    @JsonIgnore
    val frameworksScanTime: MutableMap<String, FullScanFrameworkScanTimeData> = mutableMapOf()

    @JsonIgnore
    lateinit var scanFinishedTime: Date

    @JsonIgnore
    lateinit var resultsWereFullyDisplayedTime: Date

    @JsonIgnore
    fun isFullScanFinished() = ::scanFinishedTime.isInitialized

    @JsonIgnore
    fun isFullScanStarted() = ::scanStartedTime.isInitialized
}

class FullScanFrameworkScanTimeData {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ", locale = "en")
    val startTime: Date = Date()

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ", locale = "en")
    var endTime: Date = Date()

    var totalTimeSeconds = 0L
}

package com.bridgecrew.analytics

import com.bridgecrew.cache.InMemCache
import com.bridgecrew.settings.PLUGIN_NAME
import com.bridgecrew.settings.PrismaSettingsState
import com.google.gson.annotations.Expose
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import java.util.*


@OptIn(ExperimentalSerializationApi::class)
@Serializable
sealed class AnalyticsData(@EncodeDefault val pluginName: String = PLUGIN_NAME) {
    
    @EncodeDefault
    val installationId: String = PrismaSettingsState().getInstance()!!.installationId

    @EncodeDefault
    var pluginVersion: String? =
        PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.prismacloud"))?.version

    @EncodeDefault
    var ideVersion: String? =
        ApplicationInfo.getInstance().fullApplicationName + " / " + ApplicationInfo.getInstance().build

    @EncodeDefault
    var operatingSystem: String? = System.getProperty("os.name") + " " + System.getProperty("os.version")

    @EncodeDefault
    var checkovVersion: String? = InMemCache.get("checkovVersion")

    @Serializable
    lateinit var eventType: String

    @Serializable(with = DateSerializer::class)
    lateinit var eventTime: Date

    abstract val eventData: Any

}

@Serializable
data class FullScanAnalyticsData(@Transient val scanNumber: Int = 0): AnalyticsData() {
    @Transient
    lateinit var buttonPressedTime: Date

    @Transient
    lateinit var scanStartedTime: Date

    @Transient
    val frameworksScanTime: MutableMap<String, FullScanFrameworkScanTimeData> = mutableMapOf()

    @Expose
    override lateinit var eventData: MutableMap<String, FullScanFrameworkScanTimeData>

    @Transient
    lateinit var scanFinishedTime: Date

    @Transient
    lateinit var resultsWereFullyDisplayedTime: Date

    fun isFullScanFinished() = ::scanFinishedTime.isInitialized
    fun isFullScanStarted() = ::scanStartedTime.isInitialized
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PluginInstallAnalyticsData(
    @EncodeDefault override val eventData: JsonObject = JsonObject(mapOf())
) : AnalyticsData()

@Serializable
data class FullScanFrameworkScanTimeData(
        @Serializable(with = DateSerializer::class)
        val startTime: Date
) {
    @Serializable(with = DateSerializer::class)
    var endTime: Date = Date()
    var totalTimeSeconds = 0L
}

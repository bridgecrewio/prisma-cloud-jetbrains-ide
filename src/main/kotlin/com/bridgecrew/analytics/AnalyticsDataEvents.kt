package com.bridgecrew.analytics

import com.bridgecrew.settings.PrismaSettingsState
import com.google.gson.annotations.Expose
import kotlinx.serialization.*
import java.util.*
import kotlinx.serialization.json.JsonObject


@OptIn(ExperimentalSerializationApi::class)
@Serializable
sealed class AnalyticsData(@EncodeDefault val pluginName: String = "jetbrains"){
    @EncodeDefault
    val installationId: String = PrismaSettingsState().getInstance()!!.installationId

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
        @EncodeDefault
        override val eventData: JsonObject = JsonObject(mapOf())
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
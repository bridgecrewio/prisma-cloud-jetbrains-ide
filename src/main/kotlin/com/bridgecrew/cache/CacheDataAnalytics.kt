package com.bridgecrew.cache


import com.bridgecrew.analytics.AnalyticsData
import com.bridgecrew.api.mapper
import com.intellij.openapi.project.Project

class CacheDataAnalytics(private val project: Project) {

    fun load(analyticsEventData: MutableList<AnalyticsData>) {
        val data = CacheDataAnalyticsStorage(project).readDataFromFile()
        if(data.isNullOrEmpty()){
            return
        }

        val analyticsEventDataCached = data.split("\n").toMutableList().map {  mapper.readValue(it, AnalyticsData::class.java) }
        analyticsEventData.addAll(analyticsEventDataCached)

        CacheDataAnalyticsStorage(project).clear()
    }

    fun stash(analyticsEventData: MutableList<AnalyticsData>) {
        val data = analyticsEventData.joinToString("\n") { mapper.writeValueAsString(it) }
        CacheDataAnalyticsStorage(project).writeDataToFile(data)
    }
}

package com.bridgecrew.cache

import com.intellij.openapi.project.Project

class CacheDataAnalytics(private val project: Project) {

    fun load(analyticsEventData: MutableList<String>) {
        val data = CacheDataAnalyticsStorage(project).readDataFromFile()
        if(data.isNullOrEmpty()){
            return
        }

        val analyticsEventDataCached = data.split("\n").toMutableList()
        analyticsEventData.addAll(analyticsEventDataCached)

        CacheDataAnalyticsStorage(project).clear()
    }

    fun stash(analyticsEventData: MutableList<String>) {
        val data = analyticsEventData.joinToString("\n")
        CacheDataAnalyticsStorage(project).writeDataToFile(data)
    }
}

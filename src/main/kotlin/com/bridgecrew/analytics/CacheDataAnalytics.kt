package com.bridgecrew.analytics

import com.bridgecrew.settings.PrismaSettingsState

class CacheDataAnalytics {
    fun load(analyticsEventData: MutableList<String>){
        val settings = PrismaSettingsState().getInstance()
        if(settings!!.analyticsEventData.isEmpty()){
            return
        }
        analyticsEventData.addAll(settings.analyticsEventData)
        settings.analyticsEventData.clear()
    }

    fun stash(analyticsEventData: MutableList<String>){
        val settings = PrismaSettingsState().getInstance()
        settings!!.analyticsEventData = analyticsEventData
    }
}
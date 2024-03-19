package com.bridgecrew.settings

import com.bridgecrew.utils.FULL_SCAN_RERO_LIMIT
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

const val PLUGIN_NAME = "jetbrains"
// represents the time interval in seconds between sending statistic data from the client to the backend
const val DEFAULT_REPORTING_INTERVAL = 5 * 60;


@State(
    name = "com.bridgecrew.settings.PrismaSettingsState",
    storages = [Storage("PrismaSettingsState.xml")]
)
class PrismaSettingsState : PersistentStateComponent<PrismaSettingsState> {

    var accessKey: String = ""
    var secretKey: String = ""
    var certificate: String = ""
    var prismaURL: String = ""
    var fullScanRepoLimit: Int = FULL_SCAN_RERO_LIMIT
    var installationId: String = ""
        set(value) {
            if (field.isNotEmpty()) {
                throw IllegalStateException("The installationId was already set.")
            } else {
                field = value
            }
        }

    fun getApiKey(): String {
        if(accessKey.isNotEmpty() && secretKey.isNotEmpty()){
            return "$accessKey::$secretKey"
        }

        return ""
    }

    fun getInstance(): PrismaSettingsState? {
        return ApplicationManager.getApplication().getService(PrismaSettingsState::class.java)
    }

    fun isConfigured(): Boolean{
        return accessKey.isNotEmpty() && secretKey.isNotEmpty() && prismaURL.isNotEmpty()
    }

    override fun getState(): PrismaSettingsState = this

    override fun loadState(state: PrismaSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
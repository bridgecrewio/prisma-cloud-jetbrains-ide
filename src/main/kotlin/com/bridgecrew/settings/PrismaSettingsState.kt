package com.bridgecrew.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


@State(
    name = "com.bridgecrew.settings.PrismaSettingsState",
    storages = [Storage("PrismaSettingsState.xml")]
)
class PrismaSettingsState : PersistentStateComponent<PrismaSettingsState> {

    var accessKey: String = ""
    var secretKey: String = ""
    var certificate: String = ""
    var prismaURL: String = ""
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

    override fun getState(): PrismaSettingsState = this

    override fun loadState(state: PrismaSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
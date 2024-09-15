package com.bridgecrew.settings

import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.ui.PrismaSettingsComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class PrismaSettingsConfigurable : Configurable {

    private val prismaSettingsComponent = PrismaSettingsComponent(this)

    override fun getDisplayName(): String = "Checkov"

    override fun createComponent(): JComponent {
        return prismaSettingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        val settings = PrismaSettingsState().getInstance()
        return !prismaSettingsComponent.accessKeyField.text.equals(settings?.accessKey) ||
                !prismaSettingsComponent.secretKeyField.text.equals(settings?.secretKey) ||
                !prismaSettingsComponent.certificateField.text.equals(settings?.certificate) ||
                !prismaSettingsComponent.prismaURLField.text.equals(settings?.prismaURL) ||
                !prismaSettingsComponent.fullScanRepoLimitField.value.equals(settings?.fullScanRepoLimit)
    }

    override fun apply() {

        if (!prismaSettingsComponent.isValid()) {
            return
        }
        val settings = PrismaSettingsState().getInstance()

        val secretKeyModified = !prismaSettingsComponent.accessKeyField.text.equals(settings?.accessKey)
        val accessKeyModified = !prismaSettingsComponent.secretKeyField.text.equals(settings?.secretKey)
        val prismaURLModified = !prismaSettingsComponent.prismaURLField.text.equals(settings?.prismaURL)
        val fullScanRepoLimitModified = !prismaSettingsComponent.fullScanRepoLimitField.value.equals(settings?.fullScanRepoLimit)

        settings?.secretKey = prismaSettingsComponent.secretKeyField.text.trim()
        settings?.accessKey = prismaSettingsComponent.accessKeyField.text.trim()
        settings?.certificate = prismaSettingsComponent.certificateField.text.trim()
        settings?.prismaURL = prismaSettingsComponent.prismaURLField.text.trim()
        settings?.fullScanRepoLimit = prismaSettingsComponent.fullScanRepoLimitField.text.toInt()

        if (accessKeyModified || secretKeyModified || prismaURLModified || fullScanRepoLimitModified) {
            ApplicationManager.getApplication().messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC)
                .settingsUpdated()
        }
    }

    override fun reset() {
        val setting = PrismaSettingsState().getInstance()
        prismaSettingsComponent.accessKeyField.text = setting?.accessKey
        prismaSettingsComponent.secretKeyField.text = setting?.secretKey
        prismaSettingsComponent.certificateField.text = setting?.certificate
        prismaSettingsComponent.prismaURLField.text = setting?.prismaURL
        prismaSettingsComponent.fullScanRepoLimitField.value = setting?.fullScanRepoLimit
    }
}

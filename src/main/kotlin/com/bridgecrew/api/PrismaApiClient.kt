package com.bridgecrew.api

import com.bridgecrew.analytics.AnalyticsData
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.DEFAULT_REPORTING_INTERVAL
import com.bridgecrew.settings.PLUGIN_NAME
import com.bridgecrew.settings.PrismaSettingsState
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

data class LoginRequest(val username: String?, val password: String?)

data class LoginResponse(val token: String)

data class ConfigResponse(val reportingInterval: Int = DEFAULT_REPORTING_INTERVAL)

data class PrismaConnectionDetails(
    val url: String,
    val accessKey: String,
    val secretKey: String
)

val mapper = ObjectMapper().apply {
    registerModule(KotlinModule.Builder().build())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

@Service
class PrismaApiClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var connection: PrismaConnectionDetails? = null
    private val client = RestTemplate(listOf(
        MappingJackson2HttpMessageConverter(mapper)
    ))

    init {
        updateConnectionDetails()
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object : CheckovSettingsListener {
                override fun settingsUpdated() {
                    updateConnectionDetails()
                }
            })
    }

    private fun updateConnectionDetails() {
        PrismaSettingsState().getInstance()?.let { settings ->
            if (settings.isConfigured()) {
                connection = PrismaConnectionDetails(settings.prismaURL, settings.accessKey, settings.secretKey)
            }
        }
    }

    fun putDataAnalytics(data: MutableList<AnalyticsData>): Boolean {
         return sendRequest<Any>("/bridgecrew/api/v1/plugins-analytics", HttpMethod.PUT, data, true) != null
    }

    fun getConfig(): ConfigResponse? {
        return sendRequest("/bridgecrew/api/v1/plugins-analytics/get-config/$PLUGIN_NAME", HttpMethod.GET, null, true)
    }

    fun login(): LoginResponse? {
        return sendRequest("/login", HttpMethod.POST, LoginRequest(connection?.accessKey, connection?.secretKey))
    }

    private inline fun <reified T> sendRequest(endpoint: String, method: HttpMethod, payload: Any?, login: Boolean = false): T? {
        try {
            if (connection == null) {
                logger.warn("API call aborted because Prisma Cloud settings were not configured in the plugin settings")
                return null
            }
            logger.info("Sending {} request '{}' to {}", method, endpoint, connection!!.url)
            val entity = HttpEntity(payload, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                // TODO: Save token and renew on exp property of the JWT
                if (login) login()?.token?.let { setBearerAuth(it) }
            })
            val response = client.exchange(connection!!.url + endpoint, method, entity, T::class.java)
            logger.info("Successfully sent {} request '{}' to {}", method, endpoint, connection!!.url)
            return response.body
        } catch (t: Throwable) {
            logger.error("Call to '$endpoint' ended with an error: ${t.message}", t)
        }
        // TODO: Return a parent object containing error details
        return null
    }
}
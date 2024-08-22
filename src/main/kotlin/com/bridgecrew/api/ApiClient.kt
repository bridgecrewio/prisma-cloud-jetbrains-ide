package com.bridgecrew.api


import com.bridgecrew.settings.DEFAULT_REPORTING_INTERVAL
import com.bridgecrew.settings.PLUGIN_NAME
import com.intellij.openapi.diagnostic.logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse


@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class ConfigResponse(val reportingInterval: Int = DEFAULT_REPORTING_INTERVAL)

private val LOG = logger<ApiClient>()

@OptIn(ExperimentalSerializationApi::class)
class ApiClient(private val username: String, private val password: String, private val prismaURL: String) {
    private val client = HttpClient.newBuilder().build()
    private val prismaURI = URI.create(prismaURL)

    fun putDataAnalytics(data: String): Boolean {
        try {
            LOG.debug("PutDataAnalytics method call")
            val authToken = this.login().token
            //todo maybe add jwt parser to get know exp timestamp?
            if (authToken.isEmpty()) {
                //todo do we need show IDE popup here?
                LOG.warn("Could not authorize for username: $username")
                return false
            }

            val request = HttpRequest.newBuilder()
                    .uri(prismaURI.resolve("/bridgecrew/api/v1/plugins-analytics"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .PUT(BodyPublishers.ofString(data))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            LOG.debug("PutDataAnalytics method called response body: ${response.body()}")
            if(response.statusCode() == 403 || response.statusCode() == 401 || response.statusCode() == 404){
                LOG.warn("Could not authorize for token: $authToken")
                return false
            }

            return response.statusCode() == 200

        } catch (e: IOException) {
            //todo do we need show IDE popup here?
            LOG.warn("IOException: ${e.message}")
            return false
        }

    }

    fun getConfig(): ConfigResponse {
        try {
            LOG.debug("getConfig method call")
            val authToken = this.login().token
            if (authToken.isEmpty()) {
                LOG.warn("Could not authorize for username: $username")
                return ConfigResponse()
            }

            val request = HttpRequest.newBuilder()
                .uri(prismaURI.resolve("/bridgecrew/api/v1/plugins-analytics/get-config/$PLUGIN_NAME"))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            LOG.debug("getConfig method called response body: ${response.body()}")
            if(response.statusCode() == 403 || response.statusCode() == 401){
                LOG.warn("Could not authorize for token: $authToken")
                return ConfigResponse()
            }

            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<ConfigResponse>(response.body())

        } catch (e: IOException) {
            //todo do we need show IDE popup here?
            LOG.warn("IOException: ${e.message}")
            return ConfigResponse()
        }

    }

    fun getToken(){
        //todo implement: compare token exp time with current time to avoid ddosing
    }

    private fun login(): LoginResponse {
        try {
            LOG.debug("Login to $prismaURL with username: $username")
            val loginRequest = LoginRequest(username, password)
            val jsonBody = Json.encodeToString(loginRequest)

            val request = HttpRequest.newBuilder()
                    .uri(prismaURI.resolve("/login"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 401) {
                //todo do we need show IDE popup here?
                LOG.warn("Incorrect username or password")
                return LoginResponse("")
            }

            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<LoginResponse>(response.body())
        } catch (e: IOException) {
            //todo do we need show IDE popup here?
            LOG.warn("Method login called with IOException: ${e.message}")
            return LoginResponse("")
        } catch (e: SerializationException) {
            LOG.warn("Method login called with SerializationException: ${e.message}")
            return LoginResponse("")
        }
    }
}

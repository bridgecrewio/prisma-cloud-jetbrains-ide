package com.bridgecrew.api


import com.intellij.openapi.diagnostic.logger
import kotlinx.serialization.*
import kotlinx.serialization.json.*
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

private val LOG = logger<ApiClient>()

@OptIn(ExperimentalSerializationApi::class)
class ApiClient(private val username: String, private val password: String, private val prismaURL: String) {
    private val client = HttpClient.newBuilder().build();
    private val prismaURI = URI.create(prismaURL)

    fun putDataAnalytics(data: String): Boolean {
        try {
            LOG.debug("PutDataAnalytics method call")
            val authToken = this.login().token
            //todo maybe add jwt parser to get know exp timestamp?
            if (authToken.isEmpty()) {
                //todo do we need show IDE popup here?
                LOG.error("Could not authorize for username: $username")
                return false
            }

            val request = HttpRequest.newBuilder()
                    .uri(prismaURI.resolve("/bridgecrew/api/v1/plugins-analytics"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .PUT(BodyPublishers.ofString(data))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOG.debug("PutDataAnalytics method called response body: ${response.body()}")
            if(response.statusCode() == 403 || response.statusCode() == 401){
                LOG.error("Could not authorize for token: $authToken")
                return false
            }

            return response.statusCode() == 200

        } catch (e: IOException) {
            //todo do we need show IDE popup here?
            LOG.error("IOException: ${e.message}")
            return false
        }

    }


    fun login(): LoginResponse {
        try {
            LOG.debug("Login to $prismaURL with username: $username")
            val loginRequest = LoginRequest(username, password)
            val jsonBody = Json.encodeToString(loginRequest)

            val request = HttpRequest.newBuilder()
                    .uri(prismaURI.resolve("/login"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401) {
                //todo do we need show IDE popup here?
                LOG.error("Incorrect username or password")
                return LoginResponse("")
            }

            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<LoginResponse>(response.body())
        } catch (e: IOException) {
            //todo do we need show IDE popup here?
            LOG.error("Method login called with IOException: ${e.message}")
            return LoginResponse("")
        } catch (e: SerializationException) {
            LOG.error("Method login called with SerializationException: ${e.message}")
            return LoginResponse("")
        }
    }
}
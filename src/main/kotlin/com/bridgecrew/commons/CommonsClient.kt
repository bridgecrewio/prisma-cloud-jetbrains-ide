package com.bridgecrew.commons

import com.bridgecrew.commons.models.Request
import com.bridgecrew.commons.models.Response
import com.bridgecrew.utils.GlobalMapper
import com.fasterxml.jackson.core.JsonProcessingException
import com.intellij.openapi.components.Service
import com.sun.jna.Native
import org.eclipse.sisu.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class CommonsClient {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Nullable
    private var commons: CommonsLibrary? = null

    init {
        try {
            val file = Native.extractFromResourcePath(
                "/lib/${System.mapLibraryName("commons-${getArch()}")}",
                javaClass.classLoader
            )
            logger.info("Loading commons lib from ${file.absolutePath}")
            commons = Native.load(file.absolutePath, CommonsLibrary::class.java)
            logger.info("Successfully loaded commons from ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to load commons lib", e)
        }
    }

    fun helloWorld() {
        commons?.HelloWorld()
    }

    fun add(a: Int, b: Int): Int {
        return commons?.Add(a, b) ?: 0
    }

    fun handleRequest(request: Request?): Response {
        try {
            val requestJson = GlobalMapper.i().writeValueAsString(request)
            val response = commons?.HandleRequest(requestJson)
            return GlobalMapper.i().readValue(response, Response::class.java)
        } catch (e: JsonProcessingException) {
            logger.error("Failed to handle request", e)
            return Response(e.message)
        }
    }

    @Throws(Exception::class)
    private fun getArch(): String {
        return when (val arch = System.getProperty("os.arch").lowercase()) {
            "x86_64", "amd64" -> "x64"
            "arm", "arm64", "aarch64" -> "arm"
            else -> throw Exception("Unsupported architecture: $arch")
        }
    }
}
package com.bridgecrew.utils

import com.intellij.openapi.application.ApplicationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplicationServiceUtil {

    companion object {

        val logger: Logger = LoggerFactory.getLogger(ApplicationServiceUtil::class.java)

        @JvmStatic
        fun <T> getService(clazz: Class<T>): T {
            val service = ApplicationManager.getApplication().getService(clazz)
            if (service == null) {
                val message = String.format("Could not find service %s", clazz.simpleName)
                logger.error(message)
                throw IllegalArgumentException(message)
            }
            return service
        }
    }
}
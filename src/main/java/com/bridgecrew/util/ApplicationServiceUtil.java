package com.bridgecrew.util;

import com.intellij.openapi.application.ApplicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationServiceUtil {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationServiceUtil.class);

    public static <T> T getService(Class<T> clazz) {
        T service = ApplicationManager.getApplication().getService(clazz);
        if (service == null) {
            String message = String.format("Could not find service %s", clazz.getSimpleName());
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        return service;
    }
}

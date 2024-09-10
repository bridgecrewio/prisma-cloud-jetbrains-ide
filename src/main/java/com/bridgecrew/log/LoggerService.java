package com.bridgecrew.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.intellij.openapi.components.Service;
import org.slf4j.LoggerFactory;

@Service
public final class LoggerService {

    public String getLogFilePath() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Appender<?> appender = rootLogger.getAppender("FILE");
        if (appender instanceof FileAppender<?> fileAppender) {
            return fileAppender.getFile();
        }
        return null;
    }
}
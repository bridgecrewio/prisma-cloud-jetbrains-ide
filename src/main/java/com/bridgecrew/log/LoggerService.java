package com.bridgecrew.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import org.slf4j.LoggerFactory;

import java.net.URL;

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

    public void initializeLogger() {
        System.setProperty("prisma.log", PathManager.getLogPath());
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            URL configURL = getClass().getClassLoader().getResource("logback.xml");
            if (configURL != null) {
                configurator.doConfigure(configURL);
            } else {
                System.err.println("Logback configuration file not found");
            }
        } catch (JoranException je) {
            System.err.println("Failed to initialize logback: " + je.getMessage());
        }
    }
}
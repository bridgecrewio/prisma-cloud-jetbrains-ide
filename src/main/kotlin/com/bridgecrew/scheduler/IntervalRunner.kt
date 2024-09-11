package com.bridgecrew.scheduler

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class IntervalRunner(val project: Project) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val timer = Timer()
    fun scheduleWithTimer(intervalFunction: () -> Unit, period: Int) {
        val stopWatch = StopWatch.createStarted()

        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    stopWatch.time
                    intervalFunction()
                    logger.info(
                        "Function in scheduleWithTimer for ${project.name} executed with delay " + TimeUnit.MILLISECONDS.toSeconds(
                            stopWatch.time
                        )
                    )
                } catch (e: Throwable) {
                    logger.info("Catch the exception: $e")
                    stopWatch.stop()
                    timer.cancel()
                }
            }
        }, 0, (1000L * period))
    }

    fun stop(){
        timer.cancel()
        logger.info("Timer stopped for ${project.name}")
    }
}

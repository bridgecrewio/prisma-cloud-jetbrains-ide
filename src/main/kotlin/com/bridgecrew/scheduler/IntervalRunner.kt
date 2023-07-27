package com.bridgecrew.scheduler

import com.bridgecrew.analytics.AnalyticsService
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.util.Timer
import org.apache.commons.lang3.time.StopWatch
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class IntervalRunner(val project: Project) {
    private val LOG = logger<IntervalRunner>()
    private val timer = Timer()
    fun scheduleWithTimer(intervalFunction: () -> Unit, period: Int) {
        val stopWatch = StopWatch.createStarted()

        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    stopWatch.time
                    intervalFunction()
                    LOG.info("Function in scheduleWithTimer for ${project.name} executed with delay " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.time))
                } catch (e: Throwable) {
                    LOG.info("Catch the exception: $e")
                    stopWatch.stop()
                    timer.cancel()
                }
            }
        }, 0, (1000L * period))
    }

    fun stop(){
        timer.cancel()
        LOG.info("Timer stopped for ${project.name}")
    }
}

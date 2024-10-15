package com.bridgecrew.scheduler

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import java.util.*

class IntervalRunner(private val name: String) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val timer = Timer()

    fun scheduleWithTimer(intervalFunction: () -> Unit, period: Int) {
        val stopWatch = StopWatch.createStarted()

        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    intervalFunction()
                    logger.info("Interval function for $name executed with delay " + stopWatch.duration.toSeconds())
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
        logger.info("Timer stopped for IntervalRunner for $name")
    }
}

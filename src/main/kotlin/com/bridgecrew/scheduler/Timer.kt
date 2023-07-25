package com.bridgecrew.scheduler

import java.util.Timer
import org.apache.commons.lang3.time.StopWatch
import java.util.*
import java.util.concurrent.TimeUnit

class IntervalRunner {
    fun scheduleWithTimer(intervalFunction: () -> Unit, period: Int) {
        val stopWatch = StopWatch.createStarted()
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    stopWatch.time
                    intervalFunction()
                    println("Function in scheduleWithTimer executed with delay " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.time))
                } catch (e: Throwable) {
                    println("Catch the exception: $e")
                    stopWatch.stop()
                    timer.cancel()
                }

            }
        }, 0, (1000L * period))
    }

    fun scheduleWithTimer(period: () -> Unit) {

    }
}

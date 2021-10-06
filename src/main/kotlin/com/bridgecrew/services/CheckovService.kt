package com.bridgecrew.services

import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner

class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())

    fun installCheckov() {
        println("Trying to install Checkov")
        for (runner in checkovRunners) {
            var isCheckovInstalled = runner.installOrUpdate()
            if (isCheckovInstalled) {
                this.selectedCheckovRunner = runner
                println("Checkov installed successfully using ${runner.javaClass.kotlin}")
                return
            }
        }

        if (selectedCheckovRunner == null) {
            throw Exception("Could not install Checkov.")
        }
    }

    fun scanFile(filePath: String, extensionVersion: String, token: String) {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        var result = selectedCheckovRunner!!.run(filePath, extensionVersion, token)
        println(result)
    }
}
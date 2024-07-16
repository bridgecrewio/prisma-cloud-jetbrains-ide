package com.bridgecrew.utils

fun isWindows(): Boolean {
    return System.getProperty("os.name").lowercase().contains("win")
}
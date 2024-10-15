package com.bridgecrew.commons

import com.sun.jna.Library

interface CommonsLibrary : Library {

    fun HelloWorld()

    fun Add(a: Int, b: Int): Int

    fun HandleRequest(request: String?): String?
}
package com.bridgecrew.cache

object InMemCache {

    private val cache: MutableMap<String, String> = mutableMapOf()

    fun get(key: String): String? {
        return cache[key]
    }

    fun set(key: String, value: String) {
        cache[key] = value
    }
}

package com.bridgecrew.commons.models

data class Response(
    var strings: List<String>?,
    var result: Boolean?
) : PluginCommonsResponse(null) {

    constructor(error: String?) : this(null, null) {
        this.error = error
    }
}
package com.bridgecrew.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class GlobalMapper {

    companion object {

        private val mapper: ObjectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build());
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        @JvmStatic
        fun i(): ObjectMapper {
            return mapper
        }
    }
}
package com.example.vampire_system.util

object Validation {
    fun requireNonBlank(value: String, field: String) {
        require(value.isNotBlank()) { "$field must not be blank" }
    }
}



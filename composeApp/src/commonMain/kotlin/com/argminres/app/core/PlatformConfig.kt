package com.argminres.app.core

expect object PlatformConfig {
    val platformName: String
    val requiresActivation: Boolean
}

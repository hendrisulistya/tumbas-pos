package com.tumbaspos.app.util

import java.security.MessageDigest

/**
 * Utility for hashing and verifying PINs using SHA-256
 */
object PinHasher {
    
    /**
     * Hash a PIN using SHA-256
     */
    fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    /**
     * Verify a PIN against a hashed PIN
     */
    fun verifyPin(pin: String, hashedPin: String): Boolean {
        val hashedInput = hashPin(pin)
        return hashedInput == hashedPin
    }
    
    /**
     * Check if a string is already hashed (64 hex characters for SHA-256)
     */
    fun isHashed(pin: String): Boolean {
        return pin.length == 64 && pin.all { it.isDigit() || it in 'a'..'f' }
    }
}

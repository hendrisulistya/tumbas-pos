package com.tumbaspos.app.domain.model

data class R2Config(
    val accountId: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val bucketName: String
) {
    val endpointUrl: String
        get() = "https://$accountId.r2.cloudflarestorage.com"
}

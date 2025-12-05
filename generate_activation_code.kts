
import java.io.File
import java.util.Properties
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: kots generate_activation_code.kts <APP_ID>")
        return
    }

    val appId = args[0]
    val localPropertiesFile = File("local.properties")
    
    if (!localPropertiesFile.exists()) {
        println("Error: local.properties not found.")
        return
    }

    val properties = Properties()
    properties.load(localPropertiesFile.inputStream())
    val secret = properties.getProperty("APP_SECRET")?.trim()?.trim('"')

    if (secret.isNullOrBlank()) {
        println("Error: APP_SECRET not found in local.properties")
        return
    }

    val code = calculateHmac(appId, secret)
    println("Activation Code for App ID '$appId': $code")
}

fun calculateHmac(data: String, key: String): String {
    val algorithm = "HmacSHA256"
    val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secretKeySpec)
    val bytes = mac.doFinal(data.toByteArray())
    val hexCode = bytes.joinToString("") { "%02x".format(it) }.substring(0, 16).uppercase()
    return hexCode.chunked(4).joinToString("-")
}

main(args)

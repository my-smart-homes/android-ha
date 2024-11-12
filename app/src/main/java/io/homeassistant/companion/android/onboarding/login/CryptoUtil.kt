package io.homeassistant.companion.android.onboarding.login
import io.homeassistant.companion.android.BuildConfig
import java.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtil {

    fun aes256CbcPkcs7Decrypt(encryptedBase64: String): String {
        // Decode Base64 encoded encrypted data
        val encryptedData = Base64.getDecoder().decode(encryptedBase64)

        // Retrieve the key and IV from environment variables
        val secretKeyString = BuildConfig.MSH_AES_KEY
        val ivString = BuildConfig.MSH_AES_IV

        // Ensure the key and IV meet the length requirements
        if (secretKeyString.length != 32) throw IllegalArgumentException("AES key must be 32 characters long for AES-256.")
        if (ivString.length != 16) throw IllegalArgumentException("AES IV must be 16 characters long for AES-CBC.")

        // Prepare key and IV
        val secretKey: Key = SecretKeySpec(secretKeyString.toByteArray(Charsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec(ivString.toByteArray(Charsets.UTF_8))

        // Initialize Cipher for decryption with AES, CBC mode, and PKCS5 padding
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        // Perform decryption
        val decryptedBytes = cipher.doFinal(encryptedData)

        // Convert decrypted bytes to a UTF-8 string
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

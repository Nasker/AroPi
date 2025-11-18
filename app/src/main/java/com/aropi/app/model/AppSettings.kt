package com.aropi.app.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Locale

/**
 * Application settings data model.
 */
data class AppSettings(
    val language: AppLanguage = AppLanguage.SPANISH,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val showLabels: Boolean = true,
    val volumeBoost: Boolean = false  // Boost media volume when speaking
)

/**
 * Supported languages in the app.
 */
@Serializable(with = AppLanguageSerializer::class)
enum class AppLanguage(val displayName: String, val locale: Locale) {
    SPANISH("Español", Locale("es", "ES")),
    CATALAN("Català", Locale("ca", "ES"));
    
    companion object {
        fun fromLocale(locale: Locale): AppLanguage {
            return values().find { it.locale.language == locale.language } ?: SPANISH
        }
    }
}

/**
 * Custom serializer for AppLanguage enum.
 */
object AppLanguageSerializer : KSerializer<AppLanguage> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("AppLanguage", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: AppLanguage) {
        encoder.encodeString(value.name)
    }
    
    override fun deserialize(decoder: Decoder): AppLanguage {
        return AppLanguage.valueOf(decoder.decodeString())
    }
}

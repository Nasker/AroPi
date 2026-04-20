package com.aropi.app.model.obf

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Open Board Format (OBF) data models.
 * Specification: https://www.openboardformat.org/
 */

@Serializable
data class OBFBoard(
    val id: String,
    val name: String? = null,
    @SerialName("locale")
    val locale: String? = "es",
    @SerialName("default_layout")
    val defaultLayout: String? = "landscape",
    val url: String? = null,
    @SerialName("license")
    val license: OBFLicense? = null,
    val buttons: List<OBFButton> = emptyList(),
    val grid: OBFGrid,
    val images: List<OBFImage> = emptyList(),
    val sounds: List<OBFSound> = emptyList(),
    @SerialName("description_html")
    val descriptionHtml: String? = null,
    @SerialName("ext_aropi_grammar_layout")
    val extAropiGrammarLayout: Boolean? = null  // Custom extension to mark AroPi boards
)

@Serializable
data class OBFGrid(
    val rows: Int,
    val columns: Int,
    val order: List<List<Int?>>  // 2D array of button IDs (null for empty cells)
)

@Serializable
data class OBFButton(
    val id: Int,
    val label: String,
    @SerialName("vocalization")
    val vocalization: String? = null,
    @SerialName("image_id")
    val imageId: String? = null,
    @SerialName("sound_id")
    val soundId: String? = null,
    @SerialName("background_color")
    val backgroundColor: String? = null,
    @SerialName("border_color")
    val borderColor: String? = null,
    @SerialName("load_board")
    val loadBoard: OBFLoadBoard? = null,
    val action: String? = null,
    val url: String? = null,
    @SerialName("ext_aropi_grammar_type")
    val extAropiGrammarType: String? = null,  // Custom extension for grammar type
    val translations: Map<String, OBFTranslation>? = null
)

@Serializable
data class OBFLoadBoard(
    val id: String,
    val url: String? = null,
    val path: String? = null
)

@Serializable
data class OBFTranslation(
    val label: String? = null,
    val vocalization: String? = null
)

@Serializable
data class OBFImage(
    val id: String,
    val url: String? = null,
    val data: String? = null,  // base64 encoded data URL
    @SerialName("content_type")
    val contentType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val license: OBFLicense? = null
)

@Serializable
data class OBFSound(
    val id: String,
    val url: String? = null,
    val data: String? = null,  // base64 encoded data URL
    @SerialName("content_type")
    val contentType: String? = null,
    val duration: Int? = null,
    val license: OBFLicense? = null
)

@Serializable
data class OBFLicense(
    val type: String,  // e.g., "private", "CC BY", "CC BY-SA"
    @SerialName("copyright_notice_url")
    val copyrightNoticeUrl: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_url")
    val authorUrl: String? = null
)

/**
 * OBZ manifest for multi-board packages
 */
@Serializable
data class OBZManifest(
    val format: String = "open-board-0.1",
    val root: String,  // ID of the root board
    val paths: OBZPaths,
    val boards: Map<String, OBFBoard>? = null
)

@Serializable
data class OBZPaths(
    val boards: Map<String, String>? = null,  // board ID -> file path (optional)
    val images: Map<String, String>? = null  // image ID -> file path
)

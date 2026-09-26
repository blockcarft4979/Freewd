package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DocumentScreenData(val url: String): NavKey {
}
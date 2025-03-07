package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventVO(
    @Json(name = "name")
    val name: String,
    @Json(name = "data")
    val data: Any,
)
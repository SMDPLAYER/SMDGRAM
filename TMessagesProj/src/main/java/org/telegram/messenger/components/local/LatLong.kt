package org.telegram.messenger.components.local


/**
 * Created by Siddikov Mukhriddin on 9/6/22
 */
data class LatLong(
    var time: String?,
    val latitude: Double,
    val longitude: Double,
    val speed: Int?,
//    var data_source: String?,
    var employee: Long? = null,
//    var app_version: String? = BuildConfig.VERSION_NAME,
)
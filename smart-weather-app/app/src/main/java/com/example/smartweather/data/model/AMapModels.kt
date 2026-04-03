package com.example.smartweather.data.model

import com.google.gson.annotations.SerializedName

/**
 * 高德地图地理编码响应
 */
data class AMapGeocodeResponse(
    val status: String,
    val info: String,
    val infocode: String,
    val count: String,
    val geocodes: List<Geocode>?
)

/**
 * 地理编码结果
 */
data class Geocode(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val country: String,
    val province: String,
    val city: String,
    val citycode: String?,
    val district: String?,
    val adcode: String?,
    val location: String,          // 经度,纬度
    val level: String?
)

/**
 * 高德地图逆地理编码响应
 */
data class AMapRegeoResponse(
    val status: String,
    val info: String,
    val infocode: String,
    val regeocode: Regeocode?
)

/**
 * 逆地理编码结果
 */
data class Regeocode(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val addressComponent: AddressComponent
)

/**
 * 地址组件
 */
data class AddressComponent(
    val country: String,
    val province: String,
    val city: String?,             // 直辖市可能为空
    val citycode: String?,
    val district: String?,
    val adcode: String?,
    val township: String?,
    val towncode: String?,
    @SerializedName("streetNumber")
    val streetNumber: StreetNumber?
)

/**
 * 街道门牌信息
 */
data class StreetNumber(
    val street: String?,
    val number: String?,
    val location: String?
)

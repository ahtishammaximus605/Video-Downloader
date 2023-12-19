package com.vid.videodownloader.model

import com.google.gson.annotations.SerializedName

data class LinkVideoData(
@SerializedName("title") var title: String? = null,
@SerializedName("uploader") var uploader: String? = null,
@SerializedName("thumbnail") var thumbnail: String? = null,
@SerializedName("description") var description: String? = null,
@SerializedName("duration") var duration: Double? = null,
@SerializedName("webpage_url") var webpageUrl: String? = null,
@SerializedName("formats") var formats: ArrayList<Formats> = arrayListOf()
)
data class Formats(
    @SerializedName("format_id") var formatId: String? = null,
    @SerializedName("url") var url: String? = null,
    @SerializedName("manifest_url") var manifestUrl: String? = null,
    @SerializedName("ext") var ext: String? = null,
    @SerializedName("resolution") var resolution: String? = null,
    @SerializedName("format") var format: String? = null,
    @SerializedName("filesize") var filesize: Int? = null
)
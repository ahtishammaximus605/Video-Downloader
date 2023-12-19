package com.vid.videodownloader.model

import android.net.Uri


data class Download (

    var name : String? = null,
    var path : String? = null,
    var size : Long? = null,
    var duration : Long? = null,
    var id : Long? = null,
    var isDownloading : Boolean = false,
    var progress : Int = 0,
    var dmId : Long = 0,
    var fbUrl : String? = null,
    var totalbytes : Long = 0,
    var isCanceled : Boolean = false,

)
package com.vid.videodownloader.model

import com.google.gson.annotations.SerializedName

data class RemoteConfig(

    @SerializedName("admob_InterID1")
    val admob_Inter_ID1: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_InterID2")
    val admob_Inter_ID2: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_InterID3")
    val admob_Inter_ID3: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_InterID4")
    val admob_Inter_ID4: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_InterID5")
    val admob_Inter_ID5: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("collapseAble_banner_ID")
    val collapseAble_banner_ID: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_adaptive_banner_id")
    val admob_adaptive_banner_id: RemoteDetailModel = RemoteDetailModel(""),
    @SerializedName("admob_small_banner_id")
    val admob_small_banner_id: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_medium_rectangle_id")
    val admob_medium_rectangle_id: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_medium_banner_ad")
    val admob_medium_banner_ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_adaptive_banner_ad")
    val admob_adaptive_banner_ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_collapsable_banner_ad")
    val admob_collapsable_banner_ad: RemoteDetailModel = RemoteDetailModel(""),


    @SerializedName("admob_splash_InterAd")
    val admob_splash_InterAd: RemoteDetailModel = RemoteDetailModel(""),

    //Native ADs
    @SerializedName("admob_splash_Native_ID")
    val admob_splash_Native_ID: RemoteDetailModel = RemoteDetailModel(""),
    @SerializedName("admob_download_Button_Native_ID")
    val admob_download_Button_Native_ID: RemoteDetailModel = RemoteDetailModel(""),

      @SerializedName("admob_splash_NativeAd")
    val admob_splash_NativeAd: RemoteDetailModel = RemoteDetailModel(""),
    @SerializedName("admob_splash_NativeAd_Position")
    val admob_splash_NativeAd_Position: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_all_banner_ad")
    val admob_all_banner_ad: RemoteDetailModel = RemoteDetailModel(""),


    @SerializedName("admob_download_screen_back_btn_Inter_ad")
    val admob_download_screen_back_btn_Inter_ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_dashboard_Native_ID")
    val admob_dashboard_Native_ID: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_dashboard_Inner_Native_ID")
    val admob_dashboard_Inner_Native_ID: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("appopen_ad")
    val appopen_ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_app_open_ID")
    val admob_app_open_ID: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_dashboard_activity_Native_Ad")
    val admob_dashboard_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_cast_native_activity_Native_Ad")
    val admob_cast_native_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_download_link_activity_Native_Ad")
    val admob_download_link_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_downloads_activity_Native_Ad")
    val admob_downloads_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_movies_activity_Native_Ad")
    val admob_movies_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_downloading_dummy_activity_Native_Ad")
    val admob_downloading_dummy_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_setting_activity_Native_Ad")
    val admob_setting_activity_Native_Ad: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("admob_share_NativeAd")
    val admob_share_NativeAd: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("dashboard_Activity_on_off")
    val dashboard_Activity_on_off: RemoteDetailModel = RemoteDetailModel(""),


    @SerializedName("callToActionBtnColor")
    val callToActionBtnColor: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("base_url_link")
    val base_url_link: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("base_url_anything")
    val base_url_anything: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("dashboard_d_via_url_inter")
    val dashboard_d_via_url_inter: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("video_player_back_inter")
    val video_player_back_inter: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("dashboard_trending_movies_inter")
    val dashboard_trending_movies_inter: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("dashboard_saved_videos_inter")
    val dashboard_saved_videos_inter: RemoteDetailModel = RemoteDetailModel(""),

    @SerializedName("dashboard_screen_mirroring_inter")
    val dashboard_screen_mirroring_inter: RemoteDetailModel = RemoteDetailModel(""),

)

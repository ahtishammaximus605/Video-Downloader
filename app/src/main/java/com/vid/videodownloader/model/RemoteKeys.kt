package com.vid.videodownloader.model


class RemoteValues {
    companion object{
        var inapp : String = "inapp"
        var am : String = "on"
        var off : String = "off"
        var low : String = "lctr"
        var high : String = "hctr"
        var medium : String = "mctr"
        var inter : String = "inter"
        var open : String = "appopen"
        var applovin : String = "applovin"

        fun isOn(key : String?): Boolean {
            return (key == RemoteValues.am
                    || key == RemoteValues.low
                    || key == RemoteValues.medium
                    || key == RemoteValues.high)
        }
    }


}
class RemoteKeys {
    companion object{
        // Open Ad
        var openad : String = "admob_inters_appopen_applovin"
        var is_open_ad : String = "show_open_ad"
        // Native Ads
        var splash_native_ctr : String ="splash_native_ctr"
        var splash_native_admob : String = "splash_native_admob"
        var splash_native : String = "splash_native"
        var dash_native : String = "dash_native"
        var admob_dash_native : String = "admob_dash_native"
        var how_to_download_native : String = "how_to_download_native"
        var downloads_native : String = "downloads_screen_native"
        var movies_native : String = "movies_screen_native"
        var share_native : String = "share_native"
        var quit_native : String = "quit_native"
        var permission_native : String = "permission_native"
        var cast_native : String = "cast_native"

        // Banner Ads
        var dashboard_banner : String = "all_banner"
        var premium_banner : String = "all_banner"
        var how_to_cast_banner : String = "all_banner"
        // Inters Ads
        var admob_dash_inters : String = "admob_dash_inters"
        var splash_inters : String = "splash_inters"
        var prem_inters : String = "premium_back_inters"
        var prem_splash_inters : String = "premium_splash_inters"
        var share_inters : String = "share_back_inters"
        var quit_inters : String = "quit_inters"

        var how_to_download_dash_click_inters : String = "how_to_download_dash_inters"
        var cast_dash_inters : String = "cast_dash_inters"
        var downloads_dash_inters : String = "downloads_dash_inters"
        var movies_dash_inters : String = "movies_dash_inters"

        var how_to_download_tab_click_inters : String = "how_to_download_tab_inters"
        var facebook_tab_click_inters : String = "facebook_tab_inters"
        var downloads_tab_click_inters : String = "downloads_tab_inters"
        var movies_tab_click_inters : String = "movies_tab_inters"
        var settings_tab_click_inters : String = "sidemenu_tab_inters"
//        var cast_back_inters : String = "cast_back_inters"
//        var facebook_tab_back_inters : String = "facebook_tab_back_inters"
//        var downloads_tab_back_inters : String = "downloads_tab_back_inters"
//        var movies_tab_back_inters : String = "movies_tab_back_inters"
//        var settings_tab_back_inters : String = "sidemenu_tab_back_inters"
        var tab_back_inters : String = "tabs_back_inters"
        var movie_thumbnail_click_inters : String = "movie_thumbnail_inters"
        var download_now_click_inters : String = "download_now_inters"
        var watch_now_inters : String = "watch_now_inters"
        var notification_tap_inters : String = "notification_tap_inters"
    }
}
class RemoteIds {
    companion object{
        // Ad Ids
        var s_inters_id : String = "splash_inters_id"
        var s_native_id : String = "splash_native_id"
        var admob_inters_id : String = "admob_inter_id"
        var admob_native_id : String = "admob_native_id"
        var open_id : String = "admob_open_id"

        var inters_id : String = "inters_id"
        var native_id : String = "native_id"

        var banner_id : String = "banner_id"


    }
}
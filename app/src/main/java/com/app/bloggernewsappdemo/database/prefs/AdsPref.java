package com.app.bloggernewsappdemo.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AdsPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AdsPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("ads_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAds(String adStatus, String adType, String backupAds, String adMobPublisherId, String adMobAppId, String adMobBannerId, String adMobInterstitialId, String adMobNativeId, String adMobAppOpenId, String adManagerBannerUnitId, String adManagerInterstitialUnitId, String adManagerNativeUnitId, String adManagerAppOpenAdUnitId, String startAppId, String unityGameId, String unityBannerId, String unityInterstitialId, String appLovinBannerId, String appLovinInterstitialId, String applovinNativeAdManualUnitId, String applovinBannerZoneId, String applovinInterstitialZoneId, String ironsourceAppKey, String ironsourceBannerId, String ironsourceInterstitialId, int interstitialAdInterval, int nativeAdIndex) {
        editor.putString("ad_status", adStatus);
        editor.putString("ad_type", adType);
        editor.putString("backup_ads", backupAds);
        editor.putString("admob_publisher_id", adMobPublisherId);
        editor.putString("admob_app_id", adMobAppId);
        editor.putString("admob_banner_unit_id", adMobBannerId);
        editor.putString("admob_interstitial_unit_id", adMobInterstitialId);
        editor.putString("admob_native_unit_id", adMobNativeId);
        editor.putString("admob_app_open_ad_unit_id", adMobAppOpenId);
        editor.putString("ad_manager_banner_unit_id", adManagerBannerUnitId);
        editor.putString("ad_manager_interstitial_unit_id", adManagerInterstitialUnitId);
        editor.putString("ad_manager_native_unit_id", adManagerNativeUnitId);
        editor.putString("ad_manager_app_open_ad_unit_id", adManagerAppOpenAdUnitId);
        editor.putString("startapp_app_id", startAppId);
        editor.putString("unity_game_id", unityGameId);
        editor.putString("unity_banner_placement_id", unityBannerId);
        editor.putString("unity_interstitial_placement_id", unityInterstitialId);
        editor.putString("applovin_banner_ad_unit_id", appLovinBannerId);
        editor.putString("applovin_interstitial_ad_unit_id", appLovinInterstitialId);
        editor.putString("applovin_native_ad_manual_unit_id", applovinNativeAdManualUnitId);
        editor.putString("applovin_banner_zone_id", applovinBannerZoneId);
        editor.putString("applovin_interstitial_zone_id", applovinInterstitialZoneId);
        editor.putString("ironsource_app_key", ironsourceAppKey);
        editor.putString("ironsource_banner_id", ironsourceBannerId);
        editor.putString("ironsource_interstitial_id", ironsourceInterstitialId);
        editor.putInt("interstitial_ad_interval", interstitialAdInterval);
        editor.putInt("native_ad_index", nativeAdIndex);
        editor.apply();
    }

    public String getAdStatus() {
        return sharedPreferences.getString("ad_status", "0");
    }

    public String getAdType() {
        return sharedPreferences.getString("ad_type", "0");
    }

    public String getBackupAds() {
        return sharedPreferences.getString("backup_ads", "none");
    }

    public String getAdMobPublisherId() {
        return sharedPreferences.getString("admob_publisher_id", "0");
    }

    public String getAdMobAppId() {
        return sharedPreferences.getString("admob_app_id", "0");
    }

    public String getAdMobBannerId() {
        return sharedPreferences.getString("admob_banner_unit_id", "0");
    }

    public String getAdMobInterstitialId() {
        return sharedPreferences.getString("admob_interstitial_unit_id", "0");
    }

    public String getAdMobNativeId() {
        return sharedPreferences.getString("admob_native_unit_id", "0");
    }

    public String getAdMobAppOpenAdId() {
        return sharedPreferences.getString("admob_app_open_ad_unit_id", "0");
    }

    public String getAdManagerBannerId() {
        return sharedPreferences.getString("ad_manager_banner_unit_id", "0");
    }

    public String getAdManagerInterstitialId() {
        return sharedPreferences.getString("ad_manager_interstitial_unit_id", "0");
    }

    public String getAdManagerNativeId() {
        return sharedPreferences.getString("ad_manager_native_unit_id", "0");
    }

    public String getAdManagerAppOpenAdId() {
        return sharedPreferences.getString("ad_manager_app_open_ad_unit_id", "0");
    }

    public String getStartappAppId() {
        return sharedPreferences.getString("startapp_app_id", "0");
    }

    public String getUnityGameId() {
        return sharedPreferences.getString("unity_game_id", "0");
    }

    public String getUnityBannerPlacementId() {
        return sharedPreferences.getString("unity_banner_placement_id", "banner");
    }

    public String getUnityInterstitialPlacementId() {
        return sharedPreferences.getString("unity_interstitial_placement_id", "video");
    }

    public String getAppLovinBannerAdUnitId() {
        return sharedPreferences.getString("applovin_banner_ad_unit_id", "0");
    }

    public String getAppLovinInterstitialAdUnitId() {
        return sharedPreferences.getString("applovin_interstitial_ad_unit_id", "0");
    }

    public String getAppLovinNativeAdManualUnitId() {
        return sharedPreferences.getString("applovin_native_ad_manual_unit_id", "0");
    }

    public String getAppLovinBannerZoneId() {
        return sharedPreferences.getString("applovin_banner_zone_id", "0");
    }

    public String getAppLovinInterstitialZoneId() {
        return sharedPreferences.getString("applovin_interstitial_zone_id", "0");
    }

    public String getIronSourceAppKey() {
        return sharedPreferences.getString("ironsource_app_key", "0");
    }

    public String getIronSourceBannerId() {
        return sharedPreferences.getString("ironsource_banner_id", "0");
    }

    public String getIronSourceInterstitialId() {
        return sharedPreferences.getString("ironsource_interstitial_id", "0");
    }

    public int getInterstitialAdInterval() {
        return sharedPreferences.getInt("interstitial_ad_interval", 0);
    }

    public int getNativeAdIndex() {
        return sharedPreferences.getInt("native_ad_index", 0);
    }

    public Integer getInterstitialAdCounter() {
        return sharedPreferences.getInt("interstitial_counter", 1);
    }

    public void updateInterstitialAdCounter(int counter) {
        editor.putInt("interstitial_counter", counter);
        editor.apply();
    }

}

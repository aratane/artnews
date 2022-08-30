package com.app.bloggernewsappdemo.activities;

import static com.app.bloggernewsappdemo.utils.Constant.BANNER_HOME;
import static com.app.bloggernewsappdemo.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.app.bloggernewsappdemo.BuildConfig;
import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.AppBarLayoutBehavior;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.RtlViewPager;
import com.app.bloggernewsappdemo.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private AppUpdateManager appUpdateManager;
    private long exitTime = 0;
    BottomNavigationView navigation;
    Toolbar toolbar;
    SharedPref sharedPref;
    AdsPref adsPref;
    CoordinatorLayout parentView;
    AdsManager adsManager;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        tools = new Tools(this);
        setContentView(R.layout.activity_main);
        Tools.setNavigation(this, sharedPref);

        sharedPref.resetPostToken();
        sharedPref.resetPageToken();

        initComponent();

        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadBannerAd(BANNER_HOME);
        adsManager.loadInterstitialAd(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());

        Tools.notificationOpenHandler(this, getIntent());

        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            checkUpdate();
            inAppReview();
        }

    }

    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void initComponent() {

        parentView = findViewById(R.id.tab_coordinator_layout);

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.app_name), false);

        navigation = findViewById(R.id.navigation);
        navigation.getMenu().clear();
        if (sharedPref.getDisplayPageMenu().equals("true")) {
            navigation.inflateMenu(R.menu.navigation_default);
        } else {
            navigation.inflateMenu(R.menu.navigation_no_page);
        }
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        viewPager = findViewById(R.id.viewpager);
        viewPagerRTL = findViewById(R.id.viewpager_rtl);
        if (Config.ENABLE_RTL_MODE) {
            tools.setupViewPagerRTL(this, viewPagerRTL, navigation, toolbar, sharedPref);
        } else {
            tools.setupViewPager(this, viewPager, navigation, toolbar, sharedPref);
        }

        if (!Tools.isConnect(this)) {
            if (sharedPref.getDisplayPageMenu().equals("true")) {
                if (Config.ENABLE_RTL_MODE) {
                    viewPagerRTL.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(3);
                }
            } else {
                if (Config.ENABLE_RTL_MODE) {
                    viewPagerRTL.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(2);
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
            adsManager.destroyBannerAd();
            return true;
        } else if (menuItem.getItemId() == R.id.action_more) {
            Intent intent = new Intent(getApplicationContext(), ActivitySettings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if (Config.ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showSnackBar(getString(R.string.press_again_to_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(BANNER_HOME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyBannerAd();
    }

    public void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                                Log.d(TAG, "In-App Review Success");
                            }
                    ).addOnFailureListener(failure -> {
                        Log.d(TAG, "In-App Review Rating Failed");
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

    private void checkUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, Constant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar("Update canceled");
            } else if (resultCode == RESULT_OK) {
                showSnackBar("Update success!");
            } else {
                showSnackBar("Update Failed!");
                checkUpdate();
            }
        }
    }

}

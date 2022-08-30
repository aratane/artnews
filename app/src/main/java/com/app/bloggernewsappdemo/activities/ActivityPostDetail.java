package com.app.bloggernewsappdemo.activities;

import static com.app.bloggernewsappdemo.utils.Constant.BANNER_POST_DETAIL;
import static com.app.bloggernewsappdemo.utils.Constant.INTERSTITIAL_POST_LIST;
import static com.app.bloggernewsappdemo.utils.Constant.NATIVE_AD_POST_DETAIL;
import static com.app.bloggernewsappdemo.utils.Constant.POST_ORDER;
import static com.app.bloggernewsappdemo.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.adapters.AdapterCategoryList;
import com.app.bloggernewsappdemo.adapters.AdapterRelated;
import com.app.bloggernewsappdemo.callbacks.CallbackPost;
import com.app.bloggernewsappdemo.callbacks.CallbackPostDetail;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbFavorite;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.rests.RestAdapter;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.AppBarLayoutBehavior;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPostDetail extends AppCompatActivity {

    private Call<CallbackPostDetail> callbackCall = null;
    private final ArrayList<Object> feedItems = new ArrayList<>();
    private View lytMainContent;
    private View lytUncategorized;
    RecyclerView recyclerView;
    private Menu menu;
    TextView txtTitle, txtDate, txtAlphabet;
    LinearLayout lytDate;
    ImageView imgDate;
    ImageView primaryImage;
    private WebView webView;
    FrameLayout customViewContainer;
    CoordinatorLayout parentView;
    private ShimmerFrameLayout lytShimmer;
    private SwipeRefreshLayout swipeRefreshLayout;
    String originalHtmlData;
    Post post;
    DbFavorite dbFavorite;
    private String singleChoiceSelected;
    SharedPref sharedPref;
    String label;
    LinearLayout lytTitle;
    AdsManager adsManager;
    AdsPref adsPref;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_post_detail);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        tools = new Tools(this);
        Tools.setNavigation(this, sharedPref);

        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);

        adsManager.loadBannerAd(BANNER_POST_DETAIL);
        adsManager.loadInterstitialAd(INTERSTITIAL_POST_LIST, 1);

        dbFavorite = new DbFavorite(this);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setRefreshing(false);

        lytMainContent = findViewById(R.id.lyt_main_content);
        lytUncategorized = findViewById(R.id.view_uncategorized);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        parentView = findViewById(R.id.coordinatorLayout);

        lytTitle = findViewById(R.id.lyt_title);
        if (sharedPref.getIsDarkTheme()) {
            lytTitle.setBackgroundResource(R.color.colorBackgroundDark);
        } else {
            lytTitle.setBackgroundResource(R.color.colorBackgroundLight);
        }

        recyclerView = findViewById(R.id.recycler_view_category);
        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);
        primaryImage = findViewById(R.id.primary_image);
        txtTitle = findViewById(R.id.txt_title);
        txtAlphabet = findViewById(R.id.txt_alphabet);
        imgDate = findViewById(R.id.ic_date);
        txtDate = findViewById(R.id.txt_date);
        lytDate = findViewById(R.id.lyt_date);

        requestAction();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
            requestAction();
        });

        setupToolbar();
        initShimmerLayout();

        if (sharedPref.showRelatedPosts().equals("true")) {
            displayRelatedPosts();
        } else {
            findViewById(R.id.viewRelatedPosts).setVisibility(View.GONE);
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }
        this.callbackCall = RestAdapter.createApiPostDetail(sharedPref.getBloggerId(), "posts", post.id).getPostDetail(apiKey);
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(@NonNull Call<CallbackPostDetail> call, @NonNull Response<CallbackPostDetail> response) {
                CallbackPostDetail resp = response.body();
                if (resp != null) {
                    displayData(resp);
                    swipeProgress(false);
                    lytMainContent.setVisibility(View.VISIBLE);
                    sharedPref.updateRetryToken(0);
                } else {
                    if (sharedPref.getRetryToken() < Constant.MAX_RETRY_TOKEN) {
                        if (sharedPref.getApiKeyPosition() >= totalKeys) {
                            sharedPref.updateApiKeyPosition(0);
                        } else {
                            sharedPref.updateApiKeyPosition(sharedPref.getApiKeyPosition() + 1);
                        }
                        new Handler().postDelayed(() -> requestPostAPI(), 100);
                        sharedPref.updateRetryToken(sharedPref.getRetryToken() + 1);
                    } else {
                        onFailRequest();
                        sharedPref.updateRetryToken(0);
                    }
                }
            }

            public void onFailure(@NonNull Call<CallbackPostDetail> call, @NonNull Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lytMainContent.setVisibility(View.GONE);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lytFailed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            lytFailed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytMainContent.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
        });
    }

    private void displayData(CallbackPostDetail post) {

        Document htmlData = Jsoup.parse(post.content);
        Elements elements = htmlData.select("img");
        if (elements.hasAttr("src")) {
            Glide.with(this)
                    .load(elements.get(0).attr("src").replace(" ", "%20"))
                    .transition(withCrossFade())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(primaryImage);
            primaryImage.setVisibility(View.VISIBLE);
            txtAlphabet.setVisibility(View.GONE);

            primaryImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                intent.putExtra("image", elements.get(0).attr("src"));
                startActivity(intent);
            });

        } else {
            primaryImage.setVisibility(View.GONE);
            txtAlphabet.setVisibility(View.VISIBLE);
            txtAlphabet.setText(post.title.substring(0, 1));
        }

        if (Config.FIRST_POST_IMAGE_AS_MAIN_IMAGE) {
            if (htmlData.select("img").first() != null) {
                Element element = htmlData.select("img").first();
                assert element != null;
                if (element.hasAttr("src")) {
                    element.remove();
                }
            }
            findViewById(R.id.lytPrimaryImage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.lytPrimaryImage).setVisibility(View.GONE);
        }
        originalHtmlData = htmlData.toString();

        txtTitle.setText(post.title);

        if (sharedPref.showPostDate().equals("true")) {
            imgDate.setVisibility(View.VISIBLE);
            txtDate.setText(Tools.getFormatedDate(post.published));
            lytDate.setVisibility(View.VISIBLE);
        } else {
            lytDate.setVisibility(View.GONE);
        }

        Tools.displayPostDescription(this, webView, originalHtmlData, customViewContainer, sharedPref);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterCategoryList adapter = new AdapterCategoryList(this, post.labels);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, items, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(EXTRA_OBJC, items.get(position));
            startActivity(intent);
            adsManager.destroyBannerAd();
        });

        if (post.labels.size() > 0) {
            if (post.labels.get(0).contains("[") && post.labels.get(0).contains("]")) {
                label = post.labels.get(0).replace("[", "").replace("]", "");
            } else {
                label = post.labels.get(0);
            }
            lytUncategorized.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            lytUncategorized.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        if (sharedPref.getIsDarkTheme()) {
            lytUncategorized.setBackgroundResource(R.drawable.bg_chips_dark);
        } else {
            lytUncategorized.setBackgroundResource(R.drawable.bg_chips_default);
        }

        adsManager.loadNativeAd(NATIVE_AD_POST_DETAIL);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
        this.menu = menu;
        if (!sharedPref.getDisplayViewOnSiteMenu().equals("true")) {
            MenuItem viewOnSiteItem = menu.findItem(R.id.action_launch);
            viewOnSiteItem.setVisible(false);
        }
        addToFavorite();
        return true;
    }

    public void addToFavorite() {
        List<Post> data = dbFavorite.getFavRow(post.id);
        if (data.size() == 0) {
            menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_favorite_outline));
        } else {
            if (data.get(0).getId().equals(post.id)) {
                menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_favorite));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.action_favorite) {
            List<Post> data = dbFavorite.getFavRow(post.id);
            if (data.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
                menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite));
            } else {
                if (data.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                    menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite_outline));
                }
            }
        } else if (itemId == R.id.action_font_size) {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new AlertDialog.Builder(ActivityPostDetail.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        WebSettings webSettings = webView.getSettings();
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                        } else {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        } else if (itemId == R.id.action_share) {
            Tools.shareArticle(this, post);
        } else if (itemId == R.id.action_launch) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url)));
        } else {
            return super.onOptionsItemSelected(menuItem);
        }

        return true;
    }

    private void displayRelatedPosts() {
        new Handler().postDelayed(() -> {
            if (post.labels.size() > 0) {
                if (post.labels.get(0).contains("[") && post.labels.get(0).contains("]")) {
                    label = post.labels.get(0).replace("[", "").replace("]", "");
                } else {
                    label = post.labels.get(0);
                }
                requestRelatedAPI(label);
            }
        }, 2000);
    }

    private void requestRelatedAPI(String category) {
        TextView txtRelated = findViewById(R.id.txt_related);
        RelativeLayout lytRelated = findViewById(R.id.lyt_related);
        RecyclerView recyclerViewRelated = findViewById(R.id.recycler_view_related);
        recyclerViewRelated.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerViewRelated.setNestedScrollingEnabled(false);
        AdapterRelated adapterRelated = new AdapterRelated(this, recyclerViewRelated, feedItems);
        recyclerViewRelated.setAdapter(adapterRelated);

        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }

        Call<CallbackPost> callbackCallRelated = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getRelatedPosts(category, POST_ORDER, apiKey);
        callbackCallRelated.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    txtRelated.setText(getString(R.string.txt_related));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        lytRelated.setVisibility(View.VISIBLE);
                    }, 2000);
                    adapterRelated.insertData(resp.items);
                    adapterRelated.setOnItemClickListener((view, obj, position) -> {
                        Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
                        intent.putExtra(EXTRA_OBJC, obj);
                        startActivity(intent);
                        sharedPref.savePostId(obj.id);
                        adsManager.destroyBannerAd();

                        if (adsPref.getInterstitialAdCounter() >= adsPref.getInterstitialAdInterval()) {
                            adsPref.updateInterstitialAdCounter(1);
                            adsManager.showInterstitialAd();
                        } else {
                            adsPref.updateInterstitialAdCounter(adsPref.getInterstitialAdCounter() + 1);
                        }
                    });
                    adapterRelated.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(parentView, obj));
                    if (resp.items.size() == 1) {
                        txtRelated.setText("");
                        lytRelated.setVisibility(View.GONE);
                    }
                } else {
                    onFailRequest();
                }
            }

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void initShimmerLayout() {
        ViewStub stub = findViewById(R.id.lytShimmerView);
        if (!Config.FIRST_POST_IMAGE_AS_MAIN_IMAGE) {
            stub.setLayoutResource(R.layout.shimmer_post_detail);
        } else {
            stub.setLayoutResource(R.layout.shimmer_post_detail_primary);
        }
        stub.inflate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(BANNER_POST_DETAIL);
    }

    public void onDestroy() {
        super.onDestroy();
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        adsManager.destroyBannerAd();
    }

}

package com.app.bloggernewsappdemo.fragments;

import static com.app.bloggernewsappdemo.utils.Constant.NATIVE_AD_POST_LIST;
import static com.app.bloggernewsappdemo.utils.Constant.POST_ORDER;
import static com.app.bloggernewsappdemo.utils.Constant.POST_PER_PAGE;
import static com.app.bloggernewsappdemo.utils.Tools.EXTRA_OBJC;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.activities.ActivityPostDetail;
import com.app.bloggernewsappdemo.activities.MainActivity;
import com.app.bloggernewsappdemo.adapters.AdapterRecent;
import com.app.bloggernewsappdemo.callbacks.CallbackPost;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.rests.RestAdapter;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPost extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterRecent adapterRecent;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    List<Post> posts = new ArrayList<>();
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    View lytShimmerHead;
    Tools tools;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post, container, false);

        if (getActivity() != null) {
            sharedPref = new SharedPref(getActivity());
            adsPref = new AdsPref(getActivity());
            adsManager = new AdsManager(getActivity());
            tools = new Tools(getActivity());
        }

        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        lytShimmerHead = rootView.findViewById(R.id.lyt_shimmer_head);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        if (Config.DISPLAY_POST_LIST_DIVIDER) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        }

        if (!sharedPref.showPostHeader().equals("true")) {
            lytShimmerHead.setVisibility(View.GONE);
            recyclerView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.item_post_padding_small), 0, getResources().getDimensionPixelSize(R.dimen.item_post_padding_small));
        }

        //set data and list adapter
        adapterRecent = new AdapterRecent(getActivity(), recyclerView, posts);
        recyclerView.setAdapter(adapterRecent);

        adapterRecent.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            ((MainActivity) getActivity()).showInterstitialAd();
            ((MainActivity) getActivity()).destroyBannerAd();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        adapterRecent.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getPostToken() != null) {
                requestAction();
            } else {
                adapterRecent.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterRecent.resetListData();
            sharedPref.resetPostToken();
            requestAction();
        });

        requestAction();

        return rootView;
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getPostToken() == null) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
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
        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getPosts(POST_ORDER, apiKey, POST_PER_PAGE, sharedPref.getPostToken());
        this.callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updatePostToken(token);
                    } else {
                        sharedPref.resetPostToken();
                    }
                    sharedPref.updateRetryToken(0);
                    adapterRecent.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(Objects.requireNonNull(getActivity()).findViewById(R.id.tab_coordinator_layout), obj));
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

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Post> posts) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && NATIVE_AD_POST_LIST != 0) {
            adapterRecent.insertDataWithNativeAd(posts);
        } else {
            adapterRecent.insertData(posts);
        }
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytFailed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoItem.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

}
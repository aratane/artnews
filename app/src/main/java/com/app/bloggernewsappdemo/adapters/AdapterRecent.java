package com.app.bloggernewsappdemo.adapters;

import static com.app.bloggernewsappdemo.utils.Constant.NATIVE_AD_POST_LIST;
import static com.app.bloggernewsappdemo.utils.Constant.NATIVE_AD_STYLE;
import static com.app.bloggernewsappdemo.utils.Constant.POST_PER_PAGE;
import static com.app.bloggernewsappdemo.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.activities.ActivityCategoryDetail;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

public class AdapterRecent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_HEAD = 2;
    private final int VIEW_AD = 3;
    private List<Post> posts;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    AdapterCategoryList adapter;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterRecent(Context context, RecyclerView view, List<Post> posts) {
        this.posts = posts;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        this.adsManager = new AdsManager((Activity) context);
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public class HeadingViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtTitle;
        public TextView txtDescription;
        public TextView txtDate;
        public View lytUncategorized;
        public TextView txtAlphabet;
        public ImageView thumbnailImage;
        public ImageView imgOverflow;
        public RecyclerView recyclerView;
        public LinearLayout lytDate;
        public RelativeLayout lytParent;

        public HeadingViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txt_title);
            txtDescription = v.findViewById(R.id.txt_description);
            txtDate = v.findViewById(R.id.txt_date);
            lytUncategorized = v.findViewById(R.id.txt_label_uncategorized);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            thumbnailImage = v.findViewById(R.id.thumbnail_image);
            imgOverflow = v.findViewById(R.id.img_overflow);
            recyclerView = v.findViewById(R.id.recycler_view);
            lytDate = v.findViewById(R.id.lyt_date);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtTitle;
        public TextView txtDescription;
        public TextView txtDate;
        public View lytUncategorized;
        public LinearLayout lytLabel;
        public TextView txtAlphabet;
        public ImageView thumbnailImage;
        public ImageView imgOverflow;
        public RecyclerView recyclerView;
        public LinearLayout lytDate;
        public LinearLayout lytParent;

        public OriginalViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txt_title);
            txtDescription = v.findViewById(R.id.txt_description);
            txtDate = v.findViewById(R.id.txt_date);
            lytUncategorized = v.findViewById(R.id.txt_label_uncategorized);
            lytLabel = v.findViewById(R.id.lyt_label);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            thumbnailImage = v.findViewById(R.id.thumbnail_image);
            imgOverflow = v.findViewById(R.id.img_overflow);
            recyclerView = v.findViewById(R.id.recycler_view);
            lytDate = v.findViewById(R.id.lyt_date);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_HEAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_heading, parent, false);
            vh = new HeadingViewHolder(v);
        } else if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            View v;
            if (NATIVE_AD_STYLE.equals("news")) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_news, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_medium, parent, false);
            }
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeadingViewHolder) {
            final Post p = (Post) posts.get(position);
            final HeadingViewHolder vItem = (HeadingViewHolder) holder;

            Document htmlData = Jsoup.parse(p.content);

            vItem.txtTitle.setText(p.title);

            if (sharedPref.showShortDescription().equals("true")) {
                vItem.txtTitle.setMaxLines(2);
                vItem.txtDescription.setText(Tools.parseHtml(htmlData.text()));
            } else {
                vItem.txtTitle.setMaxLines(3);
                vItem.txtDescription.setVisibility(View.GONE);
            }

            if (sharedPref.showPostDate().equals("true")) {
                vItem.txtDate.setText(Tools.getTimeAgo(p.published));
            } else {
                vItem.txtDate.setVisibility(View.GONE);
                vItem.lytDate.setVisibility(View.GONE);
            }

            vItem.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterCategoryList(context, p.labels);
            vItem.recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(EXTRA_OBJC, items.get(pos));
                context.startActivity(intent);
            });

            if (p.labels.size() >= 1) {
                vItem.lytUncategorized.setVisibility(View.GONE);
            } else {
                vItem.lytUncategorized.setVisibility(View.VISIBLE);
                vItem.lytUncategorized.setOnClickListener(view -> {
                });
            }

            SharedPref sharedPref = new SharedPref(context);
            if (sharedPref.getIsDarkTheme()) {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_dark);
            } else {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_default);
            }

            Elements elements = htmlData.select("img");
            if (elements.hasAttr("src")) {
                Glide.with(context)
                        .load(elements.get(0).attr("src").replace(" ", "%20"))
                        .transition(withCrossFade())
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(vItem.thumbnailImage);
                vItem.txtAlphabet.setVisibility(View.GONE);
            } else {
                vItem.thumbnailImage.setImageResource(R.drawable.bg_button_transparent);
                vItem.txtAlphabet.setVisibility(View.VISIBLE);
                vItem.txtAlphabet.setText(p.title.substring(0, 1));
            }


            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof OriginalViewHolder) {
            final Post p = (Post) posts.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            Document htmlData = Jsoup.parse(p.content);

            vItem.txtTitle.setText(p.title);

            if (sharedPref.showShortDescription().equals("true")) {
                vItem.txtTitle.setMaxLines(2);
                vItem.txtDescription.setText(Tools.parseHtml(htmlData.text()));
            } else {
                vItem.txtTitle.setMaxLines(3);
                vItem.txtDescription.setVisibility(View.GONE);
            }

            if (sharedPref.showPostDate().equals("true")) {
                vItem.txtDate.setText(Tools.getTimeAgo(p.published));
            } else {
                vItem.txtDate.setVisibility(View.GONE);
                vItem.lytDate.setVisibility(View.GONE);
            }

            vItem.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterCategoryList(context, p.labels);
            vItem.recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(EXTRA_OBJC, items.get(pos));
                context.startActivity(intent);
            });

            if (p.labels.size() >= 1) {
                vItem.lytUncategorized.setVisibility(View.GONE);
            } else {
                vItem.lytUncategorized.setVisibility(View.VISIBLE);
                vItem.lytUncategorized.setOnClickListener(view -> {
                });
            }

            SharedPref sharedPref = new SharedPref(context);
            if (sharedPref.getIsDarkTheme()) {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_dark);
            } else {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_default);
            }

            Elements elements = htmlData.select("img");
            if (elements.hasAttr("src")) {
                Glide.with(context)
                        .load(elements.get(0).attr("src").replace(" ", "%20"))
                        .transition(withCrossFade())
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().override(Constant.THUMBNAIL_WIDTH, Constant.THUMBNAIL_HEIGHT))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(vItem.thumbnailImage);
                vItem.txtAlphabet.setVisibility(View.GONE);
            } else {
                vItem.thumbnailImage.setImageResource(R.drawable.bg_button_transparent);
                vItem.txtAlphabet.setVisibility(View.VISIBLE);
                vItem.txtAlphabet.setText(p.title.substring(0, 1));
            }


            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;
            final SharedPref sharedPref = new SharedPref(context);

            vItem.loadNativeAd(context,
                    adsPref.getAdStatus(),
                    NATIVE_AD_POST_LIST,
                    adsPref.getAdType(),
                    adsPref.getBackupAds(),
                    adsPref.getAdMobNativeId(),
                    adsPref.getAdManagerNativeId(),
                    adsPref.getAppLovinNativeAdManualUnitId(),
                    sharedPref.getIsDarkTheme(),
                    Config.LEGACY_GDPR,
                    NATIVE_AD_STYLE
            );

            if (!NATIVE_AD_STYLE.equals("news")) {
                vItem.setNativeAdPadding(
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_medium),
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small),
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_medium),
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small)
                );
            }

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else if (getItemViewType(position) == VIEW_HEAD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else if (getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.posts.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void insertDataWithNativeAd(List<Post> posts) {
        setLoaded();
        int positionStart = getItemCount();
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && NATIVE_AD_POST_LIST != 0) {
            if (posts.size() >= adsPref.getNativeAdIndex())
                posts.add(adsPref.getNativeAdIndex(), new Post());
        }
        int itemCount = posts.size();
        this.posts.addAll(posts);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (posts.get(i) == null) {
                posts.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.posts.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = posts.get(position);
        if (post != null) {
            if (post.title == null || post.title.equals("")) {
                return VIEW_AD;
            }
            if (position == 0) {
                if (sharedPref.showPostHeader().equals("true")) {
                    return VIEW_HEAD;
                } else {
                    return VIEW_ITEM;
                }
            } else {
                return VIEW_ITEM;
            }
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && NATIVE_AD_POST_LIST != 0) {
                            switch (adsPref.getAdType()) {
                                case ADMOB:
                                case STARTAPP:
                                case APPLOVIN: {
                                    //posts per page plus 1 Ad
                                    int current_page = getItemCount() / (POST_PER_PAGE + 1);
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                                default: {
                                    int current_page = getItemCount() / POST_PER_PAGE;
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                            }
                        } else {
                            int current_page = getItemCount() / POST_PER_PAGE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i) lastIdx = i;
        }
        return lastIdx;
    }

}
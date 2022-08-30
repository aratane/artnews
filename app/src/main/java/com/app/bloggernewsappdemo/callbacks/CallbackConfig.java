package com.app.bloggernewsappdemo.callbacks;

import com.app.bloggernewsappdemo.models.Ads;
import com.app.bloggernewsappdemo.models.App;
import com.app.bloggernewsappdemo.models.Blog;
import com.app.bloggernewsappdemo.models.Category;
import com.app.bloggernewsappdemo.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class CallbackConfig {

    public Blog blog = null;
    public App app = null;
    public Notification notification = null;
    public Ads ads = null;
    public List<Category> labels = new ArrayList<>();

}

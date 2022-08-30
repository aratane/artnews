package com.app.bloggernewsappdemo.callbacks;

import com.app.bloggernewsappdemo.models.Author;
import com.app.bloggernewsappdemo.models.Blog;

import java.io.Serializable;

public class CallbackPageDetail implements Serializable {

    public String kind;
    public String id;
    public Blog blog = null;
    public String published;
    public String updated;
    public String url;
    public String selflink;
    public String title;
    public String content;
    public Author author = null;
    public String etag;

}

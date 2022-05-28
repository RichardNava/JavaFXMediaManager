package com.example.media;

import java.util.ArrayList;
import java.util.List;

public class MediaGroup {
    
    private final String title;
    private final List<MediaItem> items = new ArrayList<>();
    
    public MediaGroup(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public List<MediaItem> getItems() {
        return items;
    }
}
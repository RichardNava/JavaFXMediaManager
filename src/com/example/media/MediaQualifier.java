package com.example.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaQualifier {
    
    private List<MediaType> types = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private MediaOrder sortOrder = MediaOrder.TITLE_ASC;
    
    public List<MediaType> getTypes() {
        return types;
    }

    public MediaQualifier setTypes(MediaType... typeArray) {
        types.clear();
        types.addAll(Arrays.asList(typeArray));
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public MediaQualifier setTags(String... tagArray) {
        tags.clear();
        for(int i = 0; i < tagArray.length; i++) {
            tagArray[i] = tagArray[i].trim().toLowerCase();
        }
        tags.addAll(Arrays.asList(tagArray));
        return this;
    }


    public MediaOrder getSortOrder() {
        return sortOrder;
    }

    public MediaQualifier setSortOrder(MediaOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
       
    @Override
    public String toString() {
        return types.toString() + ":" + sortOrder + ":" + tags.toString();
    }
}
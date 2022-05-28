package com.example.media;

import java.io.Serializable;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;


public class MediaItem implements Serializable {

    private String title;
    private Date date;
    private String id;
    private String tags;

    public MediaItem() {}
    
    public MediaItem(String title, String id, Date date) {
        this.title = title;
        this.date = date;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MediaType getType() {
        String type = new MimetypesFileTypeMap().getContentType(id);
        if (type.startsWith("image")) {
            return MediaType.IMAGE;
        } else if (type.contains("application/ogg") || type.contains("video/ogg")) {
            return MediaType.OGV_VIDEO;
        } else if (type.contains("video/mp4")) {
            return MediaType.MP4_VIDEO;
        } else if (type.contains("video/x-flv")) {
            return MediaType.FLASH_VIDEO;
        } else {
            return MediaType.OTHER;
        }
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return getTitle() + ":" + getType() + ":" + getDate() + ":" + getId();
    }
}
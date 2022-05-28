package com.example.media;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;


public interface MediaManager extends Serializable {
    
    public void createMediaItem(MediaItem item, InputStream content) throws IOException;
    
    public MediaItem getMediaItem(String id) throws FileNotFoundException ;
    
    public void updateMediaItem(MediaItem item) throws FileNotFoundException;
    
    public void deleteMediaItem(String id);
    
    public List<MediaGroup> listMediaItems(MediaQualifier filter) throws FileNotFoundException;
}

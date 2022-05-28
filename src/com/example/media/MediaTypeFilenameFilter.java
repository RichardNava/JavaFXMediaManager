package com.example.media;

import java.io.File;
import java.io.FilenameFilter;

public class MediaTypeFilenameFilter implements FilenameFilter {

    private MediaQualifier mediaQualifier = null;
    
    public MediaTypeFilenameFilter(MediaQualifier mediaQualifier) {
        this.mediaQualifier = mediaQualifier;
    }
    
    @Override
    public boolean accept(File dir, String name) {
        StringBuilder regexParts = new StringBuilder();
        for (MediaType type : mediaQualifier.getTypes()) {
            switch (type) {
                case OGV_VIDEO:
                    regexParts.append(".*ogv$|");
                    break;
                case MP4_VIDEO:
                    regexParts.append(".*mpg4$|.*mp4$|.*m4v$|");
                    break;
                case FLASH_VIDEO:
                    regexParts.append(".*flv$|");
                    break;
                case IMAGE:
                    regexParts.append(".*jpg$|.*jpeg$|.*png$|.*gif$|");
                    break;
            }
        }
        regexParts.deleteCharAt(regexParts.length() - 1);
        
        if (name.toLowerCase().matches(regexParts.toString())) {
            File f = new File(dir, name);
            if (f.isFile()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
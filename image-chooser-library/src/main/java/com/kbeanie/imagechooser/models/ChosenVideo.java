/*******************************************************************************
 * Copyright 2013 Kumar Bibek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kbeanie.imagechooser.models;

import android.net.Uri;

import com.kbeanie.imagechooser.exceptions.ChooserException;

public class ChosenVideo extends ChosenMedia {

    //private String videoPreviewImage;
    private String videoFilePath;
    private String thumbnailPath;


    // Getters and setters

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    /*public String getVideoPreviewImage() {
        return videoPreviewImage;
    }

    public void setVideoPreviewImage(String videoPreviewImage) {
        this.videoPreviewImage = videoPreviewImage;
    }*/

    @Override
    public String getMediaHeight() {
        return "-1"; // HACK
        //return getHeight(videoPreviewImage);
    }

    @Override
    public String getMediaWidth() {
        return "-1"; // HACK
        //return getWidth(videoPreviewImage);
    }

    @Override
    public Uri getThumbUri() {
        return ChosenMedia.toFileUri(thumbnailPath);
    }

    public String getExtension() throws ChooserException {
        return getFileExtension(videoFilePath);
    }
}

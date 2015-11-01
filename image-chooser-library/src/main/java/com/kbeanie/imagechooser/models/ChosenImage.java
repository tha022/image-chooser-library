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


public class ChosenImage extends ChosenMedia {

    private Uri imageUri;
    private String filePathOriginal;


    public ChosenImage(String filePathOriginal, Uri imageUri) {
        this.filePathOriginal = filePathOriginal;
        this.imageUri = imageUri;
    }

    // Getters and setters

    public String getFilePathOriginal() {
        return filePathOriginal;
    }

    public void setFilePathOriginal(String filePathOriginal) {
        this.filePathOriginal = filePathOriginal;
    }

    public String getExtension() throws ChooserException {
        return getFileExtension(filePathOriginal);
    }

    @Override
    public String getMediaHeight() throws ChooserException {
        return getHeight(filePathOriginal);
    }

    @Override
    public String getMediaWidth() throws ChooserException {
       return getWidth(filePathOriginal);
    }

    @Override
    public Uri getMediaUri() {
        return imageUri;
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.IMAGE;
    }

    @Override
    public Uri getThumbUri() {
        return imageUri;
    }
}

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

package com.kbeanie.imagechooser.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.factory.DateFactory;
import static com.kbeanie.imagechooser.api.MediaChooserManager.*;

public class FileUtils {

    final static String TAG = FileUtils.class.getSimpleName();

    /**
     * Returns the path of the folder specified in external storage
     * @param
     * @return
     */
    /*@SuppressLint("SetWorldWritable")
    public static String getDirectory(Context context, String foldername) throws ChooserException {

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(foldername, Context.MODE_PRIVATE);
        if(!directory.setWritable(true, false)) { // then we dont need to ask for the external storage permission.
            throw new ChooserException("Could not set directory writable = "+directory);
        }
        Log.d(TAG, "directory = "+directory);
        //File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
        //        + File.separator + foldername);
        if (!directory.exists()) {
            if(!directory.mkdirs()){
                throw new ChooserException("Chould not create directory = "+directory);
            }
        }
        return directory.getAbsolutePath();
    }*/


    private static File testTmpFile;

    @VisibleForTesting
    public static void setTestTmpFile(File testTmpFile) {
        FileUtils.testTmpFile = testTmpFile;
    }

    @VisibleForTesting
    public static void resetTestTmpFile() {
        FileUtils.testTmpFile = null;
    }

    public static File createTmpFile(String extension) throws ChooserException {

        if(FileUtils.testTmpFile != null) {
            Log.d(TAG, "In test mode, returning test tmp file = "+FileUtils.testTmpFile);
            return FileUtils.testTmpFile;
        }

        // Create an image file name
        //String timeStamp = DateFactory.getInstance().getTimeInMillis()+"";
        String imageFileName =  "tmp_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                getEnvFolder(extension));
        File image;
        try {
            image = File.createTempFile(
                    imageFileName, /* prefix */
                    "."+extension, /* suffix */
                    storageDir     /* directory */
            );
        } catch (IOException e) {
            throw new ChooserException(e);
        }

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d(TAG, "Tmp file = "+image.getAbsolutePath());
        return image;
    }

    public static String createTmpFilePath(String extension) throws ChooserException {
        return createTmpFile(extension).getAbsolutePath();
    }

    public static File getTmpDir(String extension) {
        return Environment.getExternalStoragePublicDirectory(
                getEnvFolder(extension));
    }

    public static String getTmpDirString(String extension) {
        return Environment.getExternalStoragePublicDirectory(
                getEnvFolder(extension)).getAbsolutePath();
    }

    public static File getTmpDirPictures() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
    }

    public static File getTmpDirMovies() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
    }

    private static String getEnvFolder(String extension) {
        if(extension == null ||
                extension.equals(IMAGE_EXTENSION)) {
            return Environment.DIRECTORY_PICTURES;
        }
        return Environment.DIRECTORY_MOVIES;
    }

    public static String getFileExtension(String filename) throws ChooserException {
       try {
           return filename.substring(filename.lastIndexOf(".") + 1);
       } catch (StringIndexOutOfBoundsException e) {
           throw new ChooserException(e);
       }
    }

}

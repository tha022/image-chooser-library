/**
 * ****************************************************************************
 * Copyright 2013 Kumar Bibek
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package com.kbeanie.imagechooser.api;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;

import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.factory.UriFactory;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;
import com.kbeanie.imagechooser.utils.BChooserPreferences;
import com.kbeanie.imagechooser.utils.FileUtils;

import static com.kbeanie.imagechooser.helpers.StreamHelper.closeSilent;
import static com.kbeanie.imagechooser.helpers.StreamHelper.verifyCursor;

public abstract class BChooser {

    static final String TAG = BChooser.class.getSimpleName();

    protected Activity activity;

    protected Fragment fragment;
    protected android.app.Fragment appFragment;
    //protected int type;
    protected String foldername;
    protected String filePathOriginal;
    protected Bundle extras;

    protected MediaChooserListener listener;


    public BChooser(Activity activity) {
        this.activity = activity;
        initDirector(activity.getApplicationContext());
    }

    public BChooser(Fragment fragment) {
        this.fragment = fragment;
        initDirector(fragment.getActivity().getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BChooser(android.app.Fragment fragment) {
        this.appFragment = fragment;
        initDirector(fragment.getActivity().getApplicationContext());
    }

    /**
     * Call this method, to start the chooser, i.e, The camera app or the
     * gallery depending upon the type.
     * <p>
     * Returns the path, in case, a capture is requested. You will need to save
     * this path, so that, in case, the ChooserManager is destoryed due to
     * activity lifecycle, you will use this information to create the
     * ChooserManager instance again
     * </p>
     * <p>
     * In case of picking a video or image, null would be returned.
     * </p>
     *
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public abstract String choose(int type) throws ChooserException;

    /**
     * Call this method to process the result from within your onActivityResult
     * method. You don't need to do any processing at all. Just pass in the
     * request code and the data, and everything else will be taken care of.
     *
     * @param requestCode
     * @param data
     */
    public abstract void submit(int requestCode, Intent data) throws ChooserException;

    protected void checkDirectory() throws ChooserException {
        File directory;
        directory = new File(FileUtils.getDirectory(foldername));
        if (!directory.exists()) {
            if(!directory.mkdirs() && !directory.isDirectory()) {
                throw new ChooserException("Error creating directory: "+directory);
            }
        }
    }

    @SuppressLint("NewApi")
    protected void startActivity(Intent intent, int requestCode) {
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else if (appFragment != null) {
            appFragment.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * This method should be used to re-initialize the ChooserManagers in case your activity of
     * fragments are destroyed for some reason, and you need to recreate this object in onActivityResult
     *
     * @param path
     */
    public void reinitialize(String path) {
        filePathOriginal = path;
    }

    @SuppressLint("NewApi")
    protected Context getContext() {
        if (activity != null) {
            return activity.getApplicationContext();
        } else if (fragment != null) {
            return fragment.getActivity().getApplicationContext();
        } else if (appFragment != null) {
            return appFragment.getActivity().getApplicationContext();
        }
        return null;
    }

    protected boolean wasVideoSelected(Intent data) {
        if (data == null) {
            return false;
        }

        if (data.getType() != null && data.getType().startsWith("video")) {
            return true;
        }

        ContentResolver cR = getContext().getContentResolver();
        String type = cR.getType(data.getData());
        return type != null && type.startsWith("video");

    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    /**
     * Utility method which quickly looks up the file size. Use this, if you want to set a limit to
     * the media chosen, and which your application can safely handle.
     * <p/>
     * For example, you might not want a video of 1 GB to be imported into your app.
     *
     * @param uri
     * @param context
     * @return
     */
    public long queryProbableFileSize(Uri uri, Context context) {

        if (uri.toString().startsWith("file")) {
            File file = new File(uri.getPath());
            return file.length();
        }
        else if (uri.toString().startsWith("content")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                verifyCursor(uri, cursor);
                if(cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
                return 0;
            } catch (ChooserException e) {
                return 0;
            } finally {
                closeSilent(cursor);
            }
        }

        return 0;
    }

    /**
     * Set a listener, to get callbacks when the videos and the thumbnails are
     * processed
     *
     * @param listener
     */
    public void setMediaChooserListener(MediaChooserListener listener) {
        this.listener = listener;
    }

    private void initDirector(Context context){
        BChooserPreferences preferences = new BChooserPreferences(context);
        foldername = preferences.getFolderName();
    }

    protected String buildFilePathOriginal(String foldername, String extension) throws ChooserException {
        return UriFactory.getInstance().getFilePathOriginal(foldername, extension);
    }

    protected Uri buildCaptureUri(String filePathOriginal) {
        return Uri.fromFile(new File(filePathOriginal));
    }
}

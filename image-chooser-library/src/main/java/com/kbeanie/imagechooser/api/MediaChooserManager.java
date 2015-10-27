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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.kbeanie.imagechooser.BuildConfig;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.helpers.MediaHelper;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;
import com.kbeanie.imagechooser.models.ChooserType;
import com.kbeanie.imagechooser.models.ChosenImage;
import com.kbeanie.imagechooser.models.ChosenVideo;
import com.kbeanie.imagechooser.threads.VideoProcessorListener;
import com.kbeanie.imagechooser.threads.VideoProcessorThread;
import com.kbeanie.imagechooser.utils.MediaResourceUtils;

/**
 * Easy Media Chooser Library for Android Apps. Forget about coding workarounds
 * for different devices, OSes and folders.
 *
 * @author Beanie
 */
public class MediaChooserManager extends BChooser implements VideoProcessorListener {

    private final static String TAG = MediaChooserManager.class.getSimpleName();

    private MediaChooserListener listener;

    /**
     * Simplest constructor. Specify the type
     * {@link ChooserType}
     *
     * @param activity
     * @param type
     */
    public MediaChooserManager(Activity activity, int type) {
        super(activity, type);
    }

    public MediaChooserManager(Fragment fragment, int type) {
        super(fragment, type);
    }

    public MediaChooserManager(android.app.Fragment fragment, int type) {
        super(fragment, type);
    }

    /**
     * Set a listener, to get callbacks when the medias and the thumbnails are
     * processed
     *
     * @param listener
     */
    public void setMediaChooserListener(MediaChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public String choose() throws ChooserException {
        if (listener == null) {
            throw new ChooserException(
                    "MediaChooserListener cannot be null. Forgot to set MediaChooserListener???");
        }
        switch (type) {
            case ChooserType.REQUEST_PICK_PICTURE:
                pickImage();
                return null;
            case ChooserType.REQUEST_CAPTURE_PICTURE:
                this.filePathOriginal = capturePicture();
                return filePathOriginal;
            case ChooserType.REQUEST_PICK_VIDEO:
                pickVideo();
                return null;
            case ChooserType.REQUEST_CAPTURE_VIDEO:
                this.filePathOriginal = captureVideo();
                return filePathOriginal;
            default:
                throw new ChooserException(String.format("Unknown request type = %s", type));
        }
    }

    private void pickImage() throws ChooserException {
        pickMedia("image/*");
    }

    private void pickVideo() throws ChooserException {
        pickMedia("video/*");
    }

    private void pickMedia(String type) throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.setType(type);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String capturePicture() throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String path = buildFilePathOriginal(foldername, "jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildCaptureUri(path));
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);

            return path;
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String captureVideo() throws ChooserException {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.GINGERBREAD
                && sdk <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return captureVideoPatchedMethodForGingerbread();
        } else {
            return captureVideoCurrent();
        }
    }

    private String captureVideoCurrent() throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            String path = buildFilePathOriginal(foldername, "mp4");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildCaptureUri(path));
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);

            return path;
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String captureVideoPatchedMethodForGingerbread() throws ChooserException {
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);
            return null;
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }


    // onActivityResult

    @Override
    public void submit(int requestCode, Intent data) throws ChooserException {
        switch (requestCode) {
            case ChooserType.REQUEST_PICK_PICTURE:
                processImageFromGallery(data);
                break;
            case ChooserType.REQUEST_CAPTURE_PICTURE:
                onProcessedImage(new ChosenImage(filePathOriginal));
                break;
            case ChooserType.REQUEST_PICK_VIDEO:
                processVideoFromGallery(data);
                break;
            case ChooserType.REQUEST_CAPTURE_VIDEO:
                processCameraVideo(data);
                break;
            default:
                onError("onActivityResult requestCode is different from the type the chooser was initialized with.");
                break;
        }
    }

    @SuppressLint("NewApi")
    private void processImageFromGallery(Intent data) {
        if (data != null && data.getDataString() != null) {
            String uri = data.getData().toString();
            String path = MediaHelper.sanitizeURI(uri);
            if (path == null || TextUtils.isEmpty(path)) {
                onError("File path was null");
                return;
            }
            onProcessedImage(new ChosenImage(path));
        } else {
            onError("Image Uri was null!");
        }
    }

    @SuppressLint("NewApi")
    private void processVideoFromGallery(Intent data) {
        if (data != null && data.getDataString() != null) {
            String uri = data.getData().toString();
            String path = MediaHelper.sanitizeURI(uri);
            if (path == null || TextUtils.isEmpty(path)) {
                onError("File path was null");
                return;
            }

            //String ronkPath = Uri.parse(path).getPath();
            //Log.i(TAG, "ronkPath = "+ronkPath);

            VideoProcessorThread thread = new VideoProcessorThread(
                    MediaResourceUtils.getInstance(getContext()),
                    path, foldername);
            thread.setListener(this);
            thread.start();
        }
    }

    @SuppressLint("NewApi")
    private void processCameraVideo(Intent intent) {
        String path;
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.GINGERBREAD
                && sdk <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            path = intent.getDataString();
        } else {
            path = filePathOriginal;
        }
        // String ronkPath = Uri.parse(path).getPath();
        // Log.i(TAG, "ronkPath = "+ronkPath);
        VideoProcessorThread thread = new VideoProcessorThread(
                MediaResourceUtils.getInstance(getContext()),
                path,
                foldername);
        thread.setListener(this);
        thread.start();
    }

    public void onProcessedImage(ChosenImage image) {
        if (listener != null) {
            listener.onImageChosen(image);
        }
    }

    @Override
    public void onProcessedVideo(ChosenVideo video) {
        if (listener != null) {
            listener.onVideoChosen(video);
        }
    }

    @Override
    public void onError(String reason) {
        if (listener != null) {
            listener.onError(reason);
        }
    }
}


/*private void chooseMedia() throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.setType("video/*, image/*");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }*/
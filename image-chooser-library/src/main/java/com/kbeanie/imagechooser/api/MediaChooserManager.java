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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.helpers.MediaHelper;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;
import com.kbeanie.imagechooser.models.ChooserType;
import com.kbeanie.imagechooser.models.ChosenImage;
import com.kbeanie.imagechooser.models.ChosenMedia;
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

    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private Handler handlerMainThread = new Handler(Looper.getMainLooper());

    private MediaChooserListener listener;
    /**
     * Simplest constructor. Specify the type
     * {@link ChooserType}
     *
     * @param activity
     */
    public MediaChooserManager(Activity activity) {
        super(activity);
    }

    public MediaChooserManager(Fragment fragment) {
        super(fragment);
    }

    public MediaChooserManager(android.app.Fragment fragment) {
        super(fragment);
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
    public String choose(int requestCode) throws ChooserException {
        if (listener == null) {
            throw new ChooserException(
                    "MediaChooserListener cannot be null. Forgot to set MediaChooserListener???");
        }
        //this.type = type;
        switch (requestCode) {
            case ChooserType.REQUEST_PICK_PICTURE:
                pickImage(requestCode);
                return null;
            case ChooserType.REQUEST_CAPTURE_PICTURE:
                this.filePathOriginal = capturePicture(requestCode);
                return filePathOriginal;
            case ChooserType.REQUEST_PICK_VIDEO:
                pickVideo(requestCode);
                return null;
            case ChooserType.REQUEST_CAPTURE_VIDEO:
                this.filePathOriginal = captureVideo(requestCode);
                return filePathOriginal;
            default:
                throw new ChooserException(String.format("Unknown requestCode = %s", requestCode));
        }
    }

    private void pickImage(int requestCode) throws ChooserException {
        pickMedia("image/*", requestCode);
    }

    private void pickVideo(int requestCode) throws ChooserException {
        pickMedia("video/*", requestCode);
    }

    private void pickMedia(String type, int requestCode) throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.setType(type);
            startActivity(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String capturePicture(int requestCode) throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String path = buildFilePathOriginal(foldername, IMAGE_EXTENSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildCaptureUri(path));
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent, requestCode);

            return path;
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String captureVideo(int requestCode) throws ChooserException {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.GINGERBREAD
                && sdk <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return captureVideoPatchedMethodForGingerbread(requestCode);
        } else {
            return captureVideoCurrent(requestCode);
        }
    }

    private String captureVideoCurrent(int requestCode) throws ChooserException {
        checkDirectory();
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            String path = buildFilePathOriginal(foldername, VIDEO_EXTENSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildCaptureUri(path));
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent, requestCode);

            return path;
        } catch (ActivityNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    private String captureVideoPatchedMethodForGingerbread(int requestCode) throws ChooserException {
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent, requestCode);
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
                onProcessedImage(new ChosenImage(filePathOriginal, ChosenMedia.toFileUri(filePathOriginal)));
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
            onProcessedImage(new ChosenImage(path, Uri.parse(path)));
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

            VideoProcessorThread thread = new VideoProcessorThread(getContext(),
                    new MediaResourceUtils(getContext()),
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

        VideoProcessorThread thread = new VideoProcessorThread(getContext(),
                new MediaResourceUtils(getContext()),
                path,
                foldername);
        thread.setListener(this);
        thread.start();
    }

    public void onProcessedImage(final ChosenImage image) {
        if (listener != null) {
            handlerMainThread.post(new Runnable() {
                @Override
                public void run() {
                    listener.onImageChosen(image);
                }
            });
        }
    }

    @Override
    public void onProcessedVideo(final ChosenVideo video) {
        if (listener != null) {
            handlerMainThread.post(new Runnable() {
                @Override
                public void run() {
                    listener.onVideoChosen(video);
                }
            });
        }
    }

    @Override
    public void onError(final String reason) {
        if (listener != null) {
            handlerMainThread.post(new Runnable() {
                @Override
                public void run() {
                    listener.onError(reason);
                }
            });
        }
    }
}
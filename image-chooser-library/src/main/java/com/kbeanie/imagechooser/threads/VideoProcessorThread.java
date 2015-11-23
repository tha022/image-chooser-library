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

package com.kbeanie.imagechooser.threads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kbeanie.imagechooser.helpers.MediaHelper;
import com.kbeanie.imagechooser.models.ChosenVideo;
import com.kbeanie.imagechooser.utils.FileUtils;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.utils.MediaResourceUtils;

import static com.kbeanie.imagechooser.helpers.StreamHelper.*;


public class VideoProcessorThread extends Thread {

    private final static String TAG = VideoProcessorThread.class.getSimpleName();

    private VideoProcessorListener listener;

    protected Context context;
    protected MediaResourceUtils mediaResourceUtils;
    protected String filePath;
    protected String foldername;


    public VideoProcessorThread(Context context, MediaResourceUtils mediaResourceUtils, String filePath, String foldername) {
        this.context = context;
        this.mediaResourceUtils = mediaResourceUtils;
        this.filePath = filePath;
        this.foldername = foldername;
    }

    public void setListener(VideoProcessorListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            mediaResourceUtils.manageDirectoryCache("mp4", foldername);
            processVideo(filePath, foldername);
        } catch (Exception e) { // catch all, just to be sure we can send message back to listener in all circumenstances.
            Log.e(TAG, e.getMessage(), e);
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        }
    }

    private void processVideo(String path, String foldername) throws ChooserException {
        String filePath = mediaResourceUtils.getFilePath(path, foldername);
        process(filePath, foldername);
    }

    protected void process(String filePath, String foldername) throws ChooserException {
        String thumbnailPath = MediaHelper.getThumnailPath(createThumbnailOfVideo(filePath, foldername));
        processingDone(filePath, thumbnailPath);
    }

    private String createThumbnailOfVideo(String filePath, String foldername) throws ChooserException {

        Log.d(TAG, "createThumbnailOfVideo, filePath = " + filePath + " foldername = " + foldername);

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
        if (bitmap == null) {
            throw new ChooserException("Cant generate thumbnail for filePath = "+filePath);
        }
        String thumbnailPath = FileUtils.getDirectory(context, foldername) + File.separator
                + Calendar.getInstance().getTimeInMillis() + ".jpg";
        File file = new File(thumbnailPath);

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            return thumbnailPath;
        } catch(IOException e) {
            throw new ChooserException(e);
        } finally {
            flush(stream);
        }
    }

    // @Override
    protected void processingDone(String original, String thumbnail) {
        if (listener != null) {
            ChosenVideo video = new ChosenVideo();
            video.setVideoFilePath(original);
            video.setThumbnailPath(thumbnail);
            // video.setVideoPreviewImage(previewImage);
            listener.onProcessedVideo(video);
        }
    }
}

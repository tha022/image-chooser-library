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

package com.beanie.imagechooserapp;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.kbeanie.imagechooser.api.MediaChooserManager;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.models.ChooserType;
import com.kbeanie.imagechooser.models.ChosenImage;
import com.kbeanie.imagechooser.models.ChosenVideo;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;


public class VideoChooserActivity extends BasicActivity implements
        MediaChooserListener {

    private String TAG = VideoChooserActivity.class.getSimpleName();

    private MediaChooserManager videoChooserManager;

    private ProgressBar pbar;

    private ImageView imageViewThumb;
    private VideoView videoView;

    private String filePath;

    private int chooserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chooser);

        pbar = (ProgressBar) findViewById(R.id.pBar);
        pbar.setVisibility(View.GONE);

        imageViewThumb = (ImageView) findViewById(R.id.imageViewThumbnail);

        videoView = (VideoView) findViewById(R.id.videoView);
    }

    public void captureVideo(View view) {
        chooserType = ChooserType.REQUEST_CAPTURE_VIDEO;
        videoChooserManager = new MediaChooserManager(this,
                ChooserType.REQUEST_CAPTURE_VIDEO);
        videoChooserManager.setMediaChooserListener(this);
        try {
            pbar.setVisibility(View.VISIBLE);
            filePath = videoChooserManager.choose();
            Log.d(TAG, "filePath = "+filePath);
        } catch (ChooserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void pickVideo(View view) {
        chooserType = ChooserType.REQUEST_PICK_VIDEO;
        videoChooserManager = new MediaChooserManager(this,
                ChooserType.REQUEST_PICK_VIDEO);
        videoChooserManager.setMediaChooserListener(this);
        try {
            videoChooserManager.choose();
            pbar.setVisibility(View.VISIBLE);
        } catch (ChooserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onVideoChosen(final ChosenVideo video) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pbar.setVisibility(View.GONE);
                if (video != null) {
                    videoView.setVideoURI(Uri.parse(new File(video
                            .getVideoFilePath()).toString()));
                    videoView.start();
                    imageViewThumb.setImageURI(Uri.parse(new File(video
                            .getThumbnailPath()).toString()));
                }
            }
        });
    }

    @Override
    public void onImageChosen(ChosenImage image) {

    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pbar.setVisibility(View.GONE);
                Toast.makeText(VideoChooserActivity.this, reason,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_CAPTURE_VIDEO || requestCode == ChooserType.REQUEST_PICK_VIDEO)) {
            if (videoChooserManager == null) {
                reinitializeVideoChooser();
            }
            try {
                videoChooserManager.submit(requestCode, data);
            } catch (ChooserException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            pbar.setVisibility(View.GONE);
        }
    }

    // Should be called if for some reason the VideoChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeVideoChooser() {
        videoChooserManager = new MediaChooserManager(this, chooserType);
        videoChooserManager.setMediaChooserListener(this);
        videoChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("chooser_type")) {
                chooserType = savedInstanceState.getInt("chooser_type");
            }

            if (savedInstanceState.containsKey("media_path")) {
                filePath = savedInstanceState.getString("media_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}

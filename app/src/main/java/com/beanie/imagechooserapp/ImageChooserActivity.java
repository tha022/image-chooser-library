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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.MediaChooserManager;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.models.ChooserType;
import com.kbeanie.imagechooser.models.ChosenImage;
import com.kbeanie.imagechooser.models.ChosenVideo;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImageChooserActivity extends BasicActivity implements
        MediaChooserListener {

    private final static String TAG = "ICA";

    private TextView textViewFile;

    private MediaChooserManager imageChooserManager;

    private ProgressBar pbar;

    private String filePath;

    private int chooserType;

    private boolean isActivityResultOver = false;

    private String originalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Activity Created");
        setContentView(R.layout.activity_image_chooser);

        Button buttonTakePicture = (Button) findViewById(R.id.buttonTakePicture);
        buttonTakePicture.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        Button buttonChooseImage = (Button) findViewById(R.id.buttonChooseImage);
        buttonChooseImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        textViewFile = (TextView) findViewById(R.id.textViewFile);

        pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(View.GONE);
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new MediaChooserManager(this);
        imageChooserManager.setMediaChooserListener(this);
        try {
            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose(ChooserType.REQUEST_PICK_PICTURE);
        } catch (ChooserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new MediaChooserManager(this);
        imageChooserManager.setMediaChooserListener(this);
        try {
            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose(ChooserType.REQUEST_CAPTURE_PICTURE);
        } catch (ChooserException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "OnActivityResult");
        Log.i(TAG, "File Path : " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            try {
                imageChooserManager.submit(requestCode, data);
            } catch (ChooserException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            pbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "Chosen Image: O - " + image.getFilePathOriginal());
                isActivityResultOver = true;
                originalFilePath = image.getFilePathOriginal();
                pbar.setVisibility(View.GONE);
                if (image != null) {
                    Log.i(TAG, "Chosen Image: Is not null");
                    textViewFile.setText(image.getFilePathOriginal());
                } else {
                    Log.i(TAG, "Chosen Image: Is null");
                }
            }
        });
    }

    private void loadImage(ImageView iv, final String path) {
        Picasso.with(ImageChooserActivity.this)
                .load(Uri.fromFile(new File(path)))
                .fit()
                .centerInside()
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Picasso Success Loading Thumbnail - " + path);
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, "Picasso Error Loading Thumbnail Small - " + path);
                    }
                });
    }

    @Override
    public void onVideoChosen(ChosenVideo video) {

    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "OnError: " + reason);
                pbar.setVisibility(View.GONE);
                Toast.makeText(ImageChooserActivity.this, reason,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new MediaChooserManager(this);
        imageChooserManager.setMediaChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "Saving Stuff");
        Log.i(TAG, "File Path: " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        outState.putBoolean("activity_result_over", isActivityResultOver);
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        outState.putString("orig", originalFilePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("chooser_type")) {
                chooserType = savedInstanceState.getInt("chooser_type");
            }
            if (savedInstanceState.containsKey("media_path")) {
                filePath = savedInstanceState.getString("media_path");
            }
            if (savedInstanceState.containsKey("activity_result_over")) {
                isActivityResultOver = savedInstanceState.getBoolean("activity_result_over");
                originalFilePath = savedInstanceState.getString("orig");
            }
        }
        Log.i(TAG, "Restoring Stuff");
        Log.i(TAG, "File Path: " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        Log.i(TAG, "Activity Result Over: " + isActivityResultOver);
        if (isActivityResultOver) {
            populateData();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void populateData() {
        Log.i(TAG, "Populating Data");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity Destroyed");
    }
}

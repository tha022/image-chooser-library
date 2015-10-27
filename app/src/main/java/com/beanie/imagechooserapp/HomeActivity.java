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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.kbeanie.imagechooser.utils.BChooserPreferences;

import java.io.File;

import io.fabric.sdk.android.Fabric;


public class HomeActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_home);

        // One time call to setup the folder to be used for all files
        BChooserPreferences preferences = new BChooserPreferences(getApplicationContext());
        preferences.setFolderName("ICL");
    }

    int PICK_IMAGE_REQUEST = 1;

    public void goToRonk(View view) {

        // Intent intent = new Intent(Intent.ACTION_PICK);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //intent.setType("video/*, image/*");
        //intent.setType("video/*");

        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        // Intent intent = new Intent(MediaStore.ACTION_GET_CONTENT);
        // String filePathOriginal = getDirectory("thomas");
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, buildCaptureUri(filePathOriginal));
        /* if (extras != null) {
            intent.putExtras(extras);
        } */
        //startActivity(intent);
    }

    protected Uri buildCaptureUri(String filePathOriginal) {
        return Uri.fromFile(new File(filePathOriginal));
    }

    public static String getDirectory(String foldername) {
        File directory = null;
        directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + foldername);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }

    public void gotoImageChooserFragment(View view) {
        Intent intent = new Intent(this, FragmentImageChooserActivity.class);
        startActivity(intent);
    }

    public void gotoImageChooser(View view) {
        Intent intent = new Intent(this, ImageChooserActivity.class);
        startActivity(intent);
    }

    public void gotoVideoChooser(View view) {
        Intent intent = new Intent(this, VideoChooserActivity.class);
        startActivity(intent);
    }

    public void gotoMediaChooser(View view) {
        Intent intent = new Intent(this, MediaChooserActivity.class);
        startActivity(intent);
    }
}

package com.kbeanie.imagechooser.factory;

import android.app.Activity;

import com.kbeanie.imagechooser.api.BChooser;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.MediaChooserListener;
import com.kbeanie.imagechooser.api.VideoChooserManager;
import com.kbeanie.imagechooser.exceptions.CreationException;

/**
 * Created by vervik on 9/27/15.
 */
public class ChooserManagerFactory {


    public static BChooser newInstance(Activity activity, int type) {
        return newInstance(activity, type, null);
    }

    public static BChooser newInstance(Activity activity, int type, String filePath) {
        if(!(activity instanceof MediaChooserListener)) {
            throw new CreationException("Activity must implement MediaChooserListener");
        }
        return newInstance(activity, (MediaChooserListener) activity, type, filePath);
    }

    public static BChooser newInstance(Activity activity, MediaChooserListener listener, int type, String filePath) {
        BChooser manager;
        switch (type) {
            case ChooserType.REQUEST_PICK_VIDEO:
            case ChooserType.REQUEST_CAPTURE_VIDEO:
                manager = new VideoChooserManager(activity, type);
                break;
            case ChooserType.REQUEST_PICK_PICTURE:
            case ChooserType.REQUEST_CAPTURE_PICTURE:
                manager = new ImageChooserManager(activity, type);
                break;
            default:
                throw new CreationException("Not valid type = "+type);
        }

        manager.setMediaChooserListener(listener);
        manager.reinitialize(filePath);
        return manager;
    }
}

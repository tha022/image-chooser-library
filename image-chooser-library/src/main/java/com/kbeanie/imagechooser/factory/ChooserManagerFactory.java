package com.kbeanie.imagechooser.factory;

import android.app.Activity;

import com.kbeanie.imagechooser.api.BChooser;
import com.kbeanie.imagechooser.api.MediaChooserManager;
import com.kbeanie.imagechooser.models.ChooserType;
import com.kbeanie.imagechooser.listeners.MediaChooserListener;
import com.kbeanie.imagechooser.exceptions.CreationException;

/**
 * Created by vervik on 9/27/15.
 */
public class ChooserManagerFactory {


    public static BChooser newInstance(Activity activity) {
        return newInstance(activity, null);
    }

    public static BChooser newInstance(Activity activity, String filePath) {
        if(!(activity instanceof MediaChooserListener)) {
            throw new CreationException("Activity must implement MediaChooserListener");
        }
        return newInstance(activity, (MediaChooserListener) activity, filePath);
    }

    public static BChooser newInstance(Activity activity, MediaChooserListener listener, String filePath) {
        BChooser manager = new MediaChooserManager(activity);
        manager.setMediaChooserListener(listener);
        manager.reinitialize(filePath);
        return manager;
    }
}

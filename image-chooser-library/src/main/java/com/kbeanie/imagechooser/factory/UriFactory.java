package com.kbeanie.imagechooser.factory;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.kbeanie.imagechooser.utils.FileUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by vervik on 9/27/15.
 */
public class UriFactory {

    static String TAG = UriFactory.class.getSimpleName();

    /**
     * If set, it will be the temp URI where the camera app should save the captured image / video to
     *
     * intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
     */
    private String filePathOriginal;


    private UriFactory() {
        // private
    }


    /*public void setFilePathOriginal(String filePathOriginal) {
        Log.d(TAG, "File path set. Is: " + filePathOriginal);
        this.filePathOriginal = filePathOriginal;
    }*/

    public String getFilePathOriginal(Context context, String foldername, String extension) throws ChooserException {
        if(filePathOriginal != null) {
            Log.d(TAG, "File path set. We return: "+filePathOriginal);
            return filePathOriginal;
        }

        return FileUtils.createTmpFile(extension).getAbsolutePath();
        // return FileUtils.getDirectory(context, foldername)
        //        + File.separator + DateFactory.getInstance().getTimeInMillis() + "." + extension;
    }

    public void reset() {
        Log.d(TAG, "We reset capture URI");
        this.filePathOriginal = null;
    }


    private static UriFactory instance;

    public static UriFactory getInstance() {
        if(instance == null) {
            instance = new UriFactory();
        }
        return instance;
    }

    /*
    filePathOriginal = FileUtils.getDirectory(foldername)
                    + File.separator + DateFactory.getInstance().getTimeInMillis() //Calendar.getInstance().getTimeInMillis()
                    + ".jpg";
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(filePathOriginal)));
     */
}

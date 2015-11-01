
package com.kbeanie.imagechooser.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;

import com.kbeanie.imagechooser.utils.FileUtils;
import com.kbeanie.imagechooser.exceptions.ChooserException;

public abstract class ChosenMedia {

    protected SoftReference<Bitmap> getBitmap(String path) throws ChooserException {
        try {
            return new SoftReference<>(BitmapFactory.decodeStream(new FileInputStream(
                    new File(path))));
        } catch (FileNotFoundException e) {
            throw new ChooserException(e);
        }
    }

    public String getFileExtension(String path) throws ChooserException {
        return FileUtils.getFileExtension(path);
    }
    
    protected String getWidth(String path) throws ChooserException {
        String width = "";
        try {
            ExifInterface exif = new ExifInterface(path);
            width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            if (width.equals("0")) {
                width = Integer.toString(getBitmap(path).get().getWidth());
            }
        } catch (IOException e) {
            throw new ChooserException(e);
        }
        return width;
    }
    
    protected String getHeight(String path) throws ChooserException {
        String height = "";
        try {
            ExifInterface exif = new ExifInterface(path);
            height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            if (height.equals("0")) {
                height = Integer.toString(getBitmap(path).get().getHeight());
            }
        } catch (IOException e) {
            throw new ChooserException(e);
        }
        return height;
    }

    public static Uri toFileUri(String filePath) {
        return Uri.fromFile(new File(filePath));
    }

    public abstract String getMediaHeight() throws ChooserException;
    
    public abstract String getMediaWidth() throws ChooserException;

    public abstract Uri getThumbUri();

    public abstract Uri getMediaUri();
}

package com.kbeanie.imagechooser.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.kbeanie.imagechooser.BuildConfig;
import com.kbeanie.imagechooser.exceptions.ChooserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.kbeanie.imagechooser.helpers.StreamHelper.close;
import static com.kbeanie.imagechooser.helpers.StreamHelper.flush;
import static com.kbeanie.imagechooser.helpers.StreamHelper.verifyBitmap;

/**
 * Created by vervik on 10/25/15.
 */
public class MediaHelper {

    public final static String TAG = MediaHelper.class.getSimpleName();

    private final static int THUMBNAIL_BIG = 1;
    private final static int THUMBNAIL_SMALL = 2;


    public static String[] createThumbnails(String image) throws ChooserException {
        String[] images = new String[2];
        images[0] = getThumnailPath(image);
        images[1] = getThumbnailSmallPath(image);
        return images;
    }

    public static String getThumnailPath(String file) throws ChooserException {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Compressing ... THUMBNAIL");
        }
        return compressAndSaveImage(file, THUMBNAIL_BIG);
    }

    public static String getThumbnailSmallPath(String file) throws ChooserException {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Compressing ... THUMBNAIL SMALL");
        }
        return compressAndSaveImage(file, THUMBNAIL_SMALL);
    }

    public static String compressAndSaveImage(String fileImage, int scale) throws ChooserException {

        FileOutputStream stream = null;

        try {
            ExifInterface exif = new ExifInterface(fileImage);
            String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            String length = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "File name: "+fileImage+" scale: "+scale);
                Log.i(TAG, "Before: " + width + "x" + length);
            }

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = -90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            int w = Integer.parseInt(width);
            int l = Integer.parseInt(length);

            int what = w > l ? w : l;

            BitmapFactory.Options options = new BitmapFactory.Options();
            if (what > 1500) {
                options.inSampleSize = scale * 4;
            } else if (what > 1000 && what <= 1500) {
                options.inSampleSize = scale * 3;
            } else if (what > 400 && what <= 1000) {
                options.inSampleSize = scale * 2;
            } else {
                options.inSampleSize = scale;
            }
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Scale: " + (what / options.inSampleSize));
                Log.i(TAG, "Rotate: " + rotate);
            }
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage, options);
            verifyBitmap(fileImage, bitmap);

            File original = new File(fileImage);
            File file = new File(
                    (original.getParent() + File.separator + original.getName()
                            .replace(".", "_fact_" + scale + ".")));
            stream = new FileOutputStream(file);
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, false);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            if (BuildConfig.DEBUG) {
                ExifInterface exifAfter = new ExifInterface(
                        file.getAbsolutePath());
                String widthAfter = exifAfter
                        .getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                String lengthAfter = exifAfter
                        .getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "After: " + widthAfter + "x" + lengthAfter);
                }
            }

            return file.getAbsolutePath();

        } catch (IOException e) {
            throw new ChooserException(e);
        } finally {
            flush(stream);
            close(stream);
        }
    }

    // Change the URI only when the returned string contains "file:/" prefix.
    // For all the other situations the URI doesn't need to be changed
    public static String sanitizeURI(String uri) {
        String filePathOriginal = uri;
        // Picasa on Android < 3.0
        if (uri.matches("https?://\\w+\\.googleusercontent\\.com/.+")) {
            filePathOriginal = uri;
        }
        // Local storage
        if (uri.startsWith("file://")) {
            filePathOriginal = uri.substring(7);
        }
        return filePathOriginal;
    }

    /*private String createPreviewImage() throws IOException {
        String previewImage = null;
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        if (bitmap != null) {
            previewImage = FileUtils.getDirectory(foldername) + File.separator
                    + Calendar.getInstance().getTimeInMillis() + ".jpg";
            File file = new File(previewImage);
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
        }
        return previewImage;
    }*/
}

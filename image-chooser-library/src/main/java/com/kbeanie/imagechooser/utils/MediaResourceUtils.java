package com.kbeanie.imagechooser.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.kbeanie.imagechooser.exceptions.ChooserException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import static com.kbeanie.imagechooser.helpers.StreamHelper.close;
import static com.kbeanie.imagechooser.helpers.StreamHelper.closeSilent;
import static com.kbeanie.imagechooser.helpers.StreamHelper.flush;
import static com.kbeanie.imagechooser.helpers.StreamHelper.verifyStream;

/**
 * Created by vervik on 10/27/15.
 */
public class MediaResourceUtils {

    String TAG = MediaResourceUtils.class.getSimpleName();

    // 500 MB Cache size
    protected final static int MAX_DIRECTORY_SIZE = 500 * 1024 * 1024;
    // Number of days to preserve 10 days
    protected final static int MAX_THRESHOLD_DAYS = (int) (10 * 24 * 60 * 60 * 1000);

    private Context context;
    private ContentResolver contentResolver;

    public MediaResourceUtils(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    @SuppressLint("NewApi")
    public String getAbsoluteImagePathFromUri(Uri imageUri) {
        String[] proj = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        if (imageUri.toString().startsWith(
                "content://com.android.gallery3d.provider")) {
            imageUri = Uri.parse(imageUri.toString().replace(
                    "com.android.gallery3d", "com.google.android.gallery3d"));
        }

        String filePath = "";
        String imageUriString = imageUri.toString();
        if (imageUriString.startsWith("content://com.google.android.gallery3d")
                || imageUriString
                .startsWith("content://com.google.android.apps.photos.content")
                || imageUriString
                .startsWith("content://com.android.providers.media.documents")
                || imageUriString
                .startsWith("content://com.google.android.apps.docs.storage")
                || imageUriString
                .startsWith("content://com.microsoft.skydrive.content.external")
                || imageUriString
                .startsWith("content://com.android.externalstorage.documents")
                || imageUriString
                .startsWith("content://com.android.internalstorage.documents")) {
            filePath = imageUri.toString();
        } else {
            Cursor cursor = contentResolver.query(imageUri, proj,
                    null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            cursor.close();
        }

        if (filePath == null && isDownloadsDocument(imageUri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                filePath = getPath(imageUri);
        }
        return filePath;
    }

    public String processPicasaMedia(String path, String extension, String foldername) throws ChooserException {
        InputStream inputStream = null;
        BufferedOutputStream outStream = null;

        try {
            inputStream = context.getContentResolver()
                    .openInputStream(Uri.parse(path));

            verifyStream(path, inputStream);

            String filePath = FileUtils.getDirectory(context, foldername) + File.separator
                    + Calendar.getInstance().getTimeInMillis() + extension;

            outStream = new BufferedOutputStream(new FileOutputStream(filePath));
            byte[] buf = new byte[2048];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }

            return filePath;
            //process();
        } catch (IOException e) {
            throw new ChooserException(e);
        } finally {
            close(inputStream);
            close(outStream);
        }
    }

    public String processGooglePhotosMedia(String path, String extension, String foldername) throws ChooserException {
        String retrievedExtension = checkExtension(Uri.parse(path));
        if (retrievedExtension != null
                && !TextUtils.isEmpty(retrievedExtension)) {
            extension = "." + retrievedExtension;
        }

        InputStream inputStream = null;
        BufferedOutputStream outStream = null;

        try {

            String filePath = FileUtils.getDirectory(context, foldername) + File.separator
                    + Calendar.getInstance().getTimeInMillis() + extension;

            ParcelFileDescriptor parcelFileDescriptor = context
                    .getContentResolver().openFileDescriptor(Uri.parse(path),
                            "r");

            verifyStream(path, parcelFileDescriptor);

            FileDescriptor fileDescriptor = parcelFileDescriptor
                    .getFileDescriptor();

            inputStream = new FileInputStream(fileDescriptor);

            BufferedInputStream reader = new BufferedInputStream(inputStream);

            outStream = new BufferedOutputStream(
                    new FileOutputStream(filePath));
            byte[] buf = new byte[2048];
            int len;
            while ((len = reader.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }
            return filePath;
            // process();
        } catch (IOException e) {
            throw new ChooserException(e);
        } finally {
            flush(outStream);
            close(outStream);
            close(inputStream);
        }
    }

    public String checkExtension(Uri uri) {

        String extension = "";

        // The query, since it only applies to a single document, will only
        // return
        // one row. There's no need to filter, sort, or select fields, since we
        // want
        // all fields for one document.
        Cursor cursor = context.getContentResolver().query(uri, null, null,
                null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy
            // for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file
                // name.
                String displayName = cursor.getString(cursor
                        .getColumnIndex(OpenableColumns.DISPLAY_NAME));
                int position = displayName.indexOf(".");
                extension = displayName.substring(position + 1);
                Log.i(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null. But since
                // an
                // int can't be null in Java, the behavior is
                // implementation-specific,
                // which is just a fancy term for "unpredictable". So as
                // a rule, check if it's null before assigning to an int. This
                // will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but
                    // cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            closeSilent(cursor);
        }
        return extension;
    }

    public String processContentProviderMedia(String path, String extension, String foldername)
            throws ChooserException {
        checkExtension(Uri.parse(path));

        InputStream inputStream = null;
        BufferedOutputStream outStream = null;

        try {
            inputStream = context.getContentResolver()
                    .openInputStream(Uri.parse(path));
            verifyStream(path, inputStream);

            String filePath = FileUtils.getDirectory(context, foldername) + File.separator
                    + Calendar.getInstance().getTimeInMillis() + extension;

            outStream = new BufferedOutputStream(new FileOutputStream(filePath));
            byte[] buf = new byte[2048];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }

            return filePath;
            //process();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ChooserException(e);
        } finally {
            close(inputStream);
            close(outStream);
        }
    }

    public String getFilePath(String filePath, String foldername) throws ChooserException {
        // Picasa on Android >= 3.0
        if (filePath != null && filePath.startsWith("content:")) {
            filePath = getAbsoluteImagePathFromUri(Uri.parse(filePath));
        }
        if (filePath == null || TextUtils.isEmpty(filePath)) {
            throw new ChooserException("Couldn't process a null file");
            /* if (listener != null) {
                listener.onError("Couldn't process a null file");
            } */
        } /*else if (filePath.startsWith("http")) {
            mediaResourceUtils.downloadAndProcess(filePath);
        }*/
        else if (filePath
                .startsWith("content://com.google.android.gallery3d")
                || filePath
                .startsWith("content://com.microsoft.skydrive.content.external")) {
            return processPicasaMedia(filePath, ".mp4", foldername);
        } else if (filePath
                .startsWith("content://com.google.android.apps.photos.content")
                || filePath
                .startsWith("content://com.android.providers.media.documents")
                || filePath
                .startsWith("content://com.google.android.apps.docs.storage")) {
            return processGooglePhotosMedia(filePath, ".mp4", foldername);
        } else if (filePath.startsWith("content://media/external/video")) {
            return processContentProviderMedia(filePath, ".mp4", foldername);
        } else {
            return filePath;
        }
    }

    public void manageDirectoryCache(final String extension, String foldername) throws ChooserException {
        /*if (!clearOldFiles) {
            return;
        }*/
        File directory;
        directory = new File(FileUtils.getDirectory(context, foldername));
        File[] files = directory.listFiles();
        long count = 0;
        if (files == null) {
            return;
        }
        for (File file : files) {
            count = count + file.length();
        }

        if (count > MAX_DIRECTORY_SIZE) {
            final long today = Calendar.getInstance().getTimeInMillis();
            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (today - pathname.lastModified() > MAX_THRESHOLD_DAYS
                            && pathname
                            .getAbsolutePath()
                            .toUpperCase(Locale.ENGLISH)
                            .endsWith(
                                    extension
                                            .toUpperCase(Locale.ENGLISH))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };

            File[] filterFiles = directory.listFiles(filter);
            int deletedFileCount = 0;
            for (File file : filterFiles) {
                deletedFileCount++;
                if(!file.delete()) {
                    Log.w(TAG, "Could not delete file = "+file);
                }
            }
            Log.i(TAG, "Deleted " + deletedFileCount + " files");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getPath(final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}

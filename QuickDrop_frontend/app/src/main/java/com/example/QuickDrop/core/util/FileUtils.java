package com.example.QuickDrop.core.util;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static String handleUri(Context context, Uri uri) {
        String type = context.getContentResolver().getType(uri);
        if (type == null) return null;

        String extension = getFileExtensionFromType(type);
        if (extension == null) return null;

        File dir = new File(context.getCacheDir(), "dir_name");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File outputFile = new File(dir, System.currentTimeMillis() + extension);
        copyStreamToFile(context, uri, outputFile);
        return outputFile.getAbsolutePath();
    }

    private static String getFileExtensionFromType(String type) {
        switch (type) {
            case "image/jpeg":
                return ".jpeg";
            case "image/png":
                return ".png";
            case "image/jpg":
                return ".jpg";
            default:
                return null;
        }
    }

    private static void copyStreamToFile(Context context, Uri uri, File outputFile) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4 * 1024]; // buffer size
            int byteCount;
            while ((byteCount = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

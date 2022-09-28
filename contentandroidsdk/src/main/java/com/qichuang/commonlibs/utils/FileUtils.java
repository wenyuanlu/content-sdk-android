
package com.qichuang.commonlibs.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class FileUtils {

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            bmp.compress(Bitmap.CompressFormat.PNG, 30, output);
            return output.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

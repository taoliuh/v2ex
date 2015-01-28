package com.sonaive.v2ex.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liutao on 1/16/15.
 */
public class ImageUtils {

    public static final String TAG = ImageUtils.class.getSimpleName();
    /**
     * 获取缩放的bitmap
     * @param filePath
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getScaledBitmap(String filePath, int maxWidth, int maxHeight) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap;
        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        Log.d(TAG, "Actual width: " + actualWidth + ", actual height: " + actualHeight);
        // Then compute the dimensions we would ideally like to decode to.
        int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                actualWidth, actualHeight);
        int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                actualHeight, actualWidth);
        Log.d(TAG, "Desired width: " + desiredWidth + ", desired height: " + desiredHeight);

        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
        // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
        decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        Bitmap tempBitmap = BitmapFactory.decodeFile(filePath, decodeOptions);
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                tempBitmap.getHeight() > desiredHeight)) {
            bitmap = Bitmap.createScaledBitmap(tempBitmap,
                    desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
        } else {
            bitmap = tempBitmap;
        }
        return bitmap;
    }

    /**
     * 获取缩放的bitmap
     * @param context
     * @param imageResId
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getScaledBitmap(Context context, int imageResId, int maxWidth, int maxHeight) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), imageResId, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        Log.d(TAG, "Actual width: " + actualWidth + ", actual height: " + actualHeight);

        // Then compute the dimensions we would ideally like to decode to.
        int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                actualWidth, actualHeight);
        int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                actualHeight, actualWidth);
        Log.d(TAG, "Desired width: " + desiredWidth + ", desired height: " + desiredHeight);

        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
        // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
        decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), imageResId, decodeOptions);
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                tempBitmap.getHeight() > desiredHeight)) {
            bitmap = Bitmap.createScaledBitmap(tempBitmap,
                    desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
        } else {
            bitmap = tempBitmap;
        }
        return bitmap;
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    public static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary Maximum size of the primary dimension (i.e. width for
     *        max width), or zero to maintain aspect ratio with secondary
     *        dimension
     * @param maxSecondary Maximum size of the secondary dimension, or zero to
     *        maintain aspect ratio with primary dimension
     * @param actualPrimary Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     */
    public static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                          int actualSecondary) {
        // If no dominant value at all, just return the actual.
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    /**
     * 获取图片实际尺寸
     * @param imagePath
     * @return
     */
    public static int[] getActualImageDimension(String imagePath) {
        int[] imageSize = new int[2];
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        imageSize[0] = actualWidth;
        imageSize[1] = actualHeight;
        return imageSize;
    }

    /**
     * 获取图片实际尺寸
     * @param imageResId
     * @return
     */
    public static int[] getActualImageDimension(Context context, int imageResId) {
        int[] imageSize = new int[2];
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), imageResId, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        imageSize[0] = actualWidth;
        imageSize[1] = actualHeight;
        return imageSize;
    }

    /**
     * 根据显示的最大宽度或最大高度，保持原图的宽高比缩放
     * @param imagePath 图片路径
     * @param maxWidth  图片显示的最大宽度
     * @param maxHeight 图片显示的最大高度
     * @return
     */
    public static int[] getDesiredImageDimension(String imagePath, int maxWidth, int maxHeight) {
        int[] desiredImageDimension = new int[2];
        int[] actualImageDimension = getActualImageDimension(imagePath);
        Log.d(TAG, "Actual width: " + actualImageDimension[0] + ", actual height: " + actualImageDimension[1]);
        int maxPrimary;
        int maxSecondary;
        if (actualImageDimension[0] >= actualImageDimension[1]) {
            maxPrimary = maxWidth;
            maxSecondary = 0;
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
            desiredImageDimension[1] = getResizedDimension(maxSecondary, maxPrimary, actualImageDimension[1], actualImageDimension[0]);
        } else {
            maxPrimary = maxHeight;
            maxSecondary = 0;
            desiredImageDimension[1] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[1], actualImageDimension[0]);
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
        }
        Log.d(TAG, "Desired width: " + desiredImageDimension[0] + ", desired height: " + desiredImageDimension[1]);
        return desiredImageDimension;
    }

    /**
     * 根据显示的最大宽度或最大高度，保持原图的宽高比缩放
     * @param imageResId 图片资源id
     * @param maxWidth  图片显示的最大宽度
     * @param maxHeight 图片显示的最大高度
     * @return
     */
    public static int[] getDesiredImageDimension(Context context, int imageResId, int maxWidth, int maxHeight) {
        int[] desiredImageDimension = new int[2];
        int[] actualImageDimension = getActualImageDimension(context, imageResId);
        Log.d(TAG, "Actual width: " + actualImageDimension[0] + ", actual height: " + actualImageDimension[1]);
        int maxPrimary;
        int maxSecondary;
        if (actualImageDimension[0] >= actualImageDimension[1]) {
            maxPrimary = maxWidth;
            maxSecondary = 0;
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
            desiredImageDimension[1] = getResizedDimension(maxSecondary, maxPrimary, actualImageDimension[1], actualImageDimension[0]);
        } else {
            maxPrimary = maxHeight;
            maxSecondary = 0;
            desiredImageDimension[1] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[1], actualImageDimension[0]);
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
        }
        Log.d(TAG, "Desired width: " + desiredImageDimension[0] + ", desired height: " + desiredImageDimension[1]);
        return desiredImageDimension;
    }

    public static int[] getDesiredImageDimension(int[] dimension, int maxWidth, int maxHeight) {
        int[] desiredImageDimension = new int[2];
        int[] actualImageDimension = dimension;
        Log.d(TAG, "Actual width: " + actualImageDimension[0] + ", actual height: " + actualImageDimension[1]);
        int maxPrimary;
        int maxSecondary;
        if (actualImageDimension[0] >= actualImageDimension[1]) {
            maxPrimary = maxWidth;
            maxSecondary = 0;
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
            desiredImageDimension[1] = getResizedDimension(maxSecondary, maxPrimary, actualImageDimension[1], actualImageDimension[0]);
        } else {
            maxPrimary = maxHeight;
            maxSecondary = 0;
            desiredImageDimension[1] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[1], actualImageDimension[0]);
            desiredImageDimension[0] = getResizedDimension(maxPrimary, maxSecondary, actualImageDimension[0], actualImageDimension[1]);
        }
        Log.d(TAG, "Desired width: " + desiredImageDimension[0] + ", desired height: " + desiredImageDimension[1]);
        return desiredImageDimension;
    }

    public static void compress(String path, int maxWidth, int maxHeight) {
        FileOutputStream out;
        try {
            Bitmap scaledBitmap = getScaledBitmap(path, maxWidth, maxHeight);
            out = new FileOutputStream(path);

            // write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            File file = new File(path);
//            Log.d(TAG, "Image size is : " + file.length() / 1024 + "kb");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static void watermark(Context context, String srcPath, int watermarkRes, int maxWidth, int maxHeight, int quality) {

        FileOutputStream out;
        try {
            Bitmap scaledBitmap = getScaledBitmap(srcPath, maxWidth, maxHeight);
            Bitmap rotatedBitmap = rotateBitmap(getBitmapDegree(srcPath), scaledBitmap);
            Bitmap scaledWatermark = getScaledBitmap(context, watermarkRes, maxWidth, maxHeight);
            out = new FileOutputStream(srcPath);

            Canvas canvas = new Canvas(rotatedBitmap);
            canvas.drawBitmap(scaledWatermark, 0, 0, null);

            // write the compressed bitmap at the destination specified by filename.
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
//            File file = new File(srcPath);
//            Log.d(TAG, "Image size is : " + file.length() / 1024 + "kb");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Add watermark result in OOM");
            e.printStackTrace();
        }
    }

    public static int getBitmapDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}

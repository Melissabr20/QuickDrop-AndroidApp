package com.example.QuickDrop.core.util;
import android.graphics.PorterDuff;
import android.widget.ImageView;

public class ImageColorChanger {

    public static void changeImageColor(ImageView imageView, int color) {
        imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}

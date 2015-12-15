package by.khrapovitsky.model;

import android.graphics.Bitmap;

public class BitmapAndPath {

    private Bitmap bitmap;
    private String path;

    public BitmapAndPath() {
    }

    public BitmapAndPath(Bitmap bitmap, String path) {
        this.bitmap = bitmap;
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

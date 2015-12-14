package by.khrapovitsky.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import by.khrapovitsky.R;

public class NotesCursorAdapter extends ResourceCursorAdapter {

    private Context context;
    private LruCache<String, Bitmap> mLruCache;

    public NotesCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        this.context = context;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView idNote = (TextView) view.findViewById(R.id.idNote);
        ImageView imageNote = (ImageView) view.findViewById(R.id.idImageNote);
        TextView note = (TextView) view.findViewById(R.id.note);
        TextView lastDateModify = (TextView) view.findViewById(R.id.lastDateModify);
        Integer id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow("noteText"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("lastDateModify"));
        idNote.setText(id.toString());
        note.setText(noteText);
        String path = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
        if(path==null)
            path = "-1";
        Bitmap bitmap = getBitmapFromMemCache(path);
        if(bitmap==null){
            bitmap =  getImage(path);
            imageNote.setImageBitmap(bitmap);
        }else{
            imageNote.setImageBitmap(bitmap);
        }
        lastDateModify.setText(date);
    }

    private Bitmap getImage(String path) {
        Bitmap bitmap = null;
        if(path!=null){
            if(!path.equals("-1")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(Uri.parse(path).getPath(), options);
                final int REQUIRED_SIZE = 80;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(Uri.parse(path).getPath(), options);
                addBitmapToMemoryCache(path, bitmap);
            }
        }
        if(bitmap == null)
            bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_no_image);
            addBitmapToMemoryCache("-1",bitmap);
        return bitmap;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }

}

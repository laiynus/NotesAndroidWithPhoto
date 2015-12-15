package by.khrapovitsky.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import by.khrapovitsky.model.BitmapAndPath;


public class RetainedFragment extends Fragment {

    private BitmapAndPath data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(BitmapAndPath data) {
        this.data = data;
    }

    public BitmapAndPath getData() {
        return data;
    }
}

package com.example.jingj.criminalintent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class photoFragment extends DialogFragment {

    private static final String ARG_FILE = "file";
    public ImageView photoImage;

    public static photoFragment newInstance(File file) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_FILE, file);
        photoFragment ph = new photoFragment();
        ph.setArguments(bundle);
        return ph;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment, container, false);
        photoImage = view.findViewById(R.id.dialog_photo);
        File file = (File) getArguments().getSerializable(ARG_FILE);
        Bitmap bitmap = PictureUtils.getFullBitmap(file.getPath());
        photoImage.setImageBitmap(bitmap);
        return view;
    }
}

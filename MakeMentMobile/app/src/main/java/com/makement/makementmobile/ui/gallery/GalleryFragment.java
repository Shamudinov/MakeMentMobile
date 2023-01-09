package com.makement.makementmobile.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.makement.makementmobile.MainActivity;
import com.makement.makementmobile.TimerActivity;
import com.makement.makementmobile.databinding.ActivityMainBinding;
import com.makement.makementmobile.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private ActivityMainBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = ActivityMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.makement.makementmobile;

import android.content.Context;
import android.widget.ArrayAdapter;


import androidx.annotation.NonNull;


public class TaskAdapter extends ArrayAdapter<String> {
    boolean isEnable = false;

    public TaskAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
    }


    @Override
    public boolean isEnabled(int position) {
        if (!isEnable)
            return super.isEnabled(position);
        else
            return false;
    }
}

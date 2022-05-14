package com.threeklines.ordertimesheet.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.threeklines.ordertimesheet.fragments.FragmentProcessed;
import com.threeklines.ordertimesheet.fragments.FragmentProcessing;

public class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new FragmentProcessing();
        return new FragmentProcessed();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

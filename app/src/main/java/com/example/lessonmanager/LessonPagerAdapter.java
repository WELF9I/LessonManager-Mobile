package com.example.lessonmanager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LessonPagerAdapter extends FragmentStateAdapter {

    public LessonPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return upcoming or completed lessons fragment based on position
        return LessonsFragment.newInstance(position == 0 ? "upcoming" : "completed");
    }

    @Override
    public int getItemCount() {
        return 2; // We have 2 tabs
    }
}
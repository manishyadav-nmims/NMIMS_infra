package com.nmims.app.Fragments.Academic;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.TabAdapter;
import com.nmims.app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class StudentNewsEventFragment extends Fragment {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout facultyAttendanceFrag;
    private NewsFragment newsFragment;
    private FragmentManager manager;
    private EventsFragment eventsFragment;
    private FragmentTransaction transaction;

    public StudentNewsEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_faculty_attendance, container, false);
        setRetainInstance(true);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        facultyAttendanceFrag = view.findViewById(R.id.facultyAttendanceFrag);
        facultyAttendanceFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont delete
            }
        });

        adapter = new TabAdapter(getChildFragmentManager());
        adapter.addFragment(new NewsFragment(), "NEWS");
        adapter.addFragment(new EventsFragment(), "EVENTS");
        ((StudentDrawer)getActivity()).setActionBarTitle("News And Events");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        ((StudentDrawer)getActivity()).showAnnouncements(false);

        return view;
    }
}

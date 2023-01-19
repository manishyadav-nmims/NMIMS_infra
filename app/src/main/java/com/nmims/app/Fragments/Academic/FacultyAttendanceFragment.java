package com.nmims.app.Fragments.Academic;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.TabAdapter;
import com.nmims.app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FacultyAttendanceFragment extends Fragment {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout facultyAttendanceFrag;
    private int id = 9;

    public FacultyAttendanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faculty_attendance, container,false );
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        facultyAttendanceFrag = view.findViewById(R.id.facultyAttendanceFrag);
        facultyAttendanceFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        adapter = new TabAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new MarkAttendanceLectureFragment(), "Mark Attendance");
        adapter.addFragment(new ProgramListVAFragment(), "View Attendance");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        ((FacultyDrawer)getActivity()).setActionBarTitle("Attendance");
        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        ((FacultyDrawer)getActivity()).isOpened = true;
        return view;
    }
}

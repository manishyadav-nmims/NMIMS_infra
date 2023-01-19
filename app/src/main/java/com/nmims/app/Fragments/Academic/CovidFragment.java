package com.nmims.app.Fragments.Academic;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.TabAdapter;
import com.nmims.app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CovidFragment extends Fragment {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout covidFrag;
    private int id = 9;

    public CovidFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.covid_fragment, container,false );
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        covidFrag = view.findViewById(R.id.covidFrag);
        covidFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        adapter = new TabAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new CovidTestFragment(), "Take Test");
        adapter.addFragment(new CovidApplyLeave(), "Apply Leave");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        ((StudentDrawer)getActivity()).setActionBarTitle("Covid-19 Test");
        ((StudentDrawer)getActivity()).showAnnouncements(false);

        return view;
    }
}

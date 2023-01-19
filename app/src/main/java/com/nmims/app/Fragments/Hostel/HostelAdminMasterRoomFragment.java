package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Adapters.TabAdapter;
import com.nmims.app.R;

public class HostelAdminMasterRoomFragment extends Fragment
{
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_admin_master_room,container,false);

        viewPager = view.findViewById(R.id.viewPager_hostel_master);
        tabLayout = view.findViewById(R.id.tabLayout_hostel_master);

        adapter = new TabAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new HostelOneFragment(), "HOSTEL I");
        adapter.addFragment(new HostelTwoFragment(), "HOSTEL II");
        adapter.addFragment(new HostelThreeFragment(), "HOSTEL III");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Hostel Room");
        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);
        return view;
    }
}

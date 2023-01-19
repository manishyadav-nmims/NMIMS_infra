package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.R;

public class HostelStudentLeaveFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_leave,container,false);



        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Leave");
        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);
        return view;
    }
}

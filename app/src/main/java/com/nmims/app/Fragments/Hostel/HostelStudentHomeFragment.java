package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Fragments.Academic.SupportFragment;
import com.nmims.app.R;

public class HostelStudentHomeFragment extends Fragment
{

    private TextView studentprofiletv_shh, studentLeavetv_shh, studentsportstv_shh, studentLaundrytv_shh, studentsupporttv, studentComplainttv_shh, studentAttendancetv_shh;
    private ImageView studentprofileic_shh, studentLeaveic_shh, studentsportsic_shh, studentLaundryic_shh, studentsupportic, studentComplaintic_shh, studentAttendanceic_shh;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_home_fragment,container,false);

        studentprofiletv_shh = view.findViewById(R.id.studentprofiletv_shh);
        studentprofileic_shh = view.findViewById(R.id.studentprofileIc_shh);
        studentLeavetv_shh = view.findViewById(R.id.studentLeavetv_shh);
        studentLeaveic_shh = view.findViewById(R.id.studentLeaveic_shh);
        studentsportstv_shh = view.findViewById(R.id.studentsportstv_shh);
        studentsportsic_shh = view.findViewById(R.id.studentsportsic_shh);
        studentLaundrytv_shh = view.findViewById(R.id.studentLaundrytv_shh);
        studentLaundryic_shh = view.findViewById(R.id.studentLaundryic_shh);
        studentsupporttv = view.findViewById(R.id.studentsupporttv);
        studentsupportic = view.findViewById(R.id.studentsupportic);
        studentComplainttv_shh = view.findViewById(R.id.studentComplainttv_shh);
        studentComplaintic_shh = view.findViewById(R.id.studentComplaintic_shh);

        studentAttendancetv_shh = view.findViewById(R.id.studentAttendancetv_shh);
        studentAttendanceic_shh = view.findViewById(R.id.studentAttendanceic_shh);

        studentprofiletv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentProfile();
            }
        });

        studentprofileic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentProfile();
            }
        });

        studentLeavetv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentLeave();
            }
        });

        studentLeaveic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentLeave();
            }
        });

        studentLaundrytv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentLaundry();
            }
        });

        studentLaundryic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentLaundry();
            }
        });

        studentsportsic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentSports();
            }
        });

        studentsportstv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentSports();
            }
        });

        studentsupporttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentSupport();
            }
        });

        studentsupportic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentSupport();
            }
        });

        studentComplainttv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentComplaint();
            }
        });

        studentComplaintic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentComplaint();
            }
        });

        studentAttendanceic_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentAttendance();
            }
        });

        studentAttendancetv_shh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionStudentAttendance();
            }
        });

        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("SHIRPUR");
        ((StudentHostelHomeDrawer)getActivity()).showAcademics(true);

        return view;
    }

    private void actionStudentProfile()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentProfileFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Profile");
        ft.commit();
    }

    private void actionStudentLeave()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelAdminMasterRoomFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Hostel Room");
        ft.commit();
    }

    private void actionStudentSports()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentSportsFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Sports");
        ft.commit();
    }

    private void actionStudentLaundry()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentLaundryFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Laundry");
        ft.commit();
    }

    private void actionStudentSupport()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new SupportFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Support");
        ft.commit();
    }

    private void actionStudentComplaint()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentComplaintFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Complaint");
        ft.commit();
    }

    private void actionStudentAttendance()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentAttendanceFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Attendance");
        ft.commit();
    }
}

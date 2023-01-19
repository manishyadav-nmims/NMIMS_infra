package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;

public class HostelStudentComplaintFragment extends Fragment
{
    private Spinner spinnerComplaint;
    private Button submitStudentHostelComplaintBtn;
    private EditText complaintMessageET;
    private String[] complaintArray = new String[14];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_complaint,   container, false);

        spinnerComplaint = view.findViewById(R.id.spinnerComplaint);
        submitStudentHostelComplaintBtn = view.findViewById(R.id.submitStudentHostelComplaintBtn);
        complaintMessageET = view.findViewById(R.id.complaintMessageET);

        complaintArray[0] = "Electrical";
        complaintArray[1] = "Carpentry";
        complaintArray[2] = "Plumbing";
        complaintArray[3] = "AC and water cooler";
        complaintArray[4] = "IT complaint";
        complaintArray[5] = "Mess complaint";
        complaintArray[6] = "Discipline related complaint";
        complaintArray[7] = "Personal problem related complaint";
        complaintArray[8] = "Medical complaint";
        complaintArray[9] = "Cleanliness related complaint";
        complaintArray[10] = "Laundry complaint";
        complaintArray[11] = "Sports complaint";
        complaintArray[12] = "Saloon complaint";
        complaintArray[13] = "Entertainment related complaint";
        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(),R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, complaintArray);
        spinnerComplaint.setAdapter(adapter);

        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Complaint");
        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);

        submitStudentHostelComplaintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String type = spinnerComplaint.getSelectedItem().toString();
                String complaintMsg = complaintMessageET.getText().toString().trim();
                if(!TextUtils.isEmpty(complaintMsg))
                {
                    submitComplaint(complaintMsg,type);
                }
                else
                {
                    new MyToast(getContext()).showSmallCustomToast("Please describe your complaint");
                }
            }
        });

        return view;
    }

    private void submitComplaint(String complaintMsg, String type)
    {
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("complaintMsg",complaintMsg);
            new MyLog(NMIMSApplication.getAppContext()).debug("complaintType",type);
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("submitComplaintEx",e.getMessage());
            e.printStackTrace();
        }
    }
}

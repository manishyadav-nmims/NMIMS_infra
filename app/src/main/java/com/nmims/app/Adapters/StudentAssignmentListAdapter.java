package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.StudentAssignmentDataModel;
import com.nmims.app.R;

import java.util.ArrayList;

public class StudentAssignmentListAdapter extends ArrayAdapter<StudentAssignmentDataModel> {

    private final ArrayList<StudentAssignmentDataModel> dataSet;
    private Context context;
    private int lastPosition = -1;
    private OpenStudentAssignment openStudentAssignment;

    public StudentAssignmentListAdapter(ArrayList<StudentAssignmentDataModel> data, Context context, OpenStudentAssignment openStudentAssignment) {
        super(context, R.layout.student_assignment_list, data);
        this.dataSet = data;
        this.context = context;
        this.openStudentAssignment = openStudentAssignment;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(@NonNull final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        StudentAssignmentDataModel studentAssignmentDataModel = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.student_assignment_list, null);
            viewHolder = new ViewHolder();
            viewHolder.assignmentName = convertView.findViewById(R.id.assignmentName);
            viewHolder.assignmentCourseName = convertView.findViewById(R.id.assignmentCourseName);
            viewHolder.assignmentEndDate = convertView.findViewById(R.id.assignmentEndDate);
            viewHolder.assignmentStatus = convertView.findViewById(R.id.assignmentStatus);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.assignmentName.setText(studentAssignmentDataModel.getAssignmentName());
        viewHolder.assignmentCourseName.setText( studentAssignmentDataModel.getAssignmentCourse());
        new MyLog(NMIMSApplication.getAppContext()).debug("getAssignmentEndDate",studentAssignmentDataModel.getAssignmentEndDate());
        String trimmedDate = "NA";
        try
        {
            if(!studentAssignmentDataModel.getAssignmentEndDate().equalsIgnoreCase("NA"))
            {
                trimmedDate = studentAssignmentDataModel.getAssignmentEndDate().substring(0,11);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("trimmedDateEXXXX",e.getMessage());
        }
        new MyLog(NMIMSApplication.getAppContext()).debug("trimmedDate",trimmedDate);
        viewHolder.assignmentEndDate.setText(trimmedDate);
        if (studentAssignmentDataModel.getAssignmentStatus().toLowerCase().startsWith("complete"))
        {
            viewHolder.assignmentStatus.setText(("Completed"));
            viewHolder.assignmentStatus.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
        }
        else if (studentAssignmentDataModel.getAssignmentStatus().toLowerCase().startsWith("pending"))
        {
            viewHolder.assignmentStatus.setText("Pending");
            viewHolder.assignmentStatus.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        }
        else
        {
            viewHolder.assignmentStatus.setText("Submitted");
            viewHolder.assignmentStatus.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStudentAssignment.openStudentAssignmentDetails(dataSet.get(position));
            }
        });

        return convertView;
    }

    public class ViewHolder {
        TextView assignmentName,assignmentCourseName, assignmentEndDate, assignmentStatus;
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public interface OpenStudentAssignment
    {
        void openStudentAssignmentDetails(StudentAssignmentDataModel assignmentData);
    }
}

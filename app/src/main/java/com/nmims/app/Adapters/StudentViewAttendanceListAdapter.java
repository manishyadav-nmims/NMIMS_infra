package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.nmims.app.Models.StudentViewAttendanceDataModel;
import com.nmims.app.R;
import java.util.ArrayList;

public class StudentViewAttendanceListAdapter extends ArrayAdapter<StudentViewAttendanceDataModel> {

    private final ArrayList<StudentViewAttendanceDataModel> dataSet;
    private Context context;
    private int lastPosition = -1;
    StudentViewAttendanceDataModel studentViewAttendanceDataModel;


    public StudentViewAttendanceListAdapter(ArrayList<StudentViewAttendanceDataModel> data, Context context) {
        super(context, R.layout.student_view_attendance_list, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        StudentViewAttendanceDataModel studentViewAttendanceDataModel = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.student_view_attendance_list, null);
            viewHolder = new ViewHolder();
            viewHolder.student_course_name = convertView.findViewById(R.id.student_course_name);
            viewHolder.student_present_count = convertView.findViewById(R.id.student_present_count);
            viewHolder.student_absent_count = convertView.findViewById(R.id.student_absent_count);
            viewHolder.student_lecture_count = convertView.findViewById(R.id.student_lecture_count);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.student_course_name.setText(studentViewAttendanceDataModel.getCourseName());
        viewHolder.student_present_count.setText(studentViewAttendanceDataModel.getPresent_count());
        viewHolder.student_absent_count.setText(studentViewAttendanceDataModel.getAbsent_count());
        viewHolder.student_lecture_count.setText(studentViewAttendanceDataModel.getTotal_count());

        return convertView;
    }
    public class ViewHolder {
        TextView student_course_name,student_present_count, student_absent_count, student_lecture_count;
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}

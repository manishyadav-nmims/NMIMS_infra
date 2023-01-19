package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import java.util.ArrayList;

public class ProgramListAdapterVA extends ArrayAdapter<LecturesDataModel>
{

    private ArrayList<LecturesDataModel> dataSet;
    private Context context;
    private int lastPosition = -1;
    LecturesDataModel lecturesDataModel;
    private ShowStudentAttendance showStudentAttendance;

    public ProgramListAdapterVA(ArrayList<LecturesDataModel> data, Context context, ShowStudentAttendance showStudentAttendance)
    {
        super(context, R.layout.program_and_course_list, data);
        this.dataSet = data;
        this.context = context;
        this.showStudentAttendance = showStudentAttendance;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        lecturesDataModel = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.program_and_course_list, null);
            viewHolder = new ViewHolder();
            viewHolder.programName = convertView.findViewById(R.id.programAndCourseName);
            viewHolder.programAndCourseTime = convertView.findViewById(R.id.programAndCourseTime);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.programName.setText("Lecture On "+lecturesDataModel.getClass_date());
        viewHolder.programAndCourseTime.setText("From "+lecturesDataModel.getStart_time()+" To "+lecturesDataModel.getEnd_time());
        viewHolder.programAndCourseTime.setVisibility(View.VISIBLE);

        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String courseId = null, startTime = null, endTime = null;
                courseId = dataSet.get(position).getCourseId();
                startTime = dataSet.get(position).getStart_time();
                endTime = dataSet.get(position).getEnd_time();

                showStudentAttendance.showAttendance(courseId, startTime, endTime);
            }
        });

        return convertView;
    }

    public class ViewHolder {
        TextView programName, programAndCourseTime;
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public interface ShowStudentAttendance
    {
        void showAttendance(String courseId, String startTime, String endTime);
    }
}

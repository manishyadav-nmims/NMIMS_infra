package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.R;

import java.util.ArrayList;
import java.util.Locale;

public class FacultyViewAttendanceListAdapter extends ArrayAdapter<AttendanceStudentDataModel> {
    private final ArrayList<AttendanceStudentDataModel> dataSet;
    private ArrayList<AttendanceStudentDataModel> arrayList;
    private Context context;
    private int lastPosition = -1;
    AttendanceStudentDataModel attendanceStudentDataModel;
    private NoSearchResult noSearchResult;

    public FacultyViewAttendanceListAdapter(ArrayList<AttendanceStudentDataModel> data, Context context, NoSearchResult noSearchResult) {
        super(context, R.layout.faculty_view_attendance_list, data);
        this.dataSet = data;
        this.context = context;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(dataSet);
        this.noSearchResult = noSearchResult;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FacultyViewAttendanceListAdapter.ViewHolder viewHolder;
        AttendanceStudentDataModel attendanceStudentDataModel = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.faculty_view_attendance_list, null);
            viewHolder = new FacultyViewAttendanceListAdapter.ViewHolder();
            viewHolder.student_name = convertView.findViewById(R.id.student_name);
            viewHolder.student_sapid = convertView.findViewById(R.id.student_sapid);
            viewHolder.student_rollno = convertView.findViewById(R.id.student_rollno);
            viewHolder.student_absent_present = convertView.findViewById(R.id.student_absent_present);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FacultyViewAttendanceListAdapter.ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.student_name.setText(attendanceStudentDataModel.getStudentName());
        viewHolder.student_sapid.setText(attendanceStudentDataModel.getStudentSapId());
        viewHolder.student_rollno.setText(attendanceStudentDataModel.getStudentRollNo());
        if(attendanceStudentDataModel.getStudentAbsentPresent().equals("1"))
        {
            viewHolder.student_absent_present.setText("Present");
            viewHolder.student_absent_present.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
        }
        else
        {
            viewHolder.student_absent_present.setText("Absent");
            viewHolder.student_absent_present.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        }



        //viewHolder.student_absent_present.setText("Present");

        return convertView;
    }
    public class ViewHolder {
        TextView student_name,student_sapid, student_rollno,student_absent_present;

    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public void filter(String charSet)
    {
        charSet = charSet.toLowerCase(Locale.getDefault());
        dataSet.clear();
        if (charSet.length() == 0)
        {
            dataSet.addAll(arrayList);
        }
        else
        {
            for (AttendanceStudentDataModel wp : arrayList)
            {
                String status = "";
                if(wp.getStudentAbsentPresent().equals("1"))
                {
                    status = "Present";
                }
                else
                {
                    status = "Absent";
                }

                if (wp.getStudentName().toLowerCase(Locale.getDefault()).contains(charSet))
                {
                    dataSet.add(wp);
                }
                else if(wp.getStudentRollNo().toLowerCase(Locale.getDefault()).contains(charSet))
                {
                    dataSet.add(wp);
                }
                else if(wp.getStudentSapId().toLowerCase(Locale.getDefault()).contains(charSet))
                {
                    dataSet.add(wp);
                }
                else if(status.toLowerCase(Locale.getDefault()).contains(charSet))
                {
                    dataSet.add(wp);
                }
                else
                {
                    noSearchResult.noSearchResultFound(true);
                }
            }
        }
        notifyDataSetChanged();
        if(dataSet.size() > 0)
        {
            noSearchResult.noSearchResultFound(false);
        }
        else
        {
            noSearchResult.noSearchResultFound(true);
        }
    }

    public interface NoSearchResult
    {
        void noSearchResultFound(boolean visible);
    }
}

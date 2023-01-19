package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.R;

import java.util.ArrayList;
import java.util.Locale;

public class FacultyMarkAttendanceListAdapter extends ArrayAdapter<AttendanceStudentDataModel> {

    private final ArrayList<AttendanceStudentDataModel> dataSet;
    private ArrayList<AttendanceStudentDataModel> arraylist, removearrayList;
    private Context context;
    private int lastPosition = -1;
    AttendanceStudentDataModel attendanceStudentDataModel;
    private NoSearch noSearch;

    public FacultyMarkAttendanceListAdapter(ArrayList<AttendanceStudentDataModel> data, Context context, NoSearch noSearch) {
        super(context, R.layout.faculty_mark_attendance_list, data);
        this.dataSet = data;
        this.context = context;
        this.arraylist = new ArrayList<>();
        this.removearrayList = new ArrayList<>();
        this.removearrayList.addAll(dataSet);
        this.arraylist.addAll(dataSet);
        this.noSearch = noSearch;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        AttendanceStudentDataModel attendanceStudentDataModel = getItem(position);
        if (convertView == null)
        {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.faculty_mark_attendance_list, null);
            viewHolder = new ViewHolder();
            viewHolder.student_name = convertView.findViewById(R.id.student_name);
            viewHolder.row = convertView.findViewById(R.id.row);
            viewHolder.student_sapid = convertView.findViewById(R.id.student_sapid);
            viewHolder.student_rollno = convertView.findViewById(R.id.student_rollno);
            viewHolder.student_absent_present = convertView.findViewById(R.id.student_absent_present);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.student_name.setText(attendanceStudentDataModel.getStudentName());
        viewHolder.student_sapid.setText(attendanceStudentDataModel.getStudentSapId());
        viewHolder.student_rollno.setText(attendanceStudentDataModel.getStudentRollNo());
        if(attendanceStudentDataModel.getStudentAbsentPresent().equals("Present"))
        {
            viewHolder.student_absent_present.setChecked(true);
        }
        else
        {
            viewHolder.student_absent_present.setChecked(false);
        }
        if(attendanceStudentDataModel.getStudentAbsentPresent().equals("Present"))
        {
            viewHolder.row.setBackgroundColor(getContext().getResources().getColor(R.color.colorWhite));
            viewHolder.student_name.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
            viewHolder.student_sapid.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
            viewHolder.student_rollno.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
        }
        else
        {
            viewHolder.row.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
            viewHolder.student_name.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
            viewHolder.student_sapid.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
            viewHolder.student_rollno.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
        }

        return convertView;
    }
    public class ViewHolder {
        TextView student_name,student_sapid, student_rollno;
        CheckBox student_absent_present;
        TableRow row;
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        dataSet.clear();
        if (charText.length() == 0)
        {
            dataSet.addAll(arraylist);
        }
        else
        {
            for (AttendanceStudentDataModel wp : arraylist)
            {
                if (wp.getStudentName().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    dataSet.add(wp);
                }
                else if(wp.getStudentRollNo().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    dataSet.add(wp);
                }
                else if(wp.getStudentSapId().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    dataSet.add(wp);
                }
                else if(wp.getStudentAbsentPresent().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    dataSet.add(wp);
                }
                else
                {
                    noSearch.noSearchFound(true);
                }
            }
        }
        notifyDataSetChanged();
        if(dataSet.size() > 0)
        {
            noSearch.noSearchFound(false);
        }
        else
        {
            noSearch.noSearchFound(true);
        }
    }

    public interface NoSearch
    {
        void noSearchFound(boolean visible);
    }
}

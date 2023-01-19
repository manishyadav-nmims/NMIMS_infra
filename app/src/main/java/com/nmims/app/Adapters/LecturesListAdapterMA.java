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

public class LecturesListAdapterMA extends ArrayAdapter<LecturesDataModel> {

    private final ArrayList<LecturesDataModel> dataSet;
    private Context context;
    private int lastPosition = -1;
    LecturesDataModel lecturesDataModel;
    String JsonDATA;

    public LecturesListAdapterMA(ArrayList<LecturesDataModel> data, Context context) {
        super(context, R.layout.program_and_course_list, data);
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
        LecturesDataModel lecturesDataModel = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.lectures_list, null);
            viewHolder = new ViewHolder();
            viewHolder.courseName = convertView.findViewById(R.id.courseName);
            viewHolder.lectureTime = convertView.findViewById(R.id.lectureTime);
            viewHolder.programName = convertView.findViewById(R.id.programName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;
        viewHolder.programName.setText(lecturesDataModel.getProgramName());
        viewHolder.courseName.setText(lecturesDataModel.getCourseName());
        viewHolder.lectureTime.setText(lecturesDataModel.getStart_time() +" To "+lecturesDataModel.getEnd_time());

        return convertView;
    }

    public class ViewHolder {
        TextView programName,courseName, lectureTime;
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}

package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Models.LectureList;
import com.nmims.app.R;

import java.util.List;

public class FacultyTimetableListRecyclerviewAdapter extends RecyclerView.Adapter<FacultyTimetableListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<LectureList> facultyTimetableDataModalList;
    private OpenTimetable openTimetable;

    public FacultyTimetableListRecyclerviewAdapter(Context context, List<LectureList> facultyTimetableDataModalList, OpenTimetable openTimetable)
    {
        this.context = context;
        this.facultyTimetableDataModalList = facultyTimetableDataModalList;
        this.openTimetable = openTimetable;
    }
    public FacultyTimetableListRecyclerviewAdapter(Context context){

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.timetable_design, viewGroup, false);
        return new MyViewHolder(itemView, context, facultyTimetableDataModalList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        LectureList facultyTimetableDataModal = facultyTimetableDataModalList.get(i);
        String startTime = facultyTimetableDataModal.start_time;
        startTime = startTime.split(" ")[0].split(":")[0]+":"+startTime.split(" ")[0].split(":")[1];
        String endTime = facultyTimetableDataModal.end_time;
        endTime = endTime.split(" ")[0].split(":")[0]+":"+endTime.split(" ")[0].split(":")[1];
        myViewHolder.startTimeTv.setText(startTime);
        myViewHolder.endTimeTv.setText(endTime);
        myViewHolder.roomNoTv.setText(facultyTimetableDataModal.room_no);
        myViewHolder.courseDetailsTv.setText(facultyTimetableDataModal.event_name);
    }

    @Override
    public int getItemCount() {
        return facultyTimetableDataModalList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<LectureList> facultyTimetableDataModalList;
        private TextView startTimeTv, endTimeTv, roomNoTv, courseDetailsTv;

        public MyViewHolder(@NonNull View itemView,Context context, List<LectureList> facultyTimetableDataModalList)
        {
            super(itemView);
            this.context = context;
            this.facultyTimetableDataModalList = facultyTimetableDataModalList;
            startTimeTv = itemView.findViewById(R.id.startTimeTv);
            endTimeTv = itemView.findViewById(R.id.endTimeTv);
            roomNoTv = itemView.findViewById(R.id.roomNoTv);
            courseDetailsTv = itemView.findViewById(R.id.courseDetailsTv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                openTimetable.openTimetableList(getAdapterPosition());
            }
        }
    }

    public interface OpenTimetable
    {
        void openTimetableList(int index);
    }
}

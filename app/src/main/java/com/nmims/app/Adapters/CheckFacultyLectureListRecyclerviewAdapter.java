package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.FacultyLectureDateDetailsDataModel;
import com.nmims.app.R;

import java.util.List;

public class CheckFacultyLectureListRecyclerviewAdapter extends RecyclerView.Adapter<CheckFacultyLectureListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<FacultyLectureDateDetailsDataModel> lecturesDataModelList;
    private CheckAttendanceMarked checkAttendanceMarked;

    public CheckFacultyLectureListRecyclerviewAdapter(Context context, List<FacultyLectureDateDetailsDataModel> lecturesDataModelList, CheckAttendanceMarked checkAttendanceMarked)
    {
        this.context = context;
        this.lecturesDataModelList = lecturesDataModelList;
        this.checkAttendanceMarked = checkAttendanceMarked;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.lecture_for_faculty_design, viewGroup, false);

        return new MyViewHolder(itemView, context, lecturesDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        FacultyLectureDateDetailsDataModel lecturesDataModel = lecturesDataModelList.get(i);
        myViewHolder.eventId.setText("Event Id : "+lecturesDataModel.getEventId());
        myViewHolder.programId.setText("Program Id : "+lecturesDataModel.getProgramId());
        myViewHolder.facultyId.setText("Faculty Id : "+lecturesDataModel.getFacultyId());
        myViewHolder.startTime.setText("Start Time : "+lecturesDataModel.getStart_time());
        myViewHolder.endTime.setText("End Time : "+lecturesDataModel.getEnd_time());
        myViewHolder.courseName.setText("Course Name : "+lecturesDataModel.getCourseName());
        myViewHolder.programName.setText("Program Name : "+lecturesDataModel.getProgramName());
        myViewHolder.maxEndTime.setText("Max End Time : "+lecturesDataModel.getMaxEndTime());
        myViewHolder.allotedLectures.setText("Alloted Lectures : "+lecturesDataModel.getAllotedLectures());
        myViewHolder.conductedLectures.setText("Conducted Lectures : "+lecturesDataModel.getConductedLectures());
        myViewHolder.remainingLectures.setText("Remaining Lectures : "+lecturesDataModel.getRemainingLectures());
        myViewHolder.attendanceMarked.setText("Attendance Marked : "+lecturesDataModel.getAttendanceMarked());

        if((i%2)==0)
        {
            myViewHolder.detailsContainerCFL.setBackgroundColor(context.getResources().getColor(R.color.colorExtremegrey));
        }
    }

    @Override
    public int getItemCount() {
        return lecturesDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<FacultyLectureDateDetailsDataModel> lecturesDataModelList;
        private TextView eventId, programId, facultyId, startTime, endTime, courseName, programName, maxEndTime, allotedLectures, conductedLectures, remainingLectures, attendanceMarked;
        private LinearLayout detailsContainerCFL;

        public MyViewHolder(@NonNull View itemView,Context context, List<FacultyLectureDateDetailsDataModel> lecturesDataModelList)
        {
            super(itemView);
            this.context = context;
            this.lecturesDataModelList = lecturesDataModelList;
            detailsContainerCFL = itemView.findViewById(R.id.detailsContainerCFL);
            eventId = itemView.findViewById(R.id.eventId);
            programId = itemView.findViewById(R.id.programId);
            facultyId = itemView.findViewById(R.id.facultyId);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);
            courseName = itemView.findViewById(R.id.courseName);
            programName = itemView.findViewById(R.id.programName);
            maxEndTime = itemView.findViewById(R.id.maxEndTime);
            allotedLectures = itemView.findViewById(R.id.allotedLectures);
            conductedLectures = itemView.findViewById(R.id.conductedLectures);
            remainingLectures = itemView.findViewById(R.id.remainingLectures);
            attendanceMarked = itemView.findViewById(R.id.attendanceMarked);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
           if(v.getId() == itemView.getId())
           {
               checkAttendanceMarked.isAttendanceMarked(getAdapterPosition());
               new MyLog(NMIMSApplication.getAppContext()).debug("chekF_Recy","true");
           }
        }
    }

    public interface CheckAttendanceMarked
    {
        void isAttendanceMarked(int position);
    }
}

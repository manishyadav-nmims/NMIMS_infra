package com.nmims.app.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import java.util.List;

public class ProgramListVARecyclerviewAdapter extends RecyclerView.Adapter<ProgramListVARecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<LecturesDataModel> lecturesDataModelList;
    private OpenCourses openCourses;

    public ProgramListVARecyclerviewAdapter(Context context, List<LecturesDataModel> lecturesDataModelList,OpenCourses openCourses)
    {
        this.context = context;
        this.lecturesDataModelList = lecturesDataModelList;
        this.openCourses = openCourses;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.program_and_course_list, viewGroup, false);

        return new MyViewHolder(itemView, context, lecturesDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        LecturesDataModel lecturesDataModel = lecturesDataModelList.get(i);
        myViewHolder.programAndCourseName.setText(lecturesDataModel.getProgramName());
    }

    @Override
    public int getItemCount() {
        return lecturesDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<LecturesDataModel> lecturesDataModelList;
        private TextView programAndCourseName;

        public MyViewHolder(@NonNull View itemView,Context context, List<LecturesDataModel> lecturesDataModelList)
        {
            super(itemView);
            this.context = context;
            this.lecturesDataModelList = lecturesDataModelList;
            programAndCourseName = itemView.findViewById(R.id.programAndCourseName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                String programId = lecturesDataModelList.get(getAdapterPosition()).getProgramId();
                openCourses.openCourselist(programId);
            }
        }
    }

    public interface OpenCourses
    {
        void openCourselist(String programId);
    }
}

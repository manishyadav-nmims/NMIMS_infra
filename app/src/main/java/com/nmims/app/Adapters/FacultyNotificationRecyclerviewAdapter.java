package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Models.FacultyNotificationModel;
import com.nmims.app.R;
import java.util.List;

public class FacultyNotificationRecyclerviewAdapter extends RecyclerView.Adapter<FacultyNotificationRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<FacultyNotificationModel> facultyNotificationModelList;

    public FacultyNotificationRecyclerviewAdapter(Context context, List<FacultyNotificationModel> facultyNotificationModelList)
    {
        this.context = context;
        this.facultyNotificationModelList = facultyNotificationModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.faculty_notification_design, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        FacultyNotificationModel facultyNotificationModel = facultyNotificationModelList.get(i);
        myViewHolder.faculty_notification_msg.setText(facultyNotificationModel.getMessage());
    }

    @Override
    public int getItemCount() {
        return facultyNotificationModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        private TextView faculty_notification_msg;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            faculty_notification_msg = itemView.findViewById(R.id.faculty_notification_msg);
        }
    }

    public void removeItem(int position)
    {
        facultyNotificationModelList.remove(position);
        notifyItemRemoved(position);
    }

}

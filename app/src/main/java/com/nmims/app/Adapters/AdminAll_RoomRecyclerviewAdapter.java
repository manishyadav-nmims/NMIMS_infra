package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Models.RoomDataModel;
import com.nmims.app.R;
import java.util.List;

public class AdminAll_RoomRecyclerviewAdapter extends RecyclerView.Adapter<AdminAll_RoomRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<RoomDataModel> roomDataModelList;
    private LookForStudents lookForStudents;

    public AdminAll_RoomRecyclerviewAdapter( Context context, List<RoomDataModel> roomDataModelList, LookForStudents lookForStudents)
    {
        this.context = context;
        this.roomDataModelList = roomDataModelList;
        this.lookForStudents = lookForStudents;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_room_design, parent, false);
        return new AdminAll_RoomRecyclerviewAdapter.MyViewHolder(itemView, context, roomDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        RoomDataModel roomDataModel = roomDataModelList.get(position);
        holder.selectedRoomNameTV.setText(roomDataModel.getRoomName());
        if(roomDataModel.getIsSelected().equals("Y"))
        {
            holder.selectedRoomView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
        else
        {
            holder.selectedRoomView.setBackgroundColor(context.getResources().getColor(R.color.colorLightGrey));
        }
    }

    @Override
    public int getItemCount() {
        return roomDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<RoomDataModel> roomDataModelList;
        private TextView selectedRoomNameTV;
        private View selectedRoomView;

        public MyViewHolder(View itemView, Context context, List<RoomDataModel> roomDataModelList)
        {
            super(itemView);
            this.context = context;
            this.roomDataModelList = roomDataModelList;

            selectedRoomNameTV = itemView.findViewById(R.id.selectedRoomNameTV);
            selectedRoomView = itemView.findViewById(R.id.selectedRoomView);
        }

        @Override
        public void onClick(View v)
        {
            lookForStudents.searchStudentRoomWise();
        }
    }

    public interface LookForStudents
    {
        void searchStudentRoomWise();
    }
}

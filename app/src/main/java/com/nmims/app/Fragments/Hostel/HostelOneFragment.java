package com.nmims.app.Fragments.Hostel;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Adapters.AdminAll_RoomRecyclerviewAdapter;
import com.nmims.app.Models.RoomDataModel;
import com.nmims.app.R;
import java.util.ArrayList;
import java.util.List;

public class HostelOneFragment extends Fragment
{

    private RecyclerView recyclerViewAllRoom;
    private List<RoomDataModel> roomDataModelList = new ArrayList<>();
    private AdminAll_RoomRecyclerviewAdapter adminAll_roomRecyclerviewAdapter;
    private TextView roomNumberTV;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_one,container,false);

        roomNumberTV = view.findViewById(R.id.roomNumberTV);
        recyclerViewAllRoom = view.findViewById(R.id.recyclerViewAllRoom);
        recyclerViewAllRoom.setHasFixedSize(true);
        recyclerViewAllRoom.setLayoutManager(new LinearLayoutManager(getActivity()));
        for(int i = 1; i <= 20; i++)
        {
            String isSelected = "N";
            if(i == 1)
            {
                isSelected = "Y";
            }
            RoomDataModel roomDataModel = new RoomDataModel(i,"ROOM "+i,isSelected);
            roomDataModelList.add(roomDataModel);
        }
        adminAll_roomRecyclerviewAdapter = new AdminAll_RoomRecyclerviewAdapter(getContext(), roomDataModelList, new AdminAll_RoomRecyclerviewAdapter.LookForStudents() {
            @Override
            public void searchStudentRoomWise()
            {

            }
        });

        recyclerViewAllRoom.setAdapter(adminAll_roomRecyclerviewAdapter);

        roomNumberTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.select_hostel_room_total);
                final EditText roomStartET = dialog.findViewById(R.id.roomStartET);
                final EditText roomEndET = dialog.findViewById(R.id.roomEndET);
                Button chooseRoomBtn = dialog.findViewById(R.id.chooseRoomBtn);
                chooseRoomBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        return view;
    }
}

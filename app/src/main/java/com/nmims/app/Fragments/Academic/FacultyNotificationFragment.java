package com.nmims.app.Fragments.Academic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.FacultyNotificationRecyclerviewAdapter;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.SwipeToDeleteCallback;
import com.nmims.app.Models.FacultyNotificationModel;
import com.nmims.app.R;
import java.util.ArrayList;
import java.util.List;

public class FacultyNotificationFragment extends Fragment
{
    private TextView no_notificationTV;
    private RecyclerView facultyNotificationRecyclerview;
    private DBHelper dbHelper;
    private List<FacultyNotificationModel> facultyNotificationModelList;
    private FacultyNotificationRecyclerviewAdapter facultyNotificationRecyclerviewAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_faculty_notification,container,false);

        no_notificationTV = view.findViewById(R.id.no_notificationTV);
        facultyNotificationRecyclerview = view.findViewById(R.id.facultyNotificationRecyclerview);
        facultyNotificationRecyclerview.setHasFixedSize(true);
        facultyNotificationRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        facultyNotificationModelList = new ArrayList<>();
        dbHelper = new DBHelper(getContext());
        loadAllNotification();
        enableSwipeToDeleteAndUndo();
        dbHelper.updateFacultyNotificationReadStatus();

        ((FacultyDrawer)getActivity()).setActionBarTitle("Notification");
        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        return view;
    }

    private void loadAllNotification()
    {
        try
        {
            if(facultyNotificationModelList.size() > 0)
            {
                facultyNotificationModelList.clear();
            }
            long notificationCount = dbHelper.getFacultyNotificationCount();
            new MyLog(getContext()).debug("notificationCount", String.valueOf(notificationCount));
            if(notificationCount > 0)
            {
                facultyNotificationModelList = dbHelper.getAllNotification();
                new MyLog(getContext()).debug("facultyNotificationModelList", facultyNotificationModelList.toString());
                facultyNotificationRecyclerviewAdapter = new FacultyNotificationRecyclerviewAdapter(getContext(),facultyNotificationModelList);
                facultyNotificationRecyclerview.setAdapter(facultyNotificationRecyclerviewAdapter);
            }
            else
            {
                facultyNotificationRecyclerview.setVisibility(View.GONE);
                no_notificationTV.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("loadAllNotificationEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void enableSwipeToDeleteAndUndo()
    {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i)
            {
                final int position = viewHolder.getAdapterPosition();
                dbHelper.deleteFacultyNotification(String.valueOf(facultyNotificationModelList.get(position).getId()));
                facultyNotificationRecyclerviewAdapter.removeItem(position);
                if (dbHelper.getFacultyNotificationCount() == 0) {
                    facultyNotificationRecyclerview.setVisibility(View.GONE);
                    no_notificationTV.setVisibility(View.VISIBLE);
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(facultyNotificationRecyclerview);
    }
}

package com.nmims.app.TimeTableCalendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.TimetableModel;
import com.nmims.app.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class StudentCalendarAdapter extends RecyclerView.Adapter<StudentCalendarViewHolder>
{
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    Context context;

    public StudentCalendarAdapter(Context context,ArrayList<LocalDate> days, OnItemListener onItemListener)
    {
        this.context=context;
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public StudentCalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15) //month view
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        else // week view
            layoutParams.height = (int) parent.getHeight();

        return new StudentCalendarViewHolder(view, onItemListener, days);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull StudentCalendarViewHolder holder, int position) {
        final  LocalDate date = days.get(position);
        if(date ==null){
            holder.dayOfMonth.setText("");
        }
        else {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            String daysof=String.format("%02d", date.getDayOfMonth())+"/"+String.format("%02d",date.getMonthValue())+"/"+date.getYear();
            DBHelper dbHelper= new DBHelper(context);
            ArrayList<String> dateList = dbHelper.getStudentLecturesDate();
            ArrayList<TimetableModel> holidaysList = dbHelper.getDateList();
            Collections.sort(dateList);
            if (dateList.size() > 0) {
                for (int i = 0; i < dateList.size(); i++) {
                    if (daysof.equals(dateList.get(i))){
                        holder.cellImg.setImageResource(R.drawable.circle_present_count);
                    }
                }
                if (holidaysList.size() > 0) {
                    for (int i = 0; i < holidaysList.size(); i++) {
                        if (daysof.equals(holidaysList.get(i).dateString)) {
                            if (holidaysList.get(i).isHoliday == 1) {
                                holder.cellImg.setImageResource(R.drawable.circle_absent_count);
                            }
                        }
                    }
                }
            }
            if (date.equals(CalendarUtils.selectedDate))
                holder.parentView.setBackgroundColor(Color.rgb(205,158,160));
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
    public interface  OnItemListener
    {
        void onItemClick(int position, LocalDate date);
    }
}

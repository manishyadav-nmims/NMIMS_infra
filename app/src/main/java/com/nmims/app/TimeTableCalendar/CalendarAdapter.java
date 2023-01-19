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

import com.nmims.app.Models.TimetableModel;

import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    Context context;

    public CalendarAdapter(Context context,ArrayList<LocalDate> days,OnItemListener onItemListener)
    {
        this.context=context;
        this.days=days;
        this.onItemListener=onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, viewGroup, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15) //month view
            layoutParams.height = (int) (viewGroup.getHeight() * 0.166666666);
        else // week view
            layoutParams.height = (int) viewGroup.getHeight();

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final  LocalDate date = days.get(position);
        if(date ==null){
            holder.dayOfMonth.setText("");
        }
        else {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            String daysof=String.format("%02d", date.getDayOfMonth())+"/"+String.format("%02d",date.getMonthValue())+"/"+date.getYear();
            DBHelper dbHelper= new DBHelper(context);
           ArrayList<String> dateList = dbHelper.getLecturesDate();
            ArrayList<TimetableModel> holidaysList = dbHelper.getDateList();
            dbHelper.close();
            Collections.sort(dateList);
            if (dateList.size() > 0) {
                for (int i = 0; i < dateList.size(); i++) {
                    if (dateList.get(i).equals(daysof)){
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

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }
}

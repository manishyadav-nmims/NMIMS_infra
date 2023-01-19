package com.nmims.app.TimeTableCalendar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class StudentCalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private final ArrayList<LocalDate> days;
    public final View parentView;
    public final ImageView cellImg;
    public final TextView dayOfMonth;
    private final StudentCalendarAdapter.OnItemListener onItemListener;

    public StudentCalendarViewHolder(@NonNull View itemView, StudentCalendarAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days)
    {
        super(itemView);
        parentView=itemView.findViewById(R.id.parentView);
        dayOfMonth=itemView.findViewById(R.id.cellDayText);
        cellImg=itemView.findViewById(R.id.cellImg);
        this.onItemListener=onItemListener;
        itemView.setOnClickListener(this);
        this.days=days;
    }


    @Override
    public void onClick(View view) {
        onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));

    }
}

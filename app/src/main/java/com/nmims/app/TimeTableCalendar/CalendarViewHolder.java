package com.nmims.app.TimeTableCalendar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ArrayList<LocalDate> days;
    public final View parentView;
    public final TextView dayOfMonth;
    public final ImageView cellImg;
    private final CalendarAdapter.OnItemListener onItemListener;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days )
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
    public void onClick(View v) {
        onItemListener.onItemClick(getAdapterPosition(),days.get(getAdapterPosition()));
    }
}

package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.TimeTableDataModel;
import com.nmims.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class TimeTableListRecyclerviewAdapter extends RecyclerView.Adapter<TimeTableListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<TimeTableDataModel> timeTableDataModelList;
    private OpenTimeTable openTimeTable;
    public static String timeDiff = null;

    public TimeTableListRecyclerviewAdapter(Context context, List<TimeTableDataModel> timeTableDataModelList,  OpenTimeTable openTimeTable)
    {
        this.context = context;
        this.timeTableDataModelList = timeTableDataModelList;
        this.openTimeTable = openTimeTable;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.time_table_list_design, viewGroup, false);

        return new MyViewHolder(itemView, context, timeTableDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        TimeTableDataModel timeTableDataModel = timeTableDataModelList.get(i);
        myViewHolder.timeTableSubject.setText(timeTableDataModel.getSubject());
        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDate", currentDate);
        new MyLog(NMIMSApplication.getAppContext()).debug("getStartDate", timeTableDataModel.getStartDate());
        int diff = getCountOfDays(timeTableDataModel.getStartDate(),currentDate);
        timeDiff = String.valueOf(diff);
        new MyLog(NMIMSApplication.getAppContext()).debug("diff", String.valueOf(diff));
        myViewHolder.timeTableTime.setText(diff +" days ago");
    }

    @Override
    public int getItemCount() {
        return timeTableDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<TimeTableDataModel> timeTableDataModelList;
        private TextView timeTableSubject, timeTableTime;


        public MyViewHolder(@NonNull View itemView,Context context,List<TimeTableDataModel> timeTableDataModelList)
        {
            super(itemView);
            this.context = context;
            this.timeTableDataModelList = timeTableDataModelList;
            timeTableSubject = itemView.findViewById(R.id.timeTableSubject);
            timeTableTime = itemView.findViewById(R.id.timeTableTime);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                openTimeTable.openTimeTableDetails(timeTableDataModelList.get(getAdapterPosition()));
            }
        }
    }

    public interface OpenTimeTable
    {
        void openTimeTableDetails(TimeTableDataModel timeTableDataModel);
    }

    public static int getCountOfDays(String createdDateString, String expireDateString) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date createdConvertedDate = null;
        Date expireCovertedDate = null;
        try {
            createdConvertedDate = dateFormat.parse(createdDateString);
            expireCovertedDate = dateFormat.parse(expireDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Calendar start = new GregorianCalendar();
        start.setTime(createdConvertedDate);

        Calendar end = new GregorianCalendar();
        end.setTime(expireCovertedDate);

        long diff = end.getTimeInMillis() - start.getTimeInMillis();

        float dayCount = (float) diff / (24 * 60 * 60 * 1000);

        return (int) (dayCount);
    }
}

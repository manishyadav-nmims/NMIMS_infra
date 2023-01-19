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
import com.nmims.app.Models.AnnouncementsDataModel;
import com.nmims.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class AnnouncementsListRecyclerviewAdapter extends RecyclerView.Adapter<AnnouncementsListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<AnnouncementsDataModel> announcementsDataModelList;
    private OpenAnnouncements openAnnouncements;
    public static String timeDiff = null;

    public AnnouncementsListRecyclerviewAdapter(Context context, List<AnnouncementsDataModel> announcementsDataModelList,OpenAnnouncements openAnnouncements)
    {
        this.context = context;
        this.announcementsDataModelList = announcementsDataModelList;
        this.openAnnouncements = openAnnouncements;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.announcements_list_design, viewGroup, false);

        return new MyViewHolder(itemView, context, announcementsDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        AnnouncementsDataModel announcementsDataModel = announcementsDataModelList.get(i);
        myViewHolder.announcementSubject.setText(announcementsDataModel.getSubject());
        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDate", currentDate);
        new MyLog(NMIMSApplication.getAppContext()).debug("getStartDate", announcementsDataModel.getStartDate());
        int diff = getCountOfDays(announcementsDataModel.getStartDate(),currentDate);
        timeDiff = String.valueOf(diff);
        new MyLog(NMIMSApplication.getAppContext()).debug("diff", String.valueOf(diff));
        myViewHolder.announcementTime.setText(diff +" days ago");
    }

    @Override
    public int getItemCount() {
        return announcementsDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<AnnouncementsDataModel> announcementsDataModelList;
        private TextView announcementSubject, announcementTime;


        public MyViewHolder(@NonNull View itemView,Context context,List<AnnouncementsDataModel> announcementsDataModelList)
        {
            super(itemView);
            this.context = context;
            this.announcementsDataModelList = announcementsDataModelList;
            announcementSubject = itemView.findViewById(R.id.announcementSubject);
            announcementTime = itemView.findViewById(R.id.announcementTime);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                openAnnouncements.openAnnouncementsList(announcementsDataModelList.get(getAdapterPosition()));
            }
        }
    }

    public interface OpenAnnouncements
    {
        void openAnnouncementsList(AnnouncementsDataModel announcementsDataModel);
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

package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SurveyListRecyclerviewAdapter extends RecyclerView.Adapter<SurveyListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<SurveyName> surveyNameList;
    private OpenSurvey openSurvey;

    public SurveyListRecyclerviewAdapter(Context context,List<SurveyName> surveyNameList, OpenSurvey openSurvey)
    {
        this.context = context;
        this.surveyNameList = surveyNameList;
        this.openSurvey = openSurvey;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.survey_design, viewGroup, false);
        return new MyViewHolder(itemView, context, surveyNameList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        SurveyName surveyName = surveyNameList.get(i);
        myViewHolder.surveyName.setText(surveyName.getSurveyName());
        myViewHolder.startDate.setText(getDateFormat(surveyName.getStartDate()));
        myViewHolder.endDate.setText(getDateFormat(surveyName.getEndDate()));
        if(surveyName.getSubmitted().equalsIgnoreCase("Y") || surveyName.getLocallySubmitted().equals("Y"))
        {
            myViewHolder.surveyStatus.setText("Completed");
            myViewHolder.surveyStatus.setTextColor(context.getResources().getColor(R.color.colorGreen));
            myViewHolder.startSurveyBtn.setText("Survey Completed");
        }
        else
        {
            myViewHolder.surveyStatus.setText("Pending");
            myViewHolder.surveyStatus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            myViewHolder.startSurveyBtn.setText("Start Survey");
        }

    }

    private String getDateFormat(String date)
    {
        try
        {
            String formattedDate = "";
            String inputPattern = "yyyy-MM-dd";
            String outputPattern = "dd-MM-yyyy";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            Date myDate = null;
            myDate = inputFormat.parse(date);
            formattedDate = outputFormat.format(myDate);
            new MyLog(context).debug("formattedDateBefore",date);
            new MyLog(context).debug("formattedDate",formattedDate);
            return formattedDate;
        }
        catch (Exception e)
        {
            new MyLog(context).debug("getDateFormatEx",e.getMessage());
            e.printStackTrace();
            return date;
        }
    }

    @Override
    public int getItemCount() {
        return surveyNameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<SurveyName> surveyNameList;
        private TextView surveyName, startDate, endDate, surveyStatus;
        private Button startSurveyBtn;

        public MyViewHolder(@NonNull View itemView,Context context, List<SurveyName> surveyNameList)
        {
            super(itemView);
            this.context = context;
            this.surveyNameList = surveyNameList;
            surveyName = itemView.findViewById(R.id.surveyName);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
            surveyStatus = itemView.findViewById(R.id.surveyStatus);
            startSurveyBtn = itemView.findViewById(R.id.startSurveyBtn);

            startSurveyBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == startSurveyBtn.getId())
            {
                String id = surveyNameList.get(getAdapterPosition()).getId();
                openSurvey.submitSurvey(id, getAdapterPosition());
            }
        }
    }

    public interface OpenSurvey
    {
        void submitSurvey(String id, int position);
    }
}

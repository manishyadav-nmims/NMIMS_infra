package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Models.NewsEventDataModel;
import com.nmims.app.R;
import java.util.List;

public class NewsListRecyclerviewAdapter extends RecyclerView.Adapter<NewsListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<NewsEventDataModel> newsEventDataModelList;
    private OpenNews openNews;

    public NewsListRecyclerviewAdapter(Context context, List<NewsEventDataModel> newsEventDataModelList, OpenNews openNews)
    {
        this.context = context;
        this.newsEventDataModelList = newsEventDataModelList;
        this.openNews = openNews;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.news_and_events_design, viewGroup, false);

        return new MyViewHolder(itemView, context, newsEventDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        NewsEventDataModel newsEventDataModel = newsEventDataModelList.get(i);
        myViewHolder.newsEventTv.setText(newsEventDataModel.getSubject());
        myViewHolder.newsEventDate.setText(newsEventDataModel.getStartTime());
    }

    @Override
    public int getItemCount() {
        return newsEventDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<NewsEventDataModel> newsEventDataModelList;
        private TextView newsEventTv, newsEventDate;

        public MyViewHolder(@NonNull View itemView,Context context, List<NewsEventDataModel> newsEventDataModelList)
        {
            super(itemView);
            this.context = context;
            this.newsEventDataModelList = newsEventDataModelList;

            newsEventTv = itemView.findViewById(R.id.newsEventTv);
            newsEventDate = itemView.findViewById(R.id.newsEventDate);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                String description = newsEventDataModelList.get(getAdapterPosition()).getDescription();
                openNews.openNewsDescription(description);
            }
        }
    }

    public interface OpenNews
    {
        void openNewsDescription(String description);
    }
}

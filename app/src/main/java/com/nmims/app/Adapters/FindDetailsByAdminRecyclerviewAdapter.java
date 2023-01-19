package com.nmims.app.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Models.FetchUserDataModel;
import com.nmims.app.R;

import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class FindDetailsByAdminRecyclerviewAdapter extends RecyclerView.Adapter<FindDetailsByAdminRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<FetchUserDataModel> fetchUserDataModelList;
    private OpenUserDetails openUserDetails;

    public FindDetailsByAdminRecyclerviewAdapter(Context context, List<FetchUserDataModel> fetchUserDataModelList, OpenUserDetails openUserDetails)
    {
        this.context = context;
        this.fetchUserDataModelList = fetchUserDataModelList;
        this.openUserDetails = openUserDetails;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fda_design, viewGroup, false);

        return new MyViewHolder(itemView, context, fetchUserDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        FetchUserDataModel fetchUserDataModel = fetchUserDataModelList.get(i);
        myViewHolder.fullNameFDAValue.setText(fetchUserDataModel.getFirstname()+" "+fetchUserDataModel.getLastname());
        myViewHolder.sapIdFDAValue.setText(fetchUserDataModel.getUsername());
        if(TextUtils.isEmpty(fetchUserDataModel.getEmail()))
        {
            myViewHolder.emailFDAVAlue.setText("NA");
        }
        else
        {
            myViewHolder.emailFDAVAlue.setText(fetchUserDataModel.getEmail());
        }

        if(TextUtils.isEmpty(fetchUserDataModel.getMobile()))
        {
            myViewHolder.mobileFDAVAlue.setText("NA");
        }
        else
        {
            myViewHolder.mobileFDAVAlue.setText(fetchUserDataModel.getMobile());
        }
    }

    @Override
    public int getItemCount() {
        return fetchUserDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        private Context context;
        private List<FetchUserDataModel> fetchUserDataModelList;
        private TextView fullNameFDAValue, emailFDAVAlue, mobileFDAVAlue, sapIdFDAValue;

        public MyViewHolder(@NonNull View itemView,Context context, List<FetchUserDataModel> fetchUserDataModelList)
        {
            super(itemView);
            this.context = context;
            this.fetchUserDataModelList = fetchUserDataModelList;
            sapIdFDAValue = itemView.findViewById(R.id.sapIdFDAValue);
            fullNameFDAValue = itemView.findViewById(R.id.fullNameFDAValue);
            emailFDAVAlue = itemView.findViewById(R.id.emailFDAVAlue);
            mobileFDAVAlue = itemView.findViewById(R.id.mobileFDAVAlue);

            itemView.setOnClickListener(this);
            fullNameFDAValue.setOnLongClickListener(this);
            emailFDAVAlue.setOnLongClickListener(this);
            mobileFDAVAlue.setOnLongClickListener(this);
            sapIdFDAValue.setOnLongClickListener(this);

        }

        private void copyToClipboard(String label, String text)
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context,label+" copied sucessfully",Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v)
        {
            if(v.getId() == fullNameFDAValue.getId())
            {
                copyToClipboard("fullName",fetchUserDataModelList.get(getAdapterPosition()).getFirstname()+" "+fetchUserDataModelList.get(getAdapterPosition()).getLastname());
            }
            if(v.getId() == emailFDAVAlue.getId())
            {
                copyToClipboard("email",fetchUserDataModelList.get(getAdapterPosition()).getEmail());
            }
            if(v.getId() == mobileFDAVAlue.getId())
            {
                copyToClipboard("mobile",fetchUserDataModelList.get(getAdapterPosition()).getMobile());
            }
            if(v.getId() == sapIdFDAValue.getId())
            {
                copyToClipboard("sapId",fetchUserDataModelList.get(getAdapterPosition()).getUsername());
            }
            return false;
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                openUserDetails.openUserFullDetails(fetchUserDataModelList.get(getAdapterPosition()));
            }
        }
    }

    public interface OpenUserDetails
    {
        void openUserFullDetails(FetchUserDataModel fetchUserDataModel);
    }
}

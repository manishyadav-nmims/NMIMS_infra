package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.LaundryDataModel;
import com.nmims.app.R;

import java.util.List;

public class HostelStudentViewLaundryRecyclerviewAdapter extends RecyclerView.Adapter<HostelStudentViewLaundryRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<LaundryDataModel> laundryDataModelList;

    public HostelStudentViewLaundryRecyclerviewAdapter(Context context, List<LaundryDataModel> laundryDataModelList)
    {
        this.context = context;
        this.laundryDataModelList = laundryDataModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.student_hostel_laundry_design, viewGroup, false);
        return new MyViewHolder(itemView, context, laundryDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        LaundryDataModel laundryDataModel = laundryDataModelList.get(i);
        myViewHolder.sl_no.setText(String.valueOf(laundryDataModel.getId()));
        myViewHolder.item_name.setText(laundryDataModel.getItemName());
        myViewHolder.item_type.setText(laundryDataModel.getItemType());
        myViewHolder.item_quantity.setText(laundryDataModel.getItemQuantity());

        if(laundryDataModel.getStatus().startsWith("Y"))
        {
            myViewHolder.student_shh_laundryRow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.reactangular_box_non_corner_extra_flamishgreen));//
        }
        else if(laundryDataModel.getStatus().startsWith("N"))
        {
            myViewHolder.student_shh_laundryRow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.reactangular_box_non_corner_extra_flamishred));
        }
        else if(laundryDataModel.getStatus().startsWith("P"))
        {
            myViewHolder.student_shh_laundryRow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.reactangular_box_non_corner_extra_flamishyellow));
        }
    }

    @Override
    public int getItemCount() {
        return laundryDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<LaundryDataModel> laundryDataModelList;
        private TextView sl_no, item_name, item_type, item_quantity;
        private TableRow student_shh_laundryRow;
        private DBHelper dbHelper;

        public MyViewHolder(@NonNull View itemView,Context context, List<LaundryDataModel> laundryDataModelList)
        {
            super(itemView);
            this.context = context;
            this.laundryDataModelList = laundryDataModelList;
            dbHelper = new DBHelper(context);

            sl_no = itemView.findViewById(R.id.sl_no);
            item_name = itemView.findViewById(R.id.item_name);
            item_type = itemView.findViewById(R.id.item_type);
            item_quantity = itemView.findViewById(R.id.item_quantity);
            student_shh_laundryRow = itemView.findViewById(R.id.student_shh_laundryRow);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {

            }
        }
    }

    public interface OpenLaundryData
    {
        void openSubmittedLaundryItems();
    }
}

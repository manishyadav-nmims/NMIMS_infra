package com.nmims.app.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.LaundryDataModel;
import com.nmims.app.R;

import java.util.List;

public class HostelStudentAddLaundryRecyclerviewAdapter extends RecyclerView.Adapter<HostelStudentAddLaundryRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<LaundryDataModel> laundryDataModelList;
    private RefreshLaundryData refreshLaundryData;

    public HostelStudentAddLaundryRecyclerviewAdapter(Context context, List<LaundryDataModel> laundryDataModelList, RefreshLaundryData refreshLaundryData)
    {
        this.context = context;
        this.laundryDataModelList = laundryDataModelList;
        this.refreshLaundryData = refreshLaundryData;
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

        if(laundryDataModel.getSubmitted().startsWith("Y"))
        {
            myViewHolder.student_shh_laundryRow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.reactangular_box_non_corner_extra_flamishgreen));//
        }
    }

    @Override
    public int getItemCount() {
        return laundryDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
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

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) ;
                vibe.vibrate(50);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                String itemName = laundryDataModelList.get(getAdapterPosition()).getItemName();
                if(TextUtils.isEmpty(itemName))
                {
                    itemName = "this item";
                }
                alertDialogBuilder.setTitle("Alert");
                alertDialogBuilder.setMessage("Do you want to delete "+ itemName +" ?");
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setPositiveButton(Html.fromHtml("<font color='#d2232a'>Yes</font>"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        /*boolean isDeleted = dbHelper.deleteLaundryData(String.valueOf(laundryDataModelList.get(getAdapterPosition()).getUniqueKey()));
                        if(isDeleted)
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("item_deleted",String.valueOf(laundryDataModelList.get(getAdapterPosition()).getId()-1));
                            laundryDataModelList.remove(getAdapterPosition());
                            notifyDataSetChanged();
                            refreshLaundryData.refreshLaundryItems();
                        }*/

                        laundryDataModelList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), laundryDataModelList.size());
                        notifyDataSetChanged();
                        refreshLaundryData.refreshLaundryItems();
                    }
                });

                alertDialogBuilder.setNegativeButton(Html.fromHtml("<font color='#00A25F'>No</font>"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.show();
            }

            return false;
        }
    }

    public interface RefreshLaundryData
    {
        void refreshLaundryItems();
    }
}

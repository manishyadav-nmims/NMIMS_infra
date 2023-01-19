package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Adapters.HostelStudentAddLaundryRecyclerviewAdapter;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.RandomKey;
import com.nmims.app.Models.LaundryDataModel;
import com.nmims.app.R;

import java.util.ArrayList;

public class HostelStudentLaundryFragment extends Fragment
{

    private RecyclerView studentLaundryItemRecyclerView_ssh;
    private Button studentLaundryAddNewItemBtn, studentLaundrySubmitBtn;
    private DBHelper dbHelper;
    private ArrayList<LaundryDataModel> laundryDataModelArrayList;
    private TextView sl_no, item_type, addLaundryTV, viewLaundryTV;
    private EditText item_name, item_quantity;
    private HostelStudentAddLaundryRecyclerviewAdapter hostelStudentAddLaundryRecyclerviewAdapter;
    private Spinner spinner_item_type;
    private String[] item_type_arr = new String[3];
    private RelativeLayout layoutAddLaundry, layoutViewLaundry;
    private boolean isAddlaundryVisible = true, isViewlaundryVisible = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_laundry,container,false);

        addLaundryTV = view.findViewById(R.id.addLaundryTV);
        viewLaundryTV = view.findViewById(R.id.viewLaundryTV);
        layoutAddLaundry = view.findViewById(R.id.layoutAddLaundry);
        layoutViewLaundry = view.findViewById(R.id.layoutViewLaundry);
        spinner_item_type = view.findViewById(R.id.spinner_item_type);
        sl_no = view.findViewById(R.id.sl_no);
        item_name = view.findViewById(R.id.item_name);
        item_type = view.findViewById(R.id.item_type);
        item_quantity = view.findViewById(R.id.item_quantity);
        studentLaundryAddNewItemBtn = view.findViewById(R.id.studentLaundryAddNewItemBtn);
        studentLaundrySubmitBtn = view.findViewById(R.id.studentLaundrySubmitBtn);
        studentLaundryItemRecyclerView_ssh = view.findViewById(R.id.studentLaundryItemRecyclerView_add_ssh);
        studentLaundryItemRecyclerView_ssh.setHasFixedSize(true);
        studentLaundryItemRecyclerView_ssh.setLayoutManager(new LinearLayoutManager(getActivity()));
        dbHelper = new DBHelper(getContext());
        laundryDataModelArrayList = new ArrayList<LaundryDataModel>();

        item_type_arr[0] = "Select";
        item_type_arr[1] = "Private";
        item_type_arr[2] = "Regular";

        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(),R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, item_type_arr);
        spinner_item_type.setAdapter(adapter);

        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Laundry");
        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);

        studentLaundryAddNewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
            }
        });

        addLaundryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isAddlaundryVisible)
                {
                    isAddlaundryVisible = false;
                    layoutAddLaundry.setVisibility(View.GONE);
                    addLaundryTV.setText("ADD LAUNDRY ITEMS          +");
                }
                else
                {
                    isAddlaundryVisible = true;
                    isViewlaundryVisible = false;
                    layoutAddLaundry.setVisibility(View.VISIBLE);
                    addLaundryTV.setText("ADD LAUNDRY ITEMS          -");
                    layoutViewLaundry.setVisibility(View.GONE);
                    viewLaundryTV.setText("VIEW LAUNDRY ITEMS          +");
                }
            }
        });

        viewLaundryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isViewlaundryVisible)
                {
                    isViewlaundryVisible = false;
                    layoutViewLaundry.setVisibility(View.GONE);
                    viewLaundryTV.setText("VIEW LAUNDRY ITEMS          +");
                }
                else
                {
                    isViewlaundryVisible = true;
                    isAddlaundryVisible = false;
                    layoutAddLaundry.setVisibility(View.GONE);
                    addLaundryTV.setText("ADD LAUNDRY ITEMS          +");
                    layoutViewLaundry.setVisibility(View.VISIBLE);
                    viewLaundryTV.setText("VIEW LAUNDRY ITEMS          -");
                }
            }
        });


        studentLaundrySubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitItem();
            }
        });
        loadLaundryItemList();
        //getUnsubmittedItemsList();

        return view;
    }

    private void loadLaundryItemList()
    {
        try
        {
            hostelStudentAddLaundryRecyclerviewAdapter = new HostelStudentAddLaundryRecyclerviewAdapter(getContext(), laundryDataModelArrayList, new HostelStudentAddLaundryRecyclerviewAdapter.RefreshLaundryData() {
                @Override
                public void refreshLaundryItems()
                {
                    loadLaundryItemList();
                }
            });
            studentLaundryItemRecyclerView_ssh.setAdapter(hostelStudentAddLaundryRecyclerviewAdapter);

            sl_no.setText(String.valueOf(laundryDataModelArrayList.size()+ 1));
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("loadLaundryItemListEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNewItem()
    {
        try
        {
            String itemName = item_name.getText().toString().trim();
            String itemQuantity = item_quantity.getText().toString().trim();
            String itemType  = spinner_item_type.getSelectedItem().toString();

            if(!TextUtils.isEmpty(itemName))
            {
                if(!TextUtils.isEmpty(itemQuantity) || !itemQuantity.equals("0"))
                {
                    if(!itemType.equals("Select"))
                    {
                        int id = laundryDataModelArrayList.size() + 1;
                        String uniqueKey = RandomKey.getAlphaNumericString(14);
                        new MyLog(NMIMSApplication.getAppContext()).debug("uniqueKey",uniqueKey);
                        LaundryDataModel laundryDataModel = new LaundryDataModel(id,itemName, itemType, itemQuantity,"Y","P",uniqueKey);
                        laundryDataModelArrayList.add(laundryDataModel);
                        hostelStudentAddLaundryRecyclerviewAdapter.notifyDataSetChanged();

                        sl_no.setText(String.valueOf(laundryDataModelArrayList.size()+ 1));
                        item_quantity.setText("");
                        item_name.setText("");
                        spinner_item_type.setSelection(0);
                        loadLaundryItemList();
                    }
                    else
                    {
                        new MyToast(getContext()).showSmallCustomToast("Please enter item type");
                    }
                }
                else
                {
                    new MyToast(getContext()).showSmallCustomToast("Please enter item quantity");
                }
            }
            else
            {
                new MyToast(getContext()).showSmallCustomToast("Please enter item name");
            }
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("addNewItemEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitItem()
    {
        try
        {

        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("submitItemEx",e.getMessage());
            e.printStackTrace();
        }
    }

    /*private void getUnsubmittedItemsList()
    {
        int count = (int)dbHelper.getLaundryCount();
        if(count > 0)
        {
            List<LaundryDataModel> unSubmittedLaundryList = dbHelper.getUnSubmittedLaundryList("N");
            if(unSubmittedLaundryList.size() > 0)
            {
                for(int n = 0; n < unSubmittedLaundryList.size(); n++)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("un_submit",unSubmittedLaundryList.get(n).getItemName()+"  "+ unSubmittedLaundryList.get(n).getSubmitted());
                }
            }
        }
    }*/
}

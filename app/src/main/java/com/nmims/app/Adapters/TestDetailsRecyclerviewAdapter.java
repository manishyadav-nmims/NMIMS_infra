package com.nmims.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nmims.app.Models.TestDetailsDataModel;
import com.nmims.app.R;
import java.util.List;

public class TestDetailsRecyclerviewAdapter extends RecyclerView.Adapter<TestDetailsRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<TestDetailsDataModel> testDetailsDataModelList;
    private OpenTestDetails openTestDetails;

    public TestDetailsRecyclerviewAdapter(Context context, List<TestDetailsDataModel> testDetailsDataModelList, OpenTestDetails openTestDetails)
    {
        this.context = context;
        this.testDetailsDataModelList = testDetailsDataModelList;
        this.openTestDetails = openTestDetails;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.test_details_design, viewGroup, false);
        return new MyViewHolder(itemView, context, testDetailsDataModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        TestDetailsDataModel testDetailsDataModel = testDetailsDataModelList.get(i);
        myViewHolder.schoolNameTD.setText(testDetailsDataModel.getSchoolName());
        myViewHolder.startDateTD.setText(testDetailsDataModel.getStartDate());
        myViewHolder.endDateTD.setText(testDetailsDataModel.getEndDate());
        myViewHolder.durationTD.setText(testDetailsDataModel.getDuration());
        myViewHolder.testTypeTD.setText(testDetailsDataModel.getTestType());
        myViewHolder.stud_count.setText(testDetailsDataModel.getStud_count());
        myViewHolder.faculty_name.setText(testDetailsDataModel.getFaculty_name());
    }

    @Override
    public int getItemCount() {
        return testDetailsDataModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<TestDetailsDataModel> testDetailsDataModelList;
        private TextView schoolNameTD, startDateTD, endDateTD, durationTD, testTypeTD, stud_count, faculty_name;

        public MyViewHolder(@NonNull View itemView,Context context, List<TestDetailsDataModel> testDetailsDataModelList)
        {
            super(itemView);
            this.context = context;
            this.testDetailsDataModelList = testDetailsDataModelList;

            schoolNameTD = itemView.findViewById(R.id.schoolNameTD);
            startDateTD = itemView.findViewById(R.id.startDateTD);
            endDateTD = itemView.findViewById(R.id.endDateTD);
            durationTD = itemView.findViewById(R.id.durationTD);
            testTypeTD = itemView.findViewById(R.id.testTypeTD);
            stud_count = itemView.findViewById(R.id.stud_count);
            faculty_name = itemView.findViewById(R.id.faculty_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                openTestDetails.openTest(testDetailsDataModelList.get(getAdapterPosition()));
            }
        }
    }

    public interface OpenTestDetails
    {
        void openTest(TestDetailsDataModel testDetailsDataModel);
    }
}

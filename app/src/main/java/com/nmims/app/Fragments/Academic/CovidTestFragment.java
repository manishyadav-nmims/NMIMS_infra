package com.nmims.app.Fragments.Academic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nmims.app.R;

public class CovidTestFragment extends Fragment
{
    private Spinner spinnerTemp;
    private TextView selectTempDropDown;
    private String TempRangeArr[] = new String[7];


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_covid_test,container,false);

        spinnerTemp = view.findViewById(R.id.spinnerTemp);
        selectTempDropDown = view.findViewById(R.id.selectTempDropDown);
        TempRangeArr[0]="90-93 °F";
        TempRangeArr[1]="94-97 °F";
        TempRangeArr[2]="98-101 °F";
        TempRangeArr[3]="102-105 °F";
        TempRangeArr[4]="106-109 °F";
        TempRangeArr[5]="112-115 °F";
        TempRangeArr[6]="118-121 °F";
        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(),R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, TempRangeArr);
        spinnerTemp.setAdapter(adapter);

        return view;
    }
}

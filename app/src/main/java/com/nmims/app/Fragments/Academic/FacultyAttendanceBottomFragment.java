package com.nmims.app.Fragments.Academic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;
import java.util.HashMap;
import java.util.Map;

public class FacultyAttendanceBottomFragment extends BottomSheetDialogFragment
{
    private Button saveFABtn;
    private LinearLayout linearLayoutFA;
    private DBHelper dbHelper;
    private LecturesDataModel lecturesDataModel;
    private Map<String, String> checkBoxMap = new HashMap<>();
    private String isCheckBoxClicked = "N", checkResponse = "", username="" ;
    private FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_bottom_attendance_faculty, container, false);
        dbHelper = new DBHelper(getContext());
        saveFABtn = view.findViewById(R.id.saveFABtn);
        linearLayoutFA = view.findViewById(R.id.linearLayoutFA);
        Bundle bun = this.getArguments();
        String LECTURE_ID = bun.getString("LECTURE_ID");
        new MyLog(getContext()).debug("LECTURE_ID_FA",LECTURE_ID);
        lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID);
        new MyLog(getContext()).debug("lecturesDataModelBottomSheet",lecturesDataModel.toString());
        String commanSepfacId = lecturesDataModel.getFacultyId();
        new MyLog(getContext()).debug("commanSepfacId_FA",commanSepfacId);
        String presentFacultyId = lecturesDataModel.getPresentFacultyId();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
            }
        }

        loadViewsAndData(commanSepfacId, presentFacultyId, LECTURE_ID);

        return view;
    }

    private void loadViewsAndData(String commanSepfacId, final String presentFacultyId, final String lectureId)
    {
        try
        {
            String [] facultyArr = commanSepfacId.replace(" ","").split(",");
            String[] presentFacultyArr =  presentFacultyId.replace(" ","").split(",");
            new MyLog(getContext()).debug("facultyArrLEngth",String.valueOf(facultyArr.length));
            isCheckBoxClicked = "N";

            for(String s : facultyArr)
            {
                checkBoxMap.put(s,"N");
            }
            for(String p : presentFacultyArr)
            {
                checkBoxMap.put(p,"Y");
            }

            int viewCount = facultyArr.length;
            CheckBox[] checkBoxBtn = new CheckBox[viewCount];
            for (int i = 0; i < viewCount; i++)
            {
                CheckBox checkBoxBtnView = new CheckBox(getContext());
                checkBoxBtnView.setText(facultyArr[i]);
                if(facultyArr[i].equals(username))
                {
                    checkBoxBtnView.setClickable(false);
                }
                checkBoxBtnView.setTextColor(getResources().getColor(R.color.colorBlack));
                checkBoxBtnView.setTextSize(15);
                checkBoxBtnView.setTypeface(checkBoxBtnView.getTypeface(), Typeface.BOLD);
                checkBoxBtnView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayoutFA.addView(checkBoxBtnView);


                //populate views

                for(Map.Entry<String, String> entry  : checkBoxMap.entrySet())
                {
                    if(checkBoxBtnView.getText().toString().equalsIgnoreCase(entry.getKey()))
                    {
                        if(entry.getValue().equals("Y") || entry.getKey().equals(username))
                        {
                            checkBoxBtnView.setChecked(true);
                        }
                        else
                        {
                            checkBoxBtnView.setChecked(false);
                        }
                    }
                }

                ///

                checkBoxBtn[i] = checkBoxBtnView;
                checkBoxBtnView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                        isCheckBoxClicked = "Y";
                        if(b)
                        {
                            String checkAns = compoundButton.getText().toString();
                            checkBoxMap.put(checkAns,"Y");
                        }
                        else
                        {
                            String checkAns = compoundButton.getText().toString();
                            checkBoxMap.put(checkAns,"N");
                        }
                        checkResponse = "";

                        for(Map.Entry<String, String> entry  : checkBoxMap.entrySet())
                        {
                            if(entry.getValue().equals("Y"))
                            {
                                checkResponse = checkResponse+entry.getKey()+",";
                            }
                        }
                        new MyLog(getContext()).debug("checkResponseT", checkResponse);
                        if(checkResponse.endsWith(","))
                        {
                            checkResponse = checkResponse.substring(0,checkResponse.length()-1);
                        }

                        if(checkResponse.startsWith(","))
                        {
                            checkResponse = checkResponse.substring(1,checkResponse.length());
                        }
                    }
                });
            }

            saveFABtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String finalResponse= "";
                    if(isCheckBoxClicked.equals("N"))
                    {
                        finalResponse = presentFacultyId;
                    }
                    else
                    {
                        finalResponse = checkResponse;
                    }
                    new MyLog(getContext()).debug("finalResponse", finalResponse);

                    dbHelper.updateFacultyAttendance(finalResponse, lectureId);
                    new MyToast(getContext()).showSmallCustomToast("Faculty Attendance Saved");
                }
            });
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("loadViewsAndDataEx",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fm = getFragmentManager();
    }
}

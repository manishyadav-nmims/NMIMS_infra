package com.nmims.app.Fragments.Academic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.Models.Survey.SurveyOption;
import com.nmims.app.Models.Survey.SurveyQuestion;
import com.nmims.app.Models.Survey.SurveyUserResponse;
import com.nmims.app.R;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class StudentSurveyFragmentDetails extends Fragment
{
    private List<SurveyQuestion> surveyQuestionList;
    private DBHelper dbHelper;
    private TextView surveyQuestionTV;
    private String survey_id="", response= "",  checkResponse = "" , isCheckBoxClicked = "N", username="", myApiUrlSurvey="", token="";
    private Button prevQuesBtn, nextQuesBtn;
    private int currentQuestionNumber = 1, currentPosition = 0, previousQuestionNumber=1, questionBarWidth = 0,
            questionBarHeight = 0, totalQuestionLayoutWidth = 0, currentDisplacement= 0;
    private LinearLayout linearQuesAndBox, horizontalQuestionBar;
    private Map<Integer,String> mapOptionType = new HashMap<>();
    private Button submitSurveyBtn;
    private List<SurveyUserResponse> surveyUserResponseList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private SurveyName surveyName;
    private Calendar calendar;
    private HorizontalScrollView myScrollViewHorizontal;
    private Map<String, String> checkBoxMap = new HashMap<>();
    private  Button[] myButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View  view = inflater.inflate(R.layout.fragment_student_survey_details, container, false);

        Bundle bundle = this.getArguments();
        survey_id = bundle.getString("id");
        new MyLog(getContext()).debug("survey_id",survey_id);
        myScrollViewHorizontal = view.findViewById(R.id.myScrollViewHorizontal);
        submitSurveyBtn = view.findViewById(R.id.submitSurveyBtn);
        surveyQuestionTV = view.findViewById(R.id.surveyQuestionTV);
        prevQuesBtn = view.findViewById(R.id.prevQuesBtn);
        nextQuesBtn = view.findViewById(R.id.nextQuesBtn);
        linearQuesAndBox = view.findViewById(R.id.linearQuesAndBox);
        horizontalQuestionBar = view.findViewById(R.id.horizontalQuestionBar);
        dbHelper = new DBHelper(getContext());
        myApiUrlSurvey = dbHelper.getBackEndControl("myApiUrlSurvey").getValue();
        surveyQuestionList = new ArrayList<>();
        progressDialog = new ProgressDialog(getContext());
        surveyName = dbHelper.getSurveyNameById(survey_id);
        calendar = Calendar.getInstance();

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        loadQuestionsBar();
        getPendingSurveyResponse();
        if(surveyQuestionList.size() > 0)
        {
            String questionText =  "<b>" + "Question No. : " + String.valueOf(currentQuestionNumber)+"</b> " + surveyQuestionList.get(currentQuestionNumber).getTitle();
            surveyQuestionTV.setText(Html.fromHtml(questionText));
            changeQuestions(surveyQuestionList,currentQuestionNumber,"button");
        }
        else
        {
            new MyLog(getContext()).debug("SurveyQuestionStatus", "EMPTY");
        }

        prevQuesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(currentQuestionNumber == 1 || currentPosition == 0)
                {
                    new MyToast(getContext()).showSmallCustomToast("No Previous Question");
                }
                else
                {
                    dbHelper.updateSurveyUserResponse(survey_id,surveyQuestionList.get(currentPosition).getId(),response);
                    new MyLog(getContext()).debug("currentPositionBtn", String.valueOf(currentPosition));
                    currentQuestionNumber = currentQuestionNumber-1;
                    currentPosition = currentPosition -1;

                    horizontalQuestionBar.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            int displacement =  (currentPosition*130);
                            myScrollViewHorizontal.smoothScrollTo((displacement),0);
                            new MyLog(getContext()).debug("currentPositionScrolled", String.valueOf(-(displacement)));
                        }
                    },100);


                    changeQuestions(surveyQuestionList,currentQuestionNumber,"button");
                }
            }
        });

        nextQuesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                 if(currentQuestionNumber == surveyQuestionList.size())
                 {
                     new MyToast(getContext()).showSmallCustomToast("No Next Question");
                 }
                 else
                 {
                     dbHelper.updateSurveyUserResponse(survey_id,surveyQuestionList.get(currentPosition).getId(),response);
                     new MyLog(getContext()).debug("currentPositionBtn", String.valueOf(currentPosition));
                     currentQuestionNumber = currentQuestionNumber+1;
                     currentPosition = currentPosition +1;

                     horizontalQuestionBar.postDelayed(new Runnable() {
                         @Override
                         public void run()
                         {
                             int displacement = (currentQuestionNumber-1)*questionBarWidth;
                             myScrollViewHorizontal.smoothScrollTo(+(displacement),0);
                             new MyLog(getContext()).debug("currentPositionScrolled", String.valueOf(+(displacement)));
                         }
                     },100);

                     changeQuestions(surveyQuestionList,currentQuestionNumber,"button");

                 }
            }
        });

        submitSurveyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dbHelper.updateSurveyUserResponse(survey_id,surveyQuestionList.get(currentPosition).getId(),response);
                boolean submitFlag = true;
                final List<SurveyUserResponse> userResponseList = new ArrayList<>();
                for(SurveyQuestion surveyQuestion : surveyQuestionList)
                {
                    SurveyUserResponse surveyUserResponse = dbHelper.getSurveyUserResponse(survey_id,surveyQuestion.getId());
                    if(surveyUserResponse.getResponse().equals(""))
                    {
                        submitFlag = false;
                        final int indexPosition = Integer.parseInt(surveyQuestion.getId())-1;
                        /*horizontalQuestionBar.postDelayed(new Runnable() {
                                @Override
                                public void run()
                                {
                                    int displacement = questionBarWidth*indexPosition;
                                    myScrollViewHorizontal.smoothScrollTo(totalQuestionLayoutWidth/displacement,0);
                                    new MyLog(getContext()).debug("currentPositionScrolled", String.valueOf(indexPosition));
                                }
                        },100);*/
//                        currentQuestionNumber = Integer.parseInt(surveyQuestion.getId())+1;
//                        changeQuestions(surveyQuestionList,Integer.parseInt(surveyQuestion.getId()),"button");
                        new MyToast(getContext()).showSmallCustomToast("Please answer question no. "+surveyQuestion.getId());
                        break;
                    }
                    else
                    {
                        userResponseList.add(surveyUserResponse);
                    }
                }
                if(submitFlag)
                {
                    ///////////////////////HIDING KEYBOARD IF IT IS OPENED//////////////
                    try
                    {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        new MyLog(NMIMSApplication.getAppContext()).debug("HIDING KEYBOARD",e.getMessage());
                    }

                    ///////////////////////////////////////////////////////////////////////
                    dbHelper.updateSurveyNameLocallySubmitted(survey_id);
                    new MyToast(getContext()).showSmallCustomToast("Survey Submitted Successfully");

                    ((StudentDrawer)getActivity()).onBackPressed();
                    ((StudentDrawer)getActivity()).onBackPressed();

                    /*AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    builder1.setTitle("Confirm Final Submission !");
                    builder1.setMessage("Once submitted, you can edit any answers... ");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    submitSurveyBtn.setEnabled(false);
                                    submitSurveyBtn.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            submitSurveyBtn.setEnabled(true);
                                        }
                                    },2000);
                                    for(SurveyUserResponse surveyUserResponse : userResponseList)
                                    {
                                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        surveyUserResponse.setUser_id(surveyName.getUserId());
                                        surveyUserResponse.setSubmitted_date_time(currentDateTime);
                                        //surveyUserResponse.setUsername("40521180035");
                                        surveyUserResponse.setUsername(username);
                                    }

                                    ObjectMapper Obj = new ObjectMapper();
                                    try
                                    {
                                        String jsonStr="[";
                                        for(SurveyUserResponse surveyUserResponse : userResponseList)
                                        {
                                            jsonStr = jsonStr + Obj.writeValueAsString(surveyUserResponse)+",";
                                        }
                                        if (jsonStr.endsWith(","))
                                        {
                                            jsonStr = jsonStr.substring(0,jsonStr.length()-1);
                                        }
                                        submitSurvey(jsonStr.replaceAll("\"", "\"")+"]");
                                        new MyLog(getContext()).debug("jsonStr", jsonStr);
                                    }
                                    catch (JsonProcessingException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    dbHelper.updateSurveyNameLocallySubmitted(survey_id);
                                    new MyToast(getContext()).showSmallCustomToast("Survey Submitted Successfully");
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();*/
                }
            }
        });

        return  view;
    }

    private void changeQuestions(List<SurveyQuestion> surveyQuestionList, int currentQuestionNumber, String from)
    {
        try
        {
            for(int i = 0 ; i < myButton.length; i++)
            {
                if(i == currentPosition)
                {
                    myButton[currentPosition].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                else
                {
                    myButton[i].setTextColor(getResources().getColor(R.color.darkBluishGrey));
                }
            }

            currentDisplacement = currentQuestionNumber*questionBarWidth;
            new MyLog(getContext()).debug("currentDisplacement", String.valueOf(currentDisplacement));
            isCheckBoxClicked = "N";
            if(from.equalsIgnoreCase("Bar"))
            {
                new MyLog(getContext()).debug("responseIfComma", String.valueOf(currentPosition));
                dbHelper.updateSurveyUserResponse(survey_id,String.valueOf(previousQuestionNumber),response);
            }
            response="";
            new MyLog(getContext()).debug("currentPosition", String.valueOf(currentPosition));
            if(surveyQuestionList.size() > 0)
            {
                String questionText =  "<b> <font color='#E84F4F'>" + "Question No.  " +String.valueOf(currentQuestionNumber)+ ". "+"</font> </b> " + surveyQuestionList.get(currentPosition).getTitle();
                surveyQuestionTV.setText(Html.fromHtml(questionText));
                String type = surveyQuestionList.get(currentPosition).getType_id();
                new MyLog(getContext()).debug("typeMapSize", String.valueOf(mapOptionType.size()));
                String optionViewType = mapOptionType.get(Integer.parseInt(type));
                new MyLog(getContext()).debug("type", type);
                new MyLog(getContext()).debug("optionViewType", optionViewType);
                new MyLog(getContext()).debug("currentQuestionId", surveyQuestionList.get(currentPosition).getId());
                List<SurveyOption> surveyOptionList = dbHelper.getSurveyQuestionOptions(surveyQuestionList.get(currentPosition).getId());
                SurveyUserResponse surveyUserResponse  = dbHelper.getSurveyUserResponse(survey_id,surveyQuestionList.get(currentPosition).getId());
                new MyLog(getContext()).debug("surveyOptionList", surveyOptionList.toString());

                new MyLog(getContext()).debug("surveyUserResponse",surveyUserResponse.toString());

                if(optionViewType.equals("CheckBox"))
                {
                    for(SurveyOption surveyOption : surveyOptionList)
                    {
                        checkBoxMap.put(surveyOption.getOptions(),"N");
                    }

                    if(!surveyUserResponse.getResponse().equals(""))
                    {
                        String userResponse = surveyUserResponse.getResponse();
                        new MyLog(getContext()).debug("userResponseC", userResponse.toString());
                        if(userResponse.contains(","))
                        {
                            new MyLog(getContext()).debug("yahn1", "yahn1");
                            String [] userResArr = userResponse.split(",");
                            new MyLog(getContext()).debug("userResArr", userResArr.toString());
                            for (String value : userResArr)
                            {
                                checkBoxMap.put(value, "Y");
                            }
                        }
                        else
                        {
                            new MyLog(getContext()).debug("yahn2", "yahn2");
                            checkBoxMap.put(userResponse,"Y");
                        }
                        new MyLog(getContext()).debug("surveyUserResponse",surveyQuestionList.get(currentPosition).getId()+"  "+surveyUserResponse.getResponse());
                    }
                }
                new MyLog(getContext()).debug("checkBoxMap1--->", checkBoxMap.toString());

                final int viewCount = surveyOptionList.size();

                if(optionViewType.equalsIgnoreCase("Radio Button"))
                {
                    ((ViewGroup) linearQuesAndBox).removeAllViews();
                    RadioGroup radioGroup = new RadioGroup(getContext());
                    RadioButton[] myRadioBtn = new RadioButton[viewCount];
                    for (int i = 0; i < viewCount; i++)
                    {
                        RadioButton radioButton = new RadioButton(getContext());
                        radioButton.setText(surveyOptionList.get(i).getOptions());
                        radioButton.setTextColor(getResources().getColor(R.color.colorBlack));
                        radioButton.setTextSize(15);
                        radioButton.setTypeface(radioButton.getTypeface(), Typeface.BOLD);
                        radioButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        radioGroup.addView(radioButton);
                        if(null != surveyUserResponse.getResponse())
                        {
                            if(radioButton.getText().toString().equalsIgnoreCase(surveyUserResponse.getResponse()))
                            {
                                radioButton.setChecked(true);
                                response = radioButton.getText().toString().trim();
                            }
                        }
                        myRadioBtn[i] = radioButton;
                    }
                    linearQuesAndBox.addView(radioGroup);
                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId)
                        {
                            for(int i=0; i < group.getChildCount(); i++)
                            {
                                RadioButton btn = (RadioButton) group.getChildAt(i);
                                if(btn.getId() == checkedId)
                                {
                                    response = btn.getText().toString().trim();
                                    new MyLog(getContext()).debug("UserResponse",response);
                                }
                            }
                        }
                    });
                }


                if(optionViewType.equalsIgnoreCase("CheckBox"))
                {
                    ((ViewGroup) linearQuesAndBox).removeAllViews();
                    CheckBox[] checkBoxBtn = new CheckBox[viewCount];
                    for (int i = 0; i < viewCount; i++)
                    {
                        CheckBox checkBoxBtnView = new CheckBox(getContext());
                        checkBoxBtnView.setText(surveyOptionList.get(i).getOptions());
                        checkBoxBtnView.setTextColor(getResources().getColor(R.color.colorBlack));
                        checkBoxBtnView.setTextSize(15);
                        checkBoxBtnView.setTypeface(checkBoxBtnView.getTypeface(), Typeface.BOLD);
                        checkBoxBtnView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearQuesAndBox.addView(checkBoxBtnView);

                        new MyLog(getContext()).debug("checkBoxMap2--->", checkBoxMap.toString());
                        for(Map.Entry<String, String> entry  : checkBoxMap.entrySet())
                        {
                            if(checkBoxBtnView.getText().toString().equalsIgnoreCase(entry.getKey()))
                            {
                                if(entry.getValue().equals("Y"))
                                {
                                    checkBoxBtnView.setChecked(true);
                                }
                                else
                                {
                                    checkBoxBtnView.setChecked(false);
                                }
                            }
                        }

                        checkBoxBtn[i] = checkBoxBtnView;

                        checkBoxBtnView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                            {
                                isCheckBoxClicked = "Y";
                                if(isChecked)
                                {
                                   // response = buttonView.getText().toString()+" , ";
                                    String checkAns = buttonView.getText().toString();
                                    new MyLog(getContext()).debug("UserResponse",response);
                                    checkBoxMap.put(checkAns,"Y");
                                }
                                else
                                {
                                    String checkAns = buttonView.getText().toString();
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

                               response = checkResponse;
                               new MyLog(getContext()).debug("responseCheckBox", response);
                            }
                        });
                    }
                }

                if(isCheckBoxClicked.equals("N"))
                {
                    response = surveyUserResponse.getResponse();
                }

                if(optionViewType.equalsIgnoreCase("Textbox"))
                {
                    ((ViewGroup) linearQuesAndBox).removeAllViews();
                    EditText rowEditText = new EditText(getContext());
                    rowEditText.setHint("Enter Text");
                    new MyLog(getContext()).debug("viewCount",String.valueOf(viewCount));
                    rowEditText.setTextColor(getResources().getColor(R.color.colorBlack));
                    rowEditText.setTextSize(15);
                    rowEditText.setSingleLine(true);
                    rowEditText.setBackground(getResources().getDrawable(R.drawable.border_bottom));
                    rowEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    if(null != surveyUserResponse.getResponse())
                    {
                        rowEditText.setText(surveyUserResponse.getResponse());
                        response = rowEditText.getText().toString().trim();
                    }
                    rowEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            response = s.toString().replaceAll("'","").trim();
                            new MyLog(getContext()).debug("UserResponse",response);
                        }
                    });
                    linearQuesAndBox.addView(rowEditText);
                }

                if(optionViewType.equalsIgnoreCase("Commentbox"))
                {
                    ((ViewGroup) linearQuesAndBox).removeAllViews();
                    EditText rowEditText = new EditText(getContext());
                    rowEditText.setHint("Enter Text");
                    rowEditText.setTextColor(getResources().getColor(R.color.colorBlack));
                    rowEditText.setTextSize(15);
                    rowEditText.setPadding(10,10,10,10);
                    rowEditText.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
                    rowEditText.setBackground(getResources().getDrawable(R.drawable.reactangular_box_non_corner_silver));
                    rowEditText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    rowEditText.setSingleLine(false);
                    rowEditText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    rowEditText.setTypeface(rowEditText.getTypeface(), Typeface.BOLD);
                    if(null != surveyUserResponse.getResponse())
                    {
                        rowEditText.setText(surveyUserResponse.getResponse());
                        response = rowEditText.getText().toString().trim();
                    }
                    rowEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));
                    rowEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            response = s.toString().replaceAll("'","").trim();
                            new MyLog(getContext()).debug("UserResponse",response);
                        }
                    });

                    linearQuesAndBox.addView(rowEditText);
                }

            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("changeQuestionsEX", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadQuestionsBar()
    {
        try
        {
            surveyQuestionList.clear();
            surveyQuestionList = dbHelper.getSurveyQuestion(survey_id);
            mapOptionType.put(1,"Radio Button");
            mapOptionType.put(2,"CheckBox");
            mapOptionType.put(3,"Textbox");
            mapOptionType.put(4,"Commentbox");

            //preparing horizontal question bar that can be scrolled horizontally

            myButton = new Button[surveyQuestionList.size()];

            new MyLog(getContext()).debug("surveyQuestionList_size",String.valueOf(surveyQuestionList.size()));

            for (int i = 0; i < surveyQuestionList.size(); i++)
            {
                final Button buttonView = new Button(getContext());
                buttonView.setText(String.valueOf(i+1));
                buttonView.setTextColor(getResources().getColor(R.color.darkBluishGrey));
                buttonView.setTextSize(16);
                buttonView.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_non_corner_light_black));

                buttonView.setTypeface(buttonView.getTypeface(), Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(130,100);
                params.weight = 1.0f;
                params.gravity = Gravity.CENTER_VERTICAL;
                buttonView.setLayoutParams(params);
                buttonView.setPadding(20,0,20,0);
                horizontalQuestionBar.addView(buttonView);
                final int finalI = i;
                buttonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if(currentPosition != finalI)
                        {
                            previousQuestionNumber = currentQuestionNumber;
                            currentQuestionNumber = finalI+1;
                            currentPosition = finalI;


                            buttonView.setTextColor(getResources().getColor(R.color.colorPrimary));

                            horizontalQuestionBar.postDelayed(new Runnable() {
                                @Override
                                public void run()
                                {
                                    int displacement =  (currentPosition*130);
                                    myScrollViewHorizontal.smoothScrollTo((displacement),0);
                                    new MyLog(getContext()).debug("currentPositionScrolled", String.valueOf(-(displacement)));
                                }
                            },100);
                             buttonView.setTextColor(getResources().getColor(R.color.darkBluishGrey));
                            changeQuestions(surveyQuestionList, currentQuestionNumber,"Bar");
                            new MyLog(getContext()).debug("QuestionNumber",String.valueOf(finalI+1));
                        }
                        else
                        {
                            new MyLog(getContext()).debug("QuestionNumber","Same Question Found ie "+String.valueOf(finalI+1));
                        }
                    }
                });
                myButton[i] = buttonView;

                if(dbHelper.getSurveyNameResponseCount(survey_id,"Y") > 0)
                {
                    new MyLog(getContext()).debug("insertUserResponse","Response Already Exists");
                }
                else
                {
                    insertUserResponse(finalI);
                    if(i == surveyQuestionList.size() -1)
                    {
                        dbHelper.updateSurveyNameResponseFlag(survey_id,"Y");
                    }
                }

                if(i == surveyQuestionList.size() -1)
                {
                    buttonView.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            questionBarWidth = buttonView.getWidth();
                            questionBarHeight = buttonView.getHeight();
                            totalQuestionLayoutWidth= questionBarWidth * surveyQuestionList.size();
                            new MyLog(getContext()).debug("questionBarWidth",String.valueOf(questionBarWidth));
                            new MyLog(getContext()).debug("questionBarHeight",String.valueOf(questionBarHeight));
                            new MyLog(getContext()).debug("totalQuestionLayoutWidth",String.valueOf(totalQuestionLayoutWidth));
                        }
                    },100);
                }
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("loadQuestionsEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertUserResponse(int currentPosition)
    {
        try
        {
            dbHelper.insertSurveyUserResponse(new SurveyUserResponse(survey_id,surveyQuestionList.get(currentPosition).getId(),response));
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("insertUserResponse",e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitSurvey(String jsonStr)
    {
        try
        {
            progressDialog.setTitle("Submitting Survey Report !");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new MyLog(NMIMSApplication.getAppContext()).debug("submitSurvey", "submitSurvey");

            String URL = myApiUrlSurvey + "insertSurveyByUserForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("surveyUserResponseListSize",String.valueOf(surveyUserResponseList.size()));
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responseFromApp",jsonStr);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if(response.contains("unauthorised access"))
                    {
                        progressDialog.dismiss();
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try
                    {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("Status");
                        if(status.equals("Success"))
                        {
                            progressDialog.dismiss();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Success");
                            alertDialogBuilder.setMessage("You have successfully completed your survey...");
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dbHelper.deleteSurveyQuestion(survey_id);
                                    for(SurveyQuestion surveyQuestion : surveyQuestionList)
                                    {
                                        dbHelper.deleteSurveyQuestion(surveyQuestion.getId());
                                        dbHelper.deleteSurveyUserResponse(survey_id,surveyQuestion.getId());
                                        dbHelper.deleteSurveyOptions(surveyQuestion.getId());
                                    }
                                    dbHelper.updateSurveyNameSubmitStatus(survey_id,"Y");
                                    ((StudentDrawer) Objects.requireNonNull(getActivity())).onBackPressed();
                                    ((StudentDrawer)getActivity()).onBackPressed();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                        else
                        {
                            progressDialog.dismiss();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Failed");
                            alertDialogBuilder.setMessage("Error occurred while submitting survey...");
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.dismiss();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                    }
                    catch(Exception je)
                    {
                        progressDialog.dismiss();
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Something went wrong...");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error.getCause() instanceof ConnectException || error instanceof NoConnectionError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error.getCause() instanceof SocketException)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof ServerError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No response from server!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof NetworkError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof ParseError)
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
					headers.put("username", username);
                    return headers;
                }


            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("CourseListException",e.getMessage());
        }
    }

    private void getPendingSurveyResponse()
    {
        try
        {
            List<SurveyName> surveyNameList = dbHelper.getPendingSurveyName();


            for(SurveyName surveyName : surveyNameList)
            {
                List<SurveyUserResponse> surveyUserResponseList = new ArrayList<>();
                List<SurveyQuestion> surveyQuestionList = dbHelper.getSurveyQuestion(surveyName.getId());

                for(SurveyQuestion surveyQuestion : surveyQuestionList)
                {
                    SurveyUserResponse surveyUserResponse = dbHelper.getSurveyUserResponse(surveyName.getId(), surveyQuestion.getId());
                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                    surveyUserResponse.setUser_id(surveyName.getUserId());
                    surveyUserResponse.setSubmitted_date_time(currentDateTime);
                    surveyUserResponseList.add(surveyUserResponse);
                }

                ObjectMapper Obj = new ObjectMapper();
                try
                {
                    String jsonStr="[";
                    for(SurveyUserResponse surveyUserResponse : surveyUserResponseList)
                    {
                        jsonStr = jsonStr + Obj.writeValueAsString(surveyUserResponse)+",";
                    }
                    if (jsonStr.endsWith(","))
                    {
                        jsonStr = jsonStr.substring(0,jsonStr.length()-1);
                    }
                    //submitSurvey(jsonStr.replaceAll("\"", "\"")+"]",surveyName.getId(),surveyQuestionList);
                    new MyLog(getContext()).debug("jsonStr1", jsonStr);
                }
                catch (JsonProcessingException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("getPendingSurveyResponseEx",e.getMessage());
            e.printStackTrace();
        }
    }


}

package com.nmims.app.Fragments.Academic;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.SurveyListRecyclerviewAdapter;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.Models.Survey.SurveyOption;
import com.nmims.app.Models.Survey.SurveyQuestion;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentSurveyFragment extends Fragment
{

    private RecyclerView surveyListRecyclerview;
    private TextView surveyListvaEmptyResults;
    private ImageView surveyErrorImage;
    private ProgressBar surveyListvaProgressBar;
    private DBHelper dbHelper;
    private String userName="",player_id="", maxCreatedDate="", myApiUrlSurvey="", token="";
    private RequestQueue requestQueue;
    private List<SurveyName> surveyNameList, oldSurveyNameList;
    private SurveyListRecyclerviewAdapter surveyListRecyclerviewAdapter;
    private Calendar calendar;
    private boolean hitServer = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_survey, container, false);
        surveyListvaProgressBar = view.findViewById(R.id.surveyListvaProgressBar);
        surveyErrorImage = view.findViewById(R.id.surveyErrorImage);
        surveyListvaEmptyResults = view.findViewById(R.id.surveyListvaEmptyResults);
        surveyListRecyclerview = view.findViewById(R.id.surveyListRecyclerview);
        surveyListRecyclerview.setHasFixedSize(true);
        surveyListRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        surveyNameList = new ArrayList<>();
        oldSurveyNameList = new ArrayList<>();
        dbHelper = new DBHelper(getContext());
        myApiUrlSurvey = dbHelper.getBackEndControl("myApiUrlSurvey").getValue();

        calendar = Calendar.getInstance();
        player_id = dbHelper.getNotificationData(1).getPlayerId();
        if(null != player_id)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("player_id_survey", player_id);
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("player_id_survey", "Empty");
        }

        ((StudentDrawer)getActivity()).showAnnouncements(false);
        ((StudentDrawer)getActivity()).setActionBarTitle("Survey");


        Cursor cursor = dbHelper.getLatestCurrentSurveyName();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                maxCreatedDate = cursor.getString(cursor.getColumnIndex("maxCreatedDate"));
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        Cursor cursor1 = dbHelper.getUserDataValues();
        if (cursor1!= null)
        {
            if(cursor1.moveToFirst())
            {
                userName = cursor1.getString(cursor1.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);
            }
        }
        long currentSurveyCount = dbHelper.getSurveyNameCount();
        new MyLog(NMIMSApplication.getAppContext()).debug("currentSurveyCount", String.valueOf(currentSurveyCount));
        if(currentSurveyCount < 1)
        {
            getSurveyList();
        }
        else
        {
            surveyNameList = dbHelper.getSurveyName();
            oldSurveyNameList = dbHelper.getSurveyName();

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
            String surveyCreatedDate = maxCreatedDate;
            new MyLog(getContext()).debug("surveyCreatedDateBefore",surveyCreatedDate);
            surveyCreatedDate  = getDateFormat(surveyCreatedDate);
            new MyLog(getContext()).debug("surveyCreatedDateAfter",surveyCreatedDate);
            long timeDiff= timeDifferencePerfect(surveyCreatedDate,currentDate);
            new MyLog(getContext()).debug("timeDiff",String.valueOf(timeDiff));

            if(timeDiff > 0)
            {
                hitServer = true;
            }

            new MyLog(getContext()).debug("hitServer",String.valueOf(hitServer));

            if(!hitServer)
            {
                if(surveyNameList.size() > 0)
                {
                    new MyLog(getContext()).debug("surveyNameList",surveyNameList.toString());
                    surveyListRecyclerviewAdapter = new SurveyListRecyclerviewAdapter(getContext(), surveyNameList, new SurveyListRecyclerviewAdapter.OpenSurvey() {
                        @Override
                        public void submitSurvey(String id, int position)
                        {
                            if(surveyNameList.get(position).getLocallySubmitted().equals("N") && surveyNameList.get(position).getSubmitted().equals("N"))
                            {
                                Bundle bundle = new Bundle();
                                bundle.putString("id", id);
                                new MyLog(NMIMSApplication.getAppContext()).debug("id",id);
                                StudentSurveyFragmentDetails studentSurveyFragmentDetails = new StudentSurveyFragmentDetails();
                                studentSurveyFragmentDetails.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.add(R.id.StudentHome,studentSurveyFragmentDetails);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack("Survey Details");
                                ft.commit();
                            }
                            else
                            {
                                new MyToast(getContext()).showSmallCustomToast("You have already completed this survey");
                            }
                        }
                    });
                    surveyListRecyclerview.setAdapter(surveyListRecyclerviewAdapter);
                    surveyListvaProgressBar.setVisibility(View.GONE);
                }
                else
                {
                    surveyListvaProgressBar.setVisibility(View.INVISIBLE);
                    surveyErrorImage.setVisibility(View.VISIBLE);
                    surveyListvaEmptyResults.setText("No Survey Found");
                    surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                    surveyListRecyclerview.setVisibility(View.INVISIBLE);
                }
            }
            else
            {
                getSurveyList();
            }
        }

        return view;
    }

    private void getSurveyList()
    {
        try
        {
            surveyListvaProgressBar.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyList", "getSurveyList");

            if(surveyNameList.size() > 0)
            {
                surveyNameList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            String URL = myApiUrlSurvey +"getCurrentSurveyForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            //jsonObject.put("username","40521180035");
            jsonObject.put("username",userName);
            jsonObject.put("player_id",player_id);
            final String mRequestBody = jsonObject.toString();
            new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBody",mRequestBody);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if(response.contains("unauthorised access"))
                    {
                        surveyListvaProgressBar.setVisibility(View.GONE);
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try
                    {
                        JSONArray jsonArray = new JSONArray(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            surveyListvaProgressBar.setVisibility(View.INVISIBLE);
                            surveyErrorImage.setVisibility(View.VISIBLE);
                            surveyListvaEmptyResults.setText("No Survey Found");
                            surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                            surveyListRecyclerview.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            for(int i = 0; i <jsonArray.length(); i++)
                            {
                                JSONObject jsonSurveyName = jsonArray.getJSONObject(i);
                                String id= "", surveyName="", userId="", startDate="", endDate="", submitted = "N", questionList="", userResponseCount="";

                                if(jsonSurveyName.has("surveyName"))
                                {
                                    surveyName = jsonSurveyName.getString("surveyName").replaceAll("'","");
                                }
                                if(!surveyName.equals(""))
                                {
                                    if(jsonSurveyName.has("id"))
                                    {
                                        id = jsonSurveyName.getString("id");
                                    }

                                    if(jsonSurveyName.has("startDateStr"))
                                    {
                                        startDate = jsonSurveyName.getString("startDateStr");
                                    }

                                    if(jsonSurveyName.has("userId"))
                                    {
                                        userId = jsonSurveyName.getString("userId");
                                    }

                                    if(jsonSurveyName.has("endDateStr"))
                                    {
                                        endDate = jsonSurveyName.getString("endDateStr");
                                    }

                                    if(jsonSurveyName.has("userResponseCount"))
                                    {
                                        userResponseCount = jsonSurveyName.getString("userResponseCount");
                                    }

                                    if(Integer.parseInt(userResponseCount) > 0)
                                    {
                                        submitted = "Y";
                                    }
                                    //replaceAll("[\u0080-\uffff]", "")

                                    boolean replaceFlag = true;
                                    String uniqueKeyContraint = surveyName.replace("'", "")+startDate+endDate;
                                    new MyLog(NMIMSApplication.getAppContext()).debug("uniqueKeyContraint", uniqueKeyContraint);
                                    for(SurveyName oldSurveyName : oldSurveyNameList)
                                    {
                                        String oldUniqueKeyContraint = oldSurveyName.getSurveyName()+oldSurveyName.getStartDate()+oldSurveyName.getEndDate();
                                        new MyLog(NMIMSApplication.getAppContext()).debug("oldUniqueKeyContraint", oldUniqueKeyContraint);

                                        if(uniqueKeyContraint.equals(oldUniqueKeyContraint))
                                        {
                                            replaceFlag = false;
                                            new MyLog(NMIMSApplication.getAppContext()).debug("ContraintMatched "+String.valueOf(i), "true");
                                        }
                                        else
                                        {
                                            replaceFlag = true;
                                            new MyLog(NMIMSApplication.getAppContext()).debug("ContraintMatched "+String.valueOf(i), "false");
                                        }
                                    }

                                    if(replaceFlag)
                                    {
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        dbHelper.insertSurveyName(new SurveyName(surveyName.replace("'", ""),userId, startDate, endDate, submitted, "N",createdDate,"N"));

                                        if(jsonSurveyName.has("questionList"))
                                        {
                                            questionList = jsonSurveyName.getString("questionList").replaceAll("'","");;

                                            JSONArray jsonArraySurveyQuestion = new JSONArray(questionList);
                                            if(jsonArraySurveyQuestion.length() > 0)
                                            {
                                                for(int j = 0; j <jsonArraySurveyQuestion.length(); j++)
                                                {
                                                    String questionId="", title="", type_id="", optionList="", is_checked="";
                                                    JSONObject jsonSurveyQuestion = jsonArraySurveyQuestion.getJSONObject(j);

                                                    if(jsonSurveyQuestion.has("id"))
                                                    {
                                                        questionId = jsonSurveyQuestion.getString("id");
                                                    }

                                                    if(jsonSurveyQuestion.has("title"))
                                                    {
                                                        title = jsonSurveyQuestion.getString("title").replaceAll("'","");;
                                                    }

                                                    if(jsonSurveyQuestion.has("type_id"))
                                                    {
                                                        type_id = jsonSurveyQuestion.getString("type_id");
                                                    }

                                                    if(jsonSurveyQuestion.has("is_checked"))
                                                    {
                                                        is_checked = jsonSurveyQuestion.getString("is_checked");
                                                    }

                                                    dbHelper.insertSurveyQuestion(new SurveyQuestion( title.replace("'", ""),type_id, is_checked, id));

                                                    if(jsonSurveyQuestion.has("optionList"))
                                                    {
                                                        optionList = jsonSurveyQuestion.getString("optionList").replaceAll("'","");;
                                                        JSONArray jsonArraySurveyOption = new JSONArray(optionList);

                                                        if(jsonArraySurveyOption.length() > 0)
                                                        {
                                                            for(int k = 0; k <jsonArraySurveyOption.length(); k++)
                                                            {
                                                                String optionId="", options="", question_id="", option_type_id="";
                                                                JSONObject jsonSurveyOption = jsonArraySurveyOption.getJSONObject(k);

                                                                if(jsonSurveyOption.has("id"))
                                                                {
                                                                    optionId = jsonSurveyOption.getString("id");
                                                                }

                                                                if(jsonSurveyOption.has("options"))
                                                                {
                                                                    options = jsonSurveyOption.getString("options").replaceAll("'","");;
                                                                }

                                                                if(jsonSurveyOption.has("question_id"))
                                                                {
                                                                    question_id = jsonSurveyOption.getString("question_id");
                                                                }

                                                                if(jsonSurveyOption.has("type_id"))
                                                                {
                                                                    option_type_id = jsonSurveyOption.getString("type_id").replaceAll("'","");;
                                                                }

                                                                dbHelper.insertSurveyOption(new SurveyOption(options.replace("'", ""), question_id, option_type_id));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        new MyLog(getContext()).debug("SurveyNameStatus","Survey Already Exists In Local");
                                    }
                                }
                                else
                                {
                                    new MyLog(getContext()).debug("SurveyNameStatus","No Current Survey");
                                }
                            }

                            if(dbHelper.getSurveyNameCount() > 0)
                            {
                                new MyLog(getContext()).debug("getSurveyNameCount",String.valueOf(dbHelper.getSurveyNameCount()));

                                surveyNameList = dbHelper.getSurveyName();
                                if(surveyNameList.size() > 0)
                                {
                                    for(SurveyName surveyName : surveyNameList)
                                    {
                                        new MyLog(getContext()).debug(surveyName.getSurveyName(),surveyName.getStartDate()+" - "+surveyName.getEndDate());
                                    }
                                }
                                surveyListRecyclerviewAdapter = new SurveyListRecyclerviewAdapter(getContext(), surveyNameList, new SurveyListRecyclerviewAdapter.OpenSurvey() {
                                    @Override
                                    public void submitSurvey(String id, int position)
                                    {
                                        if(surveyNameList.get(position).getLocallySubmitted().equals("N") && surveyNameList.get(position).getSubmitted().equals("N"))
                                        {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("id", id);
                                            new MyLog(NMIMSApplication.getAppContext()).debug("id",id);
                                            StudentSurveyFragmentDetails studentSurveyFragmentDetails = new StudentSurveyFragmentDetails();
                                            studentSurveyFragmentDetails.setArguments(bundle);
                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.add(R.id.StudentHome,studentSurveyFragmentDetails);
                                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                            ft.addToBackStack("Survey Details");
                                            ft.commit();
                                        }
                                        else
                                        {
                                            new MyToast(getContext()).showSmallCustomToast("You have already done with this survey");
                                        }
                                    }
                                });

                                surveyListRecyclerview.setAdapter(surveyListRecyclerviewAdapter);
                            }
                            else
                            {
                                surveyListvaProgressBar.setVisibility(View.INVISIBLE);
                                surveyErrorImage.setVisibility(View.VISIBLE);
                                surveyListvaEmptyResults.setText("No Survey Found");
                                surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                                surveyListRecyclerview.setVisibility(View.INVISIBLE);
                            }

                            surveyListvaProgressBar.setVisibility(View.GONE);
                        }
                    }
                    catch(Exception je)
                    {
                        surveyListvaProgressBar.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        surveyListvaEmptyResults.setText("No Survey Found");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        surveyListRecyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    surveyListvaProgressBar.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        surveyListvaEmptyResults.setText("Oops! Connection timeout error!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        surveyListvaEmptyResults.setText("Oops! Unable to reach server!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        surveyListvaEmptyResults.setText("Oops! No Internet Connection Available!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        surveyListvaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        surveyListvaEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        surveyListvaEmptyResults.setText("Oops! No response from server!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        surveyListvaEmptyResults.setText("Oops! It seems your internet is slow!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        surveyListvaEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        surveyListvaEmptyResults.setText("Oops! An unknown error occurred!");
                        surveyListvaEmptyResults.setVisibility(View.VISIBLE);
                        surveyListRecyclerview.setVisibility(View.GONE);
                        surveyErrorImage.setVisibility(View.VISIBLE);
                        surveyErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
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
					headers.put("username", userName);
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

    public long timeDifferencePerfect(String time1, String time2)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = null;
        Date d2 = null;
        long diff = 0;
        try
        {
            d1 = format.parse(time1);
            d2 = format.parse(time2);
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataB",d2.toString());


            Date d = format.parse(time2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            String newTime = format.format(cal.getTime());
            d2 = format.parse(newTime);


            new MyLog(NMIMSApplication.getAppContext()).debug("d1_data",d1.toString());
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataA",d2.toString());

            diff = d2.getTime() - d1.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    private String getDateFormat(String date)
    {
        try
        {
            String formattedDate = "";
            String inputPattern = "yyyy-MM-dd HH:mm:ss";
            String outputPattern = "yyyy-MM-dd";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            Date myDate = null;
            myDate = inputFormat.parse(date);
            formattedDate = outputFormat.format(myDate);
            new MyLog(getContext()).debug("formattedDateBefore",date);
            new MyLog(getContext()).debug("formattedDate",formattedDate);
            return formattedDate;
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("getDateFormatEx",e.getMessage());
            e.printStackTrace();
            return date;
        }
    }
}

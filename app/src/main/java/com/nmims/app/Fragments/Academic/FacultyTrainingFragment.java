package com.nmims.app.Fragments.Academic;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FacultyTrainingFragment extends Fragment
{
    private Button submitFT;
    private CheckBox checkboxFT;
    private CardView cardViewFT;
    private TextView noTrainingMsgFT;
    private String sharedPrefschoolName="", userName="",trainingProgramId="",myApiUrlLms="", token="";
    private DBHelper dbHelper;
    private RelativeLayout fragment_FacultyTrainingFragment;
    private ProgressBar progressBarFT;
    private ImageView errorImageFT;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_training_faculty, container, false);

        checkboxFT = view.findViewById(R.id.checkboxFT);
        submitFT = view.findViewById(R.id.submitFT);
        cardViewFT = view.findViewById(R.id.cardViewFT);
        noTrainingMsgFT = view.findViewById(R.id.noTrainingMsgFT);
        progressBarFT = view.findViewById(R.id.ProgressBarFT);
        errorImageFT = view.findViewById(R.id.errorImageFT);
        fragment_FacultyTrainingFragment = view.findViewById(R.id.fragment_FacultyTrainingFragment);

        ((FacultyDrawer)getActivity()).showShuffleBtn(false);
        ((FacultyDrawer)getActivity()).setActionBarTitle("Training Session");

        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                token = cursor.getString(cursor.getColumnIndex("token"));
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
                new MyLog(NMIMSApplication.getAppContext()).debug("userName",userName);
            }
        }

        checkboxFT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    submitFT.setEnabled(true);
                    submitFT.setBackgroundResource(R.drawable.round_corner_button);
                    submitFT.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else
                {
                    submitFT.setEnabled(false);
                    submitFT.setBackgroundResource(R.drawable.round_corner_button_grey);
                    submitFT.setTextColor(getResources().getColor(R.color.colorDarkGrey));
                }
            }
        });

        fragment_FacultyTrainingFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ////////////Dont delete
            }
        });


        submitFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTrainingReport();
            }
        });

        getTrainingInfo();

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("faculty_training_fragment", "faculty_training_fragment");
        mFirebaseAnalytics.logEvent("Faculty_Training_Fragment", params);
        ///////////////////////////////////////////////

        return  view;
    }

    private void submitTrainingReport()
    {
       try
       {
           progressBarFT.setVisibility(View.VISIBLE);
           final AESEncryption aes = new AESEncryption(getContext());
           String URL = myApiUrlLms + sharedPrefschoolName+"/insertTrainingAttendance";
           new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
           RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
           Map<String, Object> mapJ = new HashMap<String, Object>();
           mapJ.put("username",userName);
           mapJ.put("trainingProgramId",trainingProgramId);

           final String mRequestBody = aes.encryptMap(mapJ);

           StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
           {
               @Override
               public void onResponse(String response)
               {
                   String respStr = aes.decrypt(response);
                   if(respStr.contains("unauthorised access"))
                   {
                       progressBarFT.setVisibility(View.GONE);
                       ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                       return;
                   }
                   try
                   {
                       JSONObject jsonResponseObj = new JSONObject(respStr);
                       String status = "";
                       if(jsonResponseObj.has("Status"))
                       {
                           status = jsonResponseObj.getString("Status");
                           new MyLog(NMIMSApplication.getAppContext()).debug("status",status);
                           if(status.equalsIgnoreCase("Success"))
                           {
                               showUpdateDialog("Success","Submitted Sucessfully...");
                           }
                           else
                           {
                               showUpdateDialog("Error Occurred","Please try again...");
                           }
                       }
                       else
                       {
                           showUpdateDialog("Error Occurred","Something went wrong...");
                       }
                   }
                   catch(Exception je)
                   {
                       new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                       showUpdateDialog("Error",je.getMessage());
                   }
               }
           }, new Response.ErrorListener()
           {
               @Override
               public void onErrorResponse(VolleyError error)
               {
                   progressBarFT.setVisibility(View.GONE);
                   new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

                   if (error instanceof TimeoutError)
                   {
                       showUpdateDialog("Error","Oops! Connection timeout error!");
                   }
                   else if (error.getCause() instanceof ConnectException)
                   {
                       showUpdateDialog("Error","Oops! Unable to reach server!");
                   }

                   else if (error instanceof NoConnectionError)
                   {
                       showUpdateDialog("Error","Oops! No Internet Connection Available!");
                   }

                   else if (error.getCause() instanceof SocketException)
                   {
                       showUpdateDialog("Error","Oops! We are Sorry Something went wrong. We're working on it now!");
                   }
                   else if (error instanceof AuthFailureError)
                   {
                       showUpdateDialog("Error","Oops! Server couldn't find the authenticated request!");
                   }
                   else if (error instanceof ServerError)
                   {
                       showUpdateDialog("Error","Oops! No response from server!");
                   }
                   else if (error instanceof NetworkError)
                   {
                       showUpdateDialog("Error","Oops! It seems your internet is slow!");
                   }
                   else if (error instanceof ParseError)
                   {
                       showUpdateDialog("Error","Oops! Parse Error (because of invalid json or xml)!");
                   }
                   else
                   {
                       showUpdateDialog("Error","Oops! An unknown error occurred!");
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
           new MyLog(NMIMSApplication.getAppContext()).debug("submitTrainingReportEX",e.getMessage());
       }
    }

    private void showUpdateDialog(final String Title, String Message)
    {
        try
        {
            progressBarFT.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(Title);
            builder.setMessage(Message);
            builder.setCancelable(false);
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                            if(Title.equalsIgnoreCase("Success"))
                            {
                                getActivity().onBackPressed();
                            }
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getTrainingInfo()
    {
        try
        {
            progressBarFT.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getTrainingInfo", "getTrainingInfo");
            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            final AESEncryption aes = new AESEncryption(getContext());
            String URL = myApiUrlLms + sharedPrefschoolName+"/showTrainingSession";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",userName);
            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username",userName);

            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressBarFT.setVisibility(View.GONE);
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try
                    {
                        JSONObject jsonResponseOb = new JSONObject(respStr);
                        new MyLog(NMIMSApplication.getAppContext()).debug("responseLength", String.valueOf(jsonResponseOb.length()));
                        if(jsonResponseOb.length() < 1)
                        {
                            progressBarFT.setVisibility(View.INVISIBLE);
                            errorImageFT.setVisibility(View.VISIBLE);
                            noTrainingMsgFT.setText("Currently, No training session is going on for you...");
                            noTrainingMsgFT.setVisibility(View.VISIBLE);
                            cardViewFT.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            JSONObject jsonResponseObj = new JSONObject(response);
                            String id = "",status="";
                            if(jsonResponseObj.has("id"))
                            {
                                id = jsonResponseObj.getString("id");
                                trainingProgramId = id;
                                noTrainingMsgFT.setVisibility(View.GONE);
                                errorImageFT.setVisibility(View.GONE);
                                cardViewFT.setVisibility(View.VISIBLE);
                            }
                            else if(jsonResponseObj.has("Status"))
                            {
                                status = jsonResponseObj.getString("Status");
                                noTrainingMsgFT.setVisibility(View.VISIBLE);
                                noTrainingMsgFT.setText(status);
                                errorImageFT.setVisibility(View.VISIBLE);
                                cardViewFT.setVisibility(View.GONE);
                            }
                            else
                            {
                                noTrainingMsgFT.setVisibility(View.VISIBLE);
                                noTrainingMsgFT.setText("Currently, No training session is going on for you...");
                                errorImageFT.setVisibility(View.VISIBLE);
                                cardViewFT.setVisibility(View.GONE);
                            }

                            progressBarFT.setVisibility(View.GONE);
                        }
                    }
                    catch(Exception je)
                    {
                        progressBarFT.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        noTrainingMsgFT.setText("Currently, No training session is going on for you...");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        cardViewFT.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressBarFT.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        noTrainingMsgFT.setText("Oops! Connection timeout error!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        noTrainingMsgFT.setText("Oops! Unable to reach server!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        noTrainingMsgFT.setText("Oops! No Internet Connection Available!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        noTrainingMsgFT.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        noTrainingMsgFT.setText("Oops! Server couldn't find the authenticated request!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        noTrainingMsgFT.setText("Oops! No response from server!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        noTrainingMsgFT.setText("Oops! It seems your internet is slow!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        noTrainingMsgFT.setText("Oops! Parse Error (because of invalid json or xml)!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        noTrainingMsgFT.setText("Oops! An unknown error occurred!");
                        noTrainingMsgFT.setVisibility(View.VISIBLE);
                        cardViewFT.setVisibility(View.GONE);
                        errorImageFT.setVisibility(View.VISIBLE);
                        errorImageFT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
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
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

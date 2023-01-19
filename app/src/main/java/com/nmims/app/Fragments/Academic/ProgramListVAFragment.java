package com.nmims.app.Fragments.Academic;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.ProgramListVARecyclerviewAdapter;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramListVAFragment extends Fragment {
    private RequestQueue requestQueue;
    private RecyclerView programListVARecyclerview;
    private ImageView errorImage;
    private TextView programlistvaEmptyResults;
    private ProgressBar programlistvaProgressBar;
    private ProgramListVARecyclerviewAdapter programListVARecyclerviewAdapter;
    private List<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
    private FrameLayout progrFargVA;
    private String sharedPrefschoolName1 = "", userName = "", myApiUrlLms = "", token = "";
    private DBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.program_list_va_fragment, container, false);

        programListVARecyclerview = view.findViewById(R.id.programListVARecyclerview);
        programlistvaEmptyResults = view.findViewById(R.id.programlistvaEmptyResults);
        programlistvaProgressBar = view.findViewById(R.id.programlistvaProgressBar);
        errorImage = view.findViewById(R.id.errorImage);
        programListVARecyclerview.setHasFixedSize(true);
        progrFargVA = view.findViewById(R.id.progrFargVA);
        progrFargVA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        programListVARecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);
                sharedPrefschoolName1 = cursor.getString(cursor.getColumnIndex("currentSchool"));
                //sharedPrefschoolName1 = Config.sharedPrefschoolName;
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName1);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        getProgramListVA(view);

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("program_list_fragment", "program_list_fragment");
        mFirebaseAnalytics.logEvent("Program_ListFragment", params);
        ///////////////////////////////////////////////


        return view;
    }

    private void getProgramListVA(final View view) {
        try {
            programlistvaProgressBar.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getProgramListVA", "getProgramListVA");

            if (lecturesDataModelList.size() > 0) {
                lecturesDataModelList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);

            String URL = myApiUrlLms + sharedPrefschoolName1 + "/getProgramsByUsernameForApp";

            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", userName);

            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", userName);
            final String mRequestBody = jsonObject.toString();

            ((FacultyDrawer) getActivity()).showShuffleBtn(false);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("unauthorised access")) {
                        programlistvaProgressBar.setVisibility(View.GONE);
                        ((FacultyDrawer) getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        Log.i("jsonArray", String.valueOf(jsonArray.length()));
                        if (jsonArray.length() < 1) {
                            programlistvaEmptyResults.setText("No Program List Found");
                            errorImage.setVisibility(View.VISIBLE);
                            programlistvaEmptyResults.setVisibility(View.VISIBLE);
                            programListVARecyclerview.setVisibility(View.INVISIBLE);
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);
                                String programName = jsonResponseObj.getString("programName");
                                String programId = jsonResponseObj.getString("programId");
                                LecturesDataModel lecturesDataModel = new LecturesDataModel(programName, programId);
                                lecturesDataModelList.add(lecturesDataModel);
                                new MyLog(NMIMSApplication.getAppContext()).debug("programName", programName);
                            }

                            programListVARecyclerviewAdapter = new ProgramListVARecyclerviewAdapter(getContext(), lecturesDataModelList, new ProgramListVARecyclerviewAdapter.OpenCourses() {
                                @Override
                                public void openCourselist(String programId) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("programId", programId);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("programId", programId);
                                    CoursesFragment coursesFragment = new CoursesFragment();
                                    coursesFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.FacultyHome, coursesFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Courses");
                                    ft.commit();
                                }
                            });
                            programListVARecyclerview.setAdapter(programListVARecyclerviewAdapter);
                            programlistvaProgressBar.setVisibility(View.GONE);
                        }
                    } catch (Exception je) {
                        programlistvaProgressBar.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                        programlistvaEmptyResults.setText("No Programs Found");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        programListVARecyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    programlistvaProgressBar.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
//                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                        if (error.getCause() instanceof ConnectException)
//                        {
//                            programlistvaEmptyResults.setText("Oops! No Internet Connection Available");
//                            programlistvaEmptyResults.setVisibility(View.VISIBLE);
//                            errorImage.setVisibility(View.VISIBLE);
//                            errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
//                            programListVARecyclerview.setVisibility(View.GONE);
//                        }
//                        else if (error.getCause() instanceof SocketException)
//                        {
//                            programlistvaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
//                            programlistvaEmptyResults.setVisibility(View.VISIBLE);
//                            errorImage.setVisibility(View.VISIBLE);
//                            errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
//                            programListVARecyclerview.setVisibility(View.GONE);
//                        }
//                    }

                    if (error instanceof TimeoutError) {
                        try {
                            programlistvaEmptyResults.setText("Oops! Connection timeout error!");
                            programlistvaEmptyResults.setVisibility(View.VISIBLE);
                            programListVARecyclerview.setVisibility(View.GONE);
                            errorImage.setVisibility(View.VISIBLE);
                            errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else if (error.getCause() instanceof ConnectException) {
                        programlistvaEmptyResults.setText("Oops! Unable to reach server!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NoConnectionError) {
                        programlistvaEmptyResults.setText("Oops! No Internet Connection Available!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof SocketException) {
                        programlistvaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof AuthFailureError) {
                        programlistvaEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ServerError) {
                        programlistvaEmptyResults.setText("Oops! No response from server!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NetworkError) {
                        programlistvaEmptyResults.setText("Oops! It seems your internet is slow!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ParseError) {
                        programlistvaEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    } else {
                        programlistvaEmptyResults.setText("Oops! An unknown error occurred!");
                        programlistvaEmptyResults.setVisibility(View.VISIBLE);
                        programListVARecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                }
            }) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

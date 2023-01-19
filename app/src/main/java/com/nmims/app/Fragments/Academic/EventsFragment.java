package com.nmims.app.Fragments.Academic;import android.database.Cursor;import android.os.Bundle;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.ProgressBar;import android.widget.RelativeLayout;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.annotation.Nullable;import androidx.fragment.app.Fragment;import androidx.fragment.app.FragmentTransaction;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;import com.android.volley.AuthFailureError;import com.android.volley.DefaultRetryPolicy;import com.android.volley.NetworkError;import com.android.volley.NoConnectionError;import com.android.volley.ParseError;import com.android.volley.Request;import com.android.volley.RequestQueue;import com.android.volley.Response;import com.android.volley.ServerError;import com.android.volley.TimeoutError;import com.android.volley.VolleyError;import com.android.volley.toolbox.StringRequest;import com.android.volley.toolbox.Volley;import com.google.firebase.analytics.FirebaseAnalytics;import com.nmims.app.Activities.StudentDrawer;import com.nmims.app.Adapters.EventsListRecyclerviewAdapter;import com.nmims.app.Helpers.AESEncryption;import com.nmims.app.Helpers.Config;import com.nmims.app.Helpers.DBHelper;import com.nmims.app.Helpers.MyLog;import com.nmims.app.Helpers.NMIMSApplication;import com.nmims.app.Models.NewsEventDataModel;import com.nmims.app.R;import org.json.JSONArray;import org.json.JSONObject;import java.net.ConnectException;import java.net.SocketException;import java.nio.charset.StandardCharsets;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import java.util.Map;public class EventsFragment extends Fragment{    private RecyclerView fragmentEventsRecyclerview;    private TextView eventsErrorMsg;    private ProgressBar eventsListProgressBar;    private RequestQueue requestQueue;    private List<NewsEventDataModel> newsEventDataModelList = new ArrayList<>();    private EventsListRecyclerviewAdapter eventsListRecyclerviewAdapter;    private ImageView errorImage;    private RelativeLayout eventsFrag;    private String userName="", sharedPrefschoolName="",myApiUrlLms="", token="";    private DBHelper dbHelper;    @Override    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)    {        View view = inflater.inflate(R.layout.fragment_events,container,false);        fragmentEventsRecyclerview = view.findViewById(R.id.fragmentEventsRecyclerview);        eventsErrorMsg = view.findViewById(R.id.eventsErrorMsg);        errorImage = view.findViewById(R.id.errorImage);        eventsFrag = view.findViewById(R.id.eventsFrag);        eventsFrag.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                //Dont Delete            }        });        eventsListProgressBar = view.findViewById(R.id.eventsListProgressBar);        fragmentEventsRecyclerview.setHasFixedSize(true);        fragmentEventsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));        dbHelper = new DBHelper(getContext());        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();        //myApiUrlLms = Config.myApiUrlLms;        Cursor cursor = dbHelper.getUserDataValues();        if (cursor!= null)        {            if(cursor.moveToFirst())            {                userName = cursor.getString(cursor.getColumnIndex("sapid"));                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));					   //sharedPrefschoolName = Config.sharedPrefschoolName;                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName);                token = cursor.getString(cursor.getColumnIndex("token"));            }        }        getEventList(view);        //////////ADDING FIREBASE EVENTS///////////////        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());        Bundle params = new Bundle();        params.putString("events_fragment", "events_fragment");        mFirebaseAnalytics.logEvent("Events_Fragment", params);        ///////////////////////////////////////////////        return view;    }    private void getEventList(final View view)    {        try        {            eventsListProgressBar.setVisibility(View.VISIBLE);            new MyLog(NMIMSApplication.getAppContext()).debug("getEventList", "getEventList");            if(newsEventDataModelList.size() > 0)            {                newsEventDataModelList.clear();            }            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);            final AESEncryption aes = new AESEncryption(getContext());            String URL = myApiUrlLms + sharedPrefschoolName+"/getEventsListForApp";            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",userName);            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());            Map<String, Object> mapJ = new HashMap<String, Object>();            mapJ.put("username",userName);            final String mRequestBody = aes.encryptMap(mapJ);            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()            {                @Override                public void onResponse(String response)                {                    String respStr = aes.decrypt(response);                    if(respStr.contains("unauthorised access"))                    {                        eventsListProgressBar.setVisibility(View.GONE);                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();                        return;                    }                    try                    {                        JSONArray jsonArray = new JSONArray(respStr);                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));                        if(jsonArray.length() < 1 || jsonArray.length() == 0)                        {                            eventsListProgressBar.setVisibility(View.INVISIBLE);                            eventsErrorMsg.setText("No Events Found");                            eventsErrorMsg.setVisibility(View.VISIBLE);                            errorImage.setVisibility(View.VISIBLE);                            fragmentEventsRecyclerview.setVisibility(View.INVISIBLE);                        }                        else                        {                            for(int i = 0; i <jsonArray.length(); i++)                            {                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);                                String type = jsonResponseObj.getString("type");                                String startTime = jsonResponseObj.getString("startTime");                                String subject = jsonResponseObj.getString("subject");                                String description = jsonResponseObj.getString("description");                                NewsEventDataModel newsEventDataModel = new NewsEventDataModel(type, subject, startTime, description);                                newsEventDataModelList.add(newsEventDataModel);                                new MyLog(NMIMSApplication.getAppContext()).debug("description",description);                            }                            eventsListRecyclerviewAdapter = new EventsListRecyclerviewAdapter(getContext(), newsEventDataModelList, new EventsListRecyclerviewAdapter.OpenEvents() {                                @Override                                public void openEventsDescription(String description)                                {                                    Bundle bundle = new Bundle();                                    bundle.putString("description", description);                                    new MyLog(NMIMSApplication.getAppContext()).debug("description",description);                                    NewsEventsDescriptionFragment newsEventsDescriptionFragment = new NewsEventsDescriptionFragment();                                    newsEventsDescriptionFragment.setArguments(bundle);                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();                                    ft.add(R.id.StudentHome,newsEventsDescriptionFragment);                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);                                    ft.addToBackStack("News And Event");                                    ft.commit();                                }                            });                            fragmentEventsRecyclerview.setAdapter(eventsListRecyclerviewAdapter);                            eventsListProgressBar.setVisibility(View.GONE);                        }                    }                    catch(Exception je)                    {                        eventsListProgressBar.setVisibility(View.GONE);                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());                        eventsErrorMsg.setText("No Events Found");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                        fragmentEventsRecyclerview.setVisibility(View.GONE);                    }                }            }, new Response.ErrorListener()            {                @Override                public void onErrorResponse(VolleyError error)                {                    eventsListProgressBar.setVisibility(View.GONE);                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());                    if (error instanceof TimeoutError)                    {                        eventsErrorMsg.setText("Oops! Connection timeout error!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error.getCause() instanceof ConnectException)                    {                        eventsErrorMsg.setText("Oops! Unable to reach server!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error instanceof NoConnectionError)                    {                        eventsErrorMsg.setText("Oops! No Internet Connection Available!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error.getCause() instanceof SocketException)                    {                        eventsErrorMsg.setText("Oops! We are Sorry Something went wrong. We're working on it now!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error instanceof AuthFailureError)                    {                        eventsErrorMsg.setText("Oops! Server couldn't find the authenticated request!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error instanceof ServerError)                    {                        eventsErrorMsg.setText("Oops! No response from server!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error instanceof NetworkError)                    {                        eventsErrorMsg.setText("Oops! It seems your internet is slow!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else if (error instanceof ParseError) {                        eventsErrorMsg.setText("Oops! Parse Error (because of invalid json or xml)!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                    else                    {                        eventsErrorMsg.setText("Oops! An unknown error occurred!");                        eventsErrorMsg.setVisibility(View.VISIBLE);                        fragmentEventsRecyclerview.setVisibility(View.GONE);                        errorImage.setVisibility(View.VISIBLE);                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));                    }                }            }){                @Override                public String getBodyContentType() {                    return "application/json; charset=utf-8";                }                @Override                public byte[] getBody() throws AuthFailureError {                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);                }                public Map<String, String> getHeaders() throws AuthFailureError {                    Map<String, String> headers = new HashMap<String, String>();                    headers.put("token", token);					headers.put("username", userName);                    return headers;                }            };            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));            requestQueue.add(stringRequest);        }        catch (Exception e)        {            new MyLog(NMIMSApplication.getAppContext()).debug("Event Exception", e.getMessage());        }    }}
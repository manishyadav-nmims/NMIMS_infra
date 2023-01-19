package com.nmims.app.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.nmims.app.Adapters.FindDetailsByAdminRecyclerviewAdapter;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.FetchUserDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FindDetailsByAdminActivity extends AppCompatActivity {

    private Button cancelSearchUserBtn, changeSearchStyle, searchUser;
    private EditText searchUserFDA, enterFirstNameFDA, enterLastNameFDA;
    private RecyclerView FDA_recyclerview;
    private ProgressBar fdaProgressBar;
    private TextView fdaEmptyResults, tvTitleFDA;
    private ImageView fda_errorImage;
    private List<FetchUserDataModel> fetchUserDataModelList = new ArrayList<>();
    private RequestQueue requestQueue;
    private String userName = "", firstName = "", lastName = "", myApiUrlLms = "", myApiUrlUsermgmt;
    private RelativeLayout FDA_searchBar, searchBarUsingFL;
    private boolean isFLVisible = false;
    private FindDetailsByAdminRecyclerviewAdapter findDetailsByAdminRecyclerviewAdapter;
    private int allowExitCount = 0;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_details_by_admin);
        dbHelper = new DBHelper(this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        //CommonMethods.handleSSLHandshake();
        myApiUrlUsermgmt = dbHelper.getBackEndControl("myApiUrlUsermgmt").getValue();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        tvTitleFDA = findViewById(R.id.tvTitleFDA);
        enterLastNameFDA = findViewById(R.id.enterLastNameFDA);
        enterFirstNameFDA = findViewById(R.id.enterFirstNameFDA);
        searchUser = findViewById(R.id.searchUser);
        searchBarUsingFL = findViewById(R.id.searchBarUsingFL);
        FDA_searchBar = findViewById(R.id.FDA_searchBar);
        cancelSearchUserBtn = findViewById(R.id.cancelSearchUserBtn);
        searchUserFDA = findViewById(R.id.searchUserFDA);
        FDA_recyclerview = findViewById(R.id.FDA_recyclerview);
        fdaProgressBar = findViewById(R.id.fdaProgressBar);
        fdaEmptyResults = findViewById(R.id.fdaEmptyResults);
        fda_errorImage = findViewById(R.id.fda_errorImage);
        changeSearchStyle = findViewById(R.id.changeSearchStyle);
        FDA_recyclerview.setHasFixedSize(true);
        FDA_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        searchUserFDA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cancelSearchUserBtn.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    cancelSearchUserBtn.setVisibility(View.VISIBLE);
                } else {
                    cancelSearchUserBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    cancelSearchUserBtn.setVisibility(View.VISIBLE);
                } else {
                    cancelSearchUserBtn.setVisibility(View.GONE);
                }
            }
        });

        enterFirstNameFDA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    fetchUserDataModelList.clear();
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                    if (fetchUserDataModelList.size() > 0) {
                        fetchUserDataModelList.clear();
                        findDetailsByAdminRecyclerviewAdapter.notifyDataSetChanged();
                        tvTitleFDA.setText("Find Details");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                    if (fetchUserDataModelList.size() > 0) {
                        fetchUserDataModelList.clear();
                        findDetailsByAdminRecyclerviewAdapter.notifyDataSetChanged();
                        tvTitleFDA.setText("Find Details");
                    }
                }
            }
        });

        enterLastNameFDA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    fetchUserDataModelList.clear();
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                    if (fetchUserDataModelList.size() > 0) {
                        fetchUserDataModelList.clear();
                        findDetailsByAdminRecyclerviewAdapter.notifyDataSetChanged();
                        tvTitleFDA.setText("Find Details");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    searchUser.setText("Search User");
                    searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                    if (fetchUserDataModelList.size() > 0) {
                        fetchUserDataModelList.clear();
                        findDetailsByAdminRecyclerviewAdapter.notifyDataSetChanged();
                        tvTitleFDA.setText("Find Details");
                    }
                }
            }
        });

        searchUserFDA.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchUserFDA.getWindowToken(), 0);
                    userName = searchUserFDA.getText().toString().trim();

                    if (fda_errorImage.getVisibility() == View.VISIBLE) {
                        fda_errorImage.setVisibility(View.GONE);
                        fdaEmptyResults.setVisibility(View.GONE);
                        FDA_recyclerview.setVisibility(View.VISIBLE);
                    }

                    searchUserDetails(userName);

                    return true;
                }
                return false;
            }
        });

        cancelSearchUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchUserDataModelList.clear();
                searchUserFDA.setText("");
            }
        });

        changeSearchStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFLVisible) {
                    isFLVisible = false;
                    searchBarUsingFL.setVisibility(View.GONE);
                    FDA_searchBar.setVisibility(View.VISIBLE);
                    fetchUserDataModelList.clear();
                } else {
                    isFLVisible = true;
                    searchBarUsingFL.setVisibility(View.VISIBLE);
                    FDA_searchBar.setVisibility(View.GONE);
                    fetchUserDataModelList.clear();
                }
            }
        });

        searchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchUser.getText().equals("Clear Search")) {
                    enterFirstNameFDA.setText("");
                    enterLastNameFDA.setText("");
                    searchUser.setText("Search User");
                    tvTitleFDA.setText("Find Details");
                    fetchUserDataModelList.clear();
                    findDetailsByAdminRecyclerviewAdapter.notifyDataSetChanged();
                } else {
                    firstName = enterFirstNameFDA.getText().toString().trim();
                    lastName = enterLastNameFDA.getText().toString().trim();

                    if (!firstName.equals("") || !lastName.equals("")) {
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(searchUserFDA.getWindowToken(), 0);
                        searchUser.setText("Searching...");
                        searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_green));

                        if (fda_errorImage.getVisibility() == View.VISIBLE) {
                            fda_errorImage.setVisibility(View.GONE);
                            fdaEmptyResults.setVisibility(View.GONE);
                            FDA_recyclerview.setVisibility(View.VISIBLE);
                        }

                        searchUserDetails(firstName, lastName);
                    } else {
                        new MyToast(FindDetailsByAdminActivity.this).showSmallCustomToast("Please Enter FirstName Or Lastname To Search");
                    }
                }
            }
        });
    }

    private void searchUserDetails(String username) {
        try {
            fdaProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("searchUserByUsername", "searchUserByUsername");

            if (fetchUserDataModelList.size() > 0) {
                fetchUserDataModelList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
            String URL = myApiUrlUsermgmt + "fetchUserDetailsByUserNameForSupportAdmin";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", userName);
            requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", userName);

            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", response);
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String username = "", firstname = "", lastname = "", enabled = "", email = "", mobile = "", schools = "", roles = "",
                                userRoles = "", acadSession = "", type = "", rollNo = "", campusName = "", campusId = "", programId = "", Status = "";
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonObjectLength", String.valueOf(jsonObj.length()));
                        if (response.length() < 1) {
                            fdaProgressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            fda_errorImage.setVisibility(View.VISIBLE);
                            fdaEmptyResults.setText("No User Found");
                            fdaEmptyResults.setVisibility(View.VISIBLE);
                            FDA_recyclerview.setVisibility(View.INVISIBLE);
                        } else {
                            if (jsonObj.has("Status")) {
                                Status = jsonObj.getString("Status");
                                new MyLog(NMIMSApplication.getAppContext()).debug("Status", Status);
                                fdaProgressBar.setVisibility(View.INVISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                fda_errorImage.setVisibility(View.VISIBLE);
                                fdaEmptyResults.setText("No User Found");
                                fdaEmptyResults.setVisibility(View.VISIBLE);
                                FDA_recyclerview.setVisibility(View.INVISIBLE);
                            } else {
                                if (jsonObj.has("username")) {
                                    username = jsonObj.getString("username");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
                                }
                                if (jsonObj.has("firstname")) {
                                    firstname = jsonObj.getString("firstname");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("firstname", firstname);
                                }
                                if (jsonObj.has("lastname")) {
                                    lastname = jsonObj.getString("lastname");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("lastname", lastname);
                                }
                                if (jsonObj.has("enabled")) {
                                    enabled = jsonObj.getString("enabled");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("enabled", enabled);
                                }
                                if (jsonObj.has("email")) {
                                    email = jsonObj.getString("email");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("email", email);
                                }
                                if (jsonObj.has("mobile")) {
                                    mobile = jsonObj.getString("mobile");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("mobile", mobile);
                                }
                                if (jsonObj.has("schools")) {
                                    schools = jsonObj.getString("schools");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("schools", schools);
                                }
                                if (jsonObj.has("roles")) {
                                    roles = jsonObj.getString("roles");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("roles", roles);
                                }
                                if (jsonObj.has("userRoles")) {
                                    userRoles = jsonObj.getString("userRoles");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("userRoles", userRoles);
                                }
                                if (jsonObj.has("acadSession")) {
                                    acadSession = jsonObj.getString("acadSession");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("acadSession", acadSession);
                                }
                                if (jsonObj.has("type")) {
                                    type = jsonObj.getString("type");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("type", type);
                                }
                                if (jsonObj.has("rollNo")) {
                                    rollNo = jsonObj.getString("rollNo");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("rollNo", rollNo);
                                }
                                if (jsonObj.has("campusName")) {
                                    campusName = jsonObj.getString("campusName");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("campusName", campusName);
                                }
                                if (jsonObj.has("campusId")) {
                                    campusId = jsonObj.getString("campusId");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("campusId", campusId);
                                }
                                if (jsonObj.has("programId")) {
                                    programId = jsonObj.getString("programId");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("programId", programId);
                                }

                                FetchUserDataModel fetchUserDataModel = new FetchUserDataModel(
                                        username, firstname, lastname, enabled, email, mobile, acadSession, type, roles, rollNo, campusName, campusId, programId, schools
                                );
                                fetchUserDataModelList.add(fetchUserDataModel);
                                new MyLog(NMIMSApplication.getAppContext()).debug("fetchUserDataModelList", fetchUserDataModelList.toString());

                                findDetailsByAdminRecyclerviewAdapter = new FindDetailsByAdminRecyclerviewAdapter(FindDetailsByAdminActivity.this, fetchUserDataModelList, new FindDetailsByAdminRecyclerviewAdapter.OpenUserDetails() {
                                    @Override
                                    public void openUserFullDetails(FetchUserDataModel fetchUserDataModel) {
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("fetchUserDataModel", fetchUserDataModel);
                                        Intent intent = new Intent(FindDetailsByAdminActivity.this, FullUserDetailsActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    }
                                });
                                FDA_recyclerview.setAdapter(findDetailsByAdminRecyclerviewAdapter);
                                fdaProgressBar.setVisibility(View.GONE);
                                FDA_searchBar.setVisibility(View.VISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }
                    } catch (Exception je) {
                        fdaProgressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                        fdaEmptyResults.setText("No User Found");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                        FDA_recyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fdaProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    new MyLog(FindDetailsByAdminActivity.this).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError) {
                        fdaEmptyResults.setText("Oops! Connection timeout error!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof ConnectException) {
                        fdaEmptyResults.setText("Oops! Unable to reach server!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NoConnectionError) {
                        fdaEmptyResults.setText("Oops! No Internet Connection Available!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof SocketException) {
                        fdaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof AuthFailureError) {
                        fdaEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ServerError) {
                        fdaEmptyResults.setText("Oops! No response from server!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NetworkError) {
                        fdaEmptyResults.setText("Oops! It seems your internet is slow!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ParseError) {
                        fdaEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else {
                        fdaEmptyResults.setText("Oops! An unknown error occurred!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
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
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("searchUserException", e.getMessage());
        }
    }

    private void searchUserDetails(String firstName, String LastName) {
        try {
            fdaProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("searchUserByUsername", "searchUserByUsername");

            if (fetchUserDataModelList.size() > 0) {
                fetchUserDataModelList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("firstName", firstName);
            new MyLog(NMIMSApplication.getAppContext()).debug("LastName", LastName);
            String URL = myApiUrlUsermgmt + "fetchUserDetailsByFirstLastNameForSupportAdmin";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", userName);
            requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("firstname", firstName);
            jsonObject.put("lastname", LastName);

            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                        String username = "", firstname = "", lastname = "", enabled = "", email = "", mobile = "", schools = "", roles = "",
                                userRoles = "", acadSession = "", type = "", rollNo = "", campusName = "", campusId = "", programId = "";
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArrayLength", String.valueOf(jsonArray.length()));
                        searchUser.setText("Search User");
                        searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                        if (jsonArray.length() < 1) {
                            fdaProgressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            fda_errorImage.setVisibility(View.VISIBLE);
                            fdaEmptyResults.setText("No User Found");
                            fdaEmptyResults.setVisibility(View.VISIBLE);
                            FDA_recyclerview.setVisibility(View.INVISIBLE);
                            searchUser.setText("Search User");
                            searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_black));
                        } else {
                            searchUser.setText("Clear Search");
                            searchUser.setBackgroundDrawable(getResources().getDrawable(R.drawable.reactangular_box_solid_red));
                            tvTitleFDA.setText("Find Details" + "     " + "ResultCount :" + jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);

                                if (jsonResponseObj.has("username")) {
                                    username = jsonResponseObj.getString("username");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
                                }
                                if (jsonResponseObj.has("firstname")) {
                                    firstname = jsonResponseObj.getString("firstname");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("firstname", firstname);
                                }
                                if (jsonResponseObj.has("lastname")) {
                                    lastname = jsonResponseObj.getString("lastname");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("lastname", lastname);
                                }
                                if (jsonResponseObj.has("enabled")) {
                                    enabled = jsonResponseObj.getString("enabled");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("enabled", enabled);
                                }
                                if (jsonResponseObj.has("email")) {
                                    email = jsonResponseObj.getString("email");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("email", email);
                                }
                                if (jsonResponseObj.has("mobile")) {
                                    mobile = jsonResponseObj.getString("mobile");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("mobile", mobile);
                                }
                                if (jsonResponseObj.has("schools")) {
                                    schools = jsonResponseObj.getString("schools");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("schools", schools);
                                }
                                if (jsonResponseObj.has("roles")) {
                                    roles = jsonResponseObj.getString("roles");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("roles", roles);
                                }
                                if (jsonResponseObj.has("userRoles")) {
                                    userRoles = jsonResponseObj.getString("userRoles");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("userRoles", userRoles);
                                }
                                if (jsonResponseObj.has("acadSession")) {
                                    acadSession = jsonResponseObj.getString("acadSession");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("acadSession", acadSession);
                                }
                                if (jsonResponseObj.has("type")) {
                                    type = jsonResponseObj.getString("type");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("type", type);
                                }
                                if (jsonResponseObj.has("rollNo")) {
                                    rollNo = jsonResponseObj.getString("rollNo");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("rollNo", rollNo);
                                }
                                if (jsonResponseObj.has("campusName")) {
                                    campusName = jsonResponseObj.getString("campusName");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("campusName", campusName);
                                }
                                if (jsonResponseObj.has("campusId")) {
                                    campusId = jsonResponseObj.getString("campusId");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("campusId", campusId);
                                }
                                if (jsonResponseObj.has("programId")) {
                                    programId = jsonResponseObj.getString("programId");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("programId", programId);
                                }

                                FetchUserDataModel fetchUserDataModel = new FetchUserDataModel(
                                        username, firstname, lastname, enabled, email, mobile, acadSession, type, roles, rollNo, campusName, campusId, programId, schools
                                );
                                fetchUserDataModelList.add(fetchUserDataModel);
                                new MyLog(NMIMSApplication.getAppContext()).debug("fetchUserDataModelList", fetchUserDataModelList.toString());

                            }

                            findDetailsByAdminRecyclerviewAdapter = new FindDetailsByAdminRecyclerviewAdapter(FindDetailsByAdminActivity.this, fetchUserDataModelList, new FindDetailsByAdminRecyclerviewAdapter.OpenUserDetails() {
                                @Override
                                public void openUserFullDetails(FetchUserDataModel fetchUserDataModel) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("fetchUserDataModel", fetchUserDataModel);
                                    Intent intent = new Intent(FindDetailsByAdminActivity.this, FullUserDetailsActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            });
                            FDA_recyclerview.setAdapter(findDetailsByAdminRecyclerviewAdapter);
                            fdaProgressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    } catch (Exception je) {
                        fdaProgressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                        fdaEmptyResults.setText("No User Found");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                        FDA_recyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fdaProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    searchUser.setText("Search User");
                    new MyLog(FindDetailsByAdminActivity.this).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError) {
                        fdaEmptyResults.setText("Oops! Connection timeout error!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof ConnectException) {
                        fdaEmptyResults.setText("Oops! Unable to reach server!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NoConnectionError) {
                        fdaEmptyResults.setText("Oops! No Internet Connection Available!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof SocketException) {
                        fdaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof AuthFailureError) {
                        fdaEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ServerError) {
                        fdaEmptyResults.setText("Oops! No response from server!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NetworkError) {
                        fdaEmptyResults.setText("Oops! It seems your internet is slow!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ParseError) {
                        fdaEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else {
                        fdaEmptyResults.setText("Oops! An unknown error occurred!");
                        fdaEmptyResults.setVisibility(View.VISIBLE);
                        FDA_recyclerview.setVisibility(View.GONE);
                        fda_errorImage.setVisibility(View.VISIBLE);
                        fda_errorImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
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
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(600000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("searchUserException", e.getMessage());
        }
    }
}

package com.nmims.app.Fragments.Academic;


import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Activities.ParentDrawer;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SupportFragment extends Fragment {


    private TextView emailTv, contactUsTv;
    private FrameLayout supportFrag;

    public SupportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        checkRole();
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        emailTv = view.findViewById(R.id.emailTv);
        contactUsTv = view.findViewById(R.id.contactUsTv);
        supportFrag = view.findViewById(R.id.supportFrag);
        supportFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont delete
            }
        });
        emailTv.setText(Html.fromHtml("<a href=\"mailto:portal_app_team@svkm.ac.in\">portal_app_team@svkm.ac.in</a>"));
        emailTv.setMovementMethod(LinkMovementMethod.getInstance());

        contactUsTv.setText(Html.fromHtml("<a href=\"tel:022 42199993\">022 42199993</a>"));
        contactUsTv.setMovementMethod(LinkMovementMethod.getInstance());

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("support_fragment", "support_fragment");
        mFirebaseAnalytics.logEvent("Support_Fragment", params);
        ///////////////////////////////////////////////

       /* new MyLog(NMIMSApplication.getAppContext()).debug( "myApiUrlUsermgmt" , NMIMSApplication.myApiUrlUsermgmt);
        new MyLog(NMIMSApplication.getAppContext()).debug( "myApiUrlLms" , NMIMSApplication.myApiUrlLms);
        new MyLog(NMIMSApplication.getAppContext()).debug( "myApiUrlUsermgmtCrud" , NMIMSApplication.myApiUrlUsermgmtCrud);
        new MyLog(NMIMSApplication.getAppContext()).debug( "myApiUrlSurvey" , NMIMSApplication.myApiUrlSurvey);*/

        return view;
    }

    private void checkRole()
    {
        DBHelper dbHelper = new DBHelper(getContext());
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                String role= cursor.getString(cursor.getColumnIndex("role"));
                new MyLog(NMIMSApplication.getAppContext()).debug("role", role);

                if(role.contains("ROLE_FACULTY"))
                {
                    ((FacultyDrawer)getActivity()).showShuffleBtn(false);
                    ((FacultyDrawer)getActivity()).setActionBarTitle("Support");

                }
                else if(role.contains("ROLE_STUDENT"))
                {
                    new MyLog(getContext()).debug("ActivityName_Support",getActivity().getClass().getSimpleName());
                    if(getActivity().getClass().getSimpleName().equals("StudentDrawer"))
                    {
                        ((StudentDrawer)getActivity()).showAnnouncements(false);
                        ((StudentDrawer)getActivity()).setActionBarTitle("Support");
                    }
                    else
                    {
                        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Support");
                        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);
                    }
                }
                else
                {
                    ((ParentDrawer)getActivity()).setActionBarTitle("Support");
                }
            }
        }
    }
}

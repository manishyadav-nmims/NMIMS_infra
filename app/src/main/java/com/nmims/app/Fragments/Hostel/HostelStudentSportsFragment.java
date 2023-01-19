package com.nmims.app.Fragments.Hostel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.R;

public class HostelStudentSportsFragment extends Fragment
{
    private TextView typesOfSportTV, yogaTV, noOfCompetitionParticipatedTV, shortDescriptionSportsTV;
    private RelativeLayout layoutTypesOfSport, layoutYogaTV, layoutNoOfCompetitionParticipatedTV;
    private boolean isSportsVisible = true, isYogaVisible = false, isCompetitionVisible = false, isShortDescriptionVisible = false;
    private Button submitShortDescriptionMessageBtn;
    private EditText shortDescriptionMessageET;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_sports,container,false);

        typesOfSportTV = view.findViewById(R.id.typesOfSportTV);
        yogaTV = view.findViewById(R.id.yogaTV);
        noOfCompetitionParticipatedTV = view.findViewById(R.id.noOfCompetitionParticipatedTV);

        layoutTypesOfSport = view.findViewById(R.id.layoutTypesOfSport);
        layoutYogaTV = view.findViewById(R.id.layoutYogaTV);
        layoutNoOfCompetitionParticipatedTV = view.findViewById(R.id.layoutNoOfCompetitionParticipatedTV);

        submitShortDescriptionMessageBtn = view.findViewById(R.id.submitShortDescriptionMessageBtn);
        shortDescriptionMessageET = view.findViewById(R.id.shortDescriptionMessageET);
        shortDescriptionSportsTV = view.findViewById(R.id.shortDescriptionSportsTV);

        typesOfSportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isSportsVisible)
                {
                    isSportsVisible = false;
                    typesOfSportTV.setText("TYPES OF SPORTS INTRESTED IN  +");
                    layoutTypesOfSport.setVisibility(View.GONE);
                }
                else
                {
                    typesOfSportTV.setText("TYPES OF SPORTS INTRESTED IN  -");
                    isSportsVisible = true;
                    layoutTypesOfSport.setVisibility(View.VISIBLE);
                    isYogaVisible = false;
                    yogaTV.setText("YOGA  +");
                    layoutYogaTV.setVisibility(View.GONE);
                    isCompetitionVisible = false;
                    layoutNoOfCompetitionParticipatedTV.setVisibility(View.GONE);
                    noOfCompetitionParticipatedTV.setText("NO. OF COMPETITON PARTICIPATED  +");
                    isShortDescriptionVisible = false;
                    submitShortDescriptionMessageBtn.setVisibility(View.GONE);
                    shortDescriptionMessageET.setVisibility(View.GONE);
                    shortDescriptionSportsTV.setText("SHORT DESCRIPTION  +");
                }
            }
        });

        yogaTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isYogaVisible)
                {
                    isYogaVisible = false;
                    yogaTV.setText("YOGA  +");
                    layoutYogaTV.setVisibility(View.GONE);
                }
                else
                {
                    isYogaVisible = true;
                    yogaTV.setText("YOGA  -");
                    layoutYogaTV.setVisibility(View.VISIBLE);
                    isSportsVisible = false;
                    typesOfSportTV.setText("TYPES OF SPORTS INTRESTED IN  +");
                    layoutTypesOfSport.setVisibility(View.GONE);
                    isCompetitionVisible = false;
                    noOfCompetitionParticipatedTV.setText("NO. OF COMPETITON PARTICIPATED  +");
                    layoutNoOfCompetitionParticipatedTV.setVisibility(View.GONE);
                    isShortDescriptionVisible = false;
                    submitShortDescriptionMessageBtn.setVisibility(View.GONE);
                    shortDescriptionMessageET.setVisibility(View.GONE);
                    shortDescriptionSportsTV.setText("SHORT DESCRIPTION  +");
                }
            }
        });

        noOfCompetitionParticipatedTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isCompetitionVisible)
                {
                    isCompetitionVisible = false;
                    noOfCompetitionParticipatedTV.setText("NO. OF COMPETITON PARTICIPATED  +");
                    layoutNoOfCompetitionParticipatedTV.setVisibility(View.GONE);
                }
                else
                {
                    isCompetitionVisible = true;
                    noOfCompetitionParticipatedTV.setText("NO. OF COMPETITON PARTICIPATED  -");
                    layoutNoOfCompetitionParticipatedTV.setVisibility(View.VISIBLE);
                    isYogaVisible = false;
                    yogaTV.setText("YOGA  +");
                    layoutYogaTV.setVisibility(View.GONE);
                    isSportsVisible = false;
                    layoutTypesOfSport.setVisibility(View.GONE);
                    typesOfSportTV.setText("TYPES OF SPORTS INTRESTED IN  +");
                    isShortDescriptionVisible = false;
                    submitShortDescriptionMessageBtn.setVisibility(View.GONE);
                    shortDescriptionMessageET.setVisibility(View.GONE);
                    shortDescriptionSportsTV.setText("SHORT DESCRIPTION  +");
                }
            }
        });

        shortDescriptionSportsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShortDescriptionVisible)
                {
                    isShortDescriptionVisible = false;
                    shortDescriptionSportsTV.setText("SHORT DESCRIPTION  +");
                    submitShortDescriptionMessageBtn.setVisibility(View.GONE);
                    shortDescriptionMessageET.setVisibility(View.GONE);
                }
                else
                {
                    isShortDescriptionVisible = true;
                    shortDescriptionSportsTV.setText("SHORT DESCRIPTION  -");
                    submitShortDescriptionMessageBtn.setVisibility(View.VISIBLE);
                    shortDescriptionMessageET.setVisibility(View.VISIBLE);
                    isSportsVisible = false;
                    layoutTypesOfSport.setVisibility(View.GONE);
                    typesOfSportTV.setText("TYPES OF SPORTS INTRESTED IN  +");
                    isYogaVisible = false;
                    yogaTV.setText("YOGA  +");
                    layoutYogaTV.setVisibility(View.GONE);
                    isCompetitionVisible = false;
                    noOfCompetitionParticipatedTV.setText("NO. OF COMPETITON PARTICIPATED  +");
                    layoutNoOfCompetitionParticipatedTV.setVisibility(View.GONE);
                }
            }
        });

        ((StudentHostelHomeDrawer)getActivity()).showAcademics(false);
        ((StudentHostelHomeDrawer)getActivity()).setActionBarTitle("Sports");
        return view;
    }
}

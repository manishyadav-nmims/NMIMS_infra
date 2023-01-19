package com.nmims.app.Fragments.Academic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.R;

public class CovidApplyLeave extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_covid_apply_leave,container,false);
        //CommonMethods.handleSSLHandshake();
        return view;
    }
}

package com.nmims.app.Services;

import com.nmims.app.Models.FacultyTimetable;
import com.nmims.app.Models.HolidayModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitInterface {


    @POST("mobileApi/date/current-session")
    Call<HolidayModel> getHolidayList();

    @FormUrlEncoded
    @POST("mobileApi/faculty/fetch-all-lectures")
    Call<FacultyTimetable> getFacultyTimetable(@Field("facultyId") String facultyId);
}

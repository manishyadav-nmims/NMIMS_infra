package com.nmims.app.Models.Survey;

public class SurveyUserResponse
{
    private String id;
    private String survey_id;
    private String question_id;
    private String response;
    private String user_id;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "SurveyUserResponse{" +
                "id='" + id + '\'' +
                ", survey_id='" + survey_id + '\'' +
                ", question_id='" + question_id + '\'' +
                ", response='" + response + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", submitted_date_time='" + submitted_date_time + '\'' +
                '}';
    }

    private String submitted_date_time;


    public SurveyUserResponse() {
    }

    public SurveyUserResponse(String id, String survey_id, String question_id, String response) {
        this.id = id;
        this.survey_id = survey_id;
        this.question_id = question_id;
        this.response = response;
    }

    public SurveyUserResponse(String survey_id, String question_id, String response) {
        this.survey_id = survey_id;
        this.question_id = question_id;
        this.response = response;
    }

    public SurveyUserResponse(String survey_id, String question_id, String response, String user_id, String submitted_date_time) {
        this.survey_id = survey_id;
        this.question_id = question_id;
        this.response = response;
        this.user_id = user_id;
        this.submitted_date_time = submitted_date_time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSubmitted_date_time() {
        return submitted_date_time;
    }

    public void setSubmitted_date_time(String submitted_date_time) {
        this.submitted_date_time = submitted_date_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSurvey_id() {
        return survey_id;
    }

    public void setSurvey_id(String survey_id) {
        this.survey_id = survey_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}

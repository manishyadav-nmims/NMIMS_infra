package com.nmims.app.Models.Survey;

import java.util.List;

public class SurveyName
{
    private String id;
    private String surveyName;
    private String userId;
    private String startDate;
    private String endDate;
    private List<SurveyQuestion> questionList;
    private String submitted;
    private String responseFlag;
    private String createdDate;
    private String locallySubmitted;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public SurveyName() {
    }

    public SurveyName(String id, String surveyName, String userId, String startDate, String endDate, String submitted, String responseFlag, String createdDate, String locallySubmitted) {
        this.id = id;
        this.surveyName = surveyName;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.submitted = submitted;
        this.responseFlag = responseFlag;
        this.createdDate = createdDate;
        this.locallySubmitted = locallySubmitted;
    }

    public SurveyName(String surveyName, String userId, String startDate, String endDate, String submitted, String responseFlag, String createdDate, String locallySubmitted) {
        this.surveyName = surveyName;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.submitted = submitted;
        this.responseFlag = responseFlag;
        this.createdDate = createdDate;
        this.locallySubmitted = locallySubmitted;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<SurveyQuestion> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<SurveyQuestion> questionList) {
        this.questionList = questionList;
    }

    public String getSubmitted() {
        return submitted;
    }

    public void setSubmitted(String submitted) {
        this.submitted = submitted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(String responseFlag) {
        this.responseFlag = responseFlag;
    }

    public String getLocallySubmitted() {
        return locallySubmitted;
    }

    public void setLocallySubmitted(String locallySubmitted) {
        this.locallySubmitted = locallySubmitted;
    }

    @Override
    public String toString() {
        return "SurveyName{" +
                "id='" + id + '\'' +
                ", surveyName='" + surveyName + '\'' +
                ", userId='" + userId + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", questionList=" + questionList +
                ", submitted='" + submitted + '\'' +
                ", responseFlag='" + responseFlag + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", locallySubmitted='" + locallySubmitted + '\'' +
                '}';
    }
}

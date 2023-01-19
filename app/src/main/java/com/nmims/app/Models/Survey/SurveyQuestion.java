package com.nmims.app.Models.Survey;

import java.util.List;

public class SurveyQuestion
{
    private String id;
    private String title;
    private String type_id;
    private String isChecked;
    private String survey_id;
    private List<SurveyOption> optionList;


    @Override
    public String toString() {
        return "SurveyQuestion{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type_id='" + type_id + '\'' +
                ", isChecked='" + isChecked + '\'' +
                ", survey_id='" + survey_id + '\'' +
                ", optionList=" + optionList +
                '}';
    }

    public SurveyQuestion(String id, String title, String type_id, String isChecked, String survey_id, List<SurveyOption> optionList) {
        this.id = id;
        this.title = title;
        this.type_id = type_id;
        this.isChecked = isChecked;
        this.survey_id = survey_id;
        this.optionList = optionList;
    }

    public SurveyQuestion(String id, String title, String type_id, String isChecked, String survey_id) {
        this.id = id;
        this.title = title;
        this.type_id = type_id;
        this.isChecked = isChecked;
        this.survey_id = survey_id;
    }

    public SurveyQuestion(String title, String type_id, String isChecked, String survey_id) {
        this.title = title;
        this.type_id = type_id;
        this.isChecked = isChecked;
        this.survey_id = survey_id;
    }

    public SurveyQuestion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(String isChecked) {
        this.isChecked = isChecked;
    }

    public String getSurvey_id() {
        return survey_id;
    }

    public void setSurvey_id(String survey_id) {
        this.survey_id = survey_id;
    }

    public List<SurveyOption> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<SurveyOption> optionList) {
        this.optionList = optionList;
    }
}

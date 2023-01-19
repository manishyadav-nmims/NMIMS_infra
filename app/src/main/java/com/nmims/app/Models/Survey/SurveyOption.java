package com.nmims.app.Models.Survey;

public class SurveyOption
{
    private String id;
    private String options;
    private String question_id;
    private String type_id;

    @Override
    public String toString() {
        return "SurveyOption{" +
                "id='" + id + '\'' +
                ", options='" + options + '\'' +
                ", question_id='" + question_id + '\'' +
                ", type_id='" + type_id + '\'' +
                '}';
    }

    public SurveyOption(String id, String options, String question_id, String type_id) {
        this.id = id;
        this.options = options;
        this.question_id = question_id;
        this.type_id = type_id;
    }

    public SurveyOption(String options, String question_id, String type_id) {
        this.options = options;
        this.question_id = question_id;
        this.type_id = type_id;
    }

    public SurveyOption() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }
}

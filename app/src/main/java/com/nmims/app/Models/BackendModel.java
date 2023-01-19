package com.nmims.app.Models;

public class BackendModel
{
    private String key;
    private String value;

    public BackendModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public BackendModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BackendModel{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

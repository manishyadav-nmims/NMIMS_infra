package com.nmims.app.Models;

public class ICAComponentsDataModel
{
    private String componentId;
    private String componentName;
    private String componentMarks;

    public ICAComponentsDataModel() {
    }

    public ICAComponentsDataModel(String componentId, String componentName, String componentMarks) {
        this.componentId = componentId;
        this.componentName = componentName;
        this.componentMarks = componentMarks;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentMarks() {
        return componentMarks;
    }

    public void setComponentMarks(String componentMarks) {
        this.componentMarks = componentMarks;
    }
}

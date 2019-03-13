package com.smoftware.elmour.quest;

import com.badlogic.gdx.utils.ObjectMap;

public class QuestTask {

    public static enum QuestType{
        FETCH,
        KILL,
        DELIVERY,
        GUARD,
        ESCORT,
        RETURN,
        DISCOVER
    }

    public static enum QuestTaskPropertyType{
        IS_TASK_COMPLETE,
        TARGET_TYPE,
        TARGET_NUM,
        TARGET_LOCATION,
        NONE
    }

    private ObjectMap<QuestTaskPropertyType, Object> taskProperties;
    private String id;
    private String taskPhrase;
    private QuestType questType;

    public QuestTask(){
        taskProperties = new ObjectMap<QuestTaskPropertyType, Object>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getTaskPhrase() {
        return taskPhrase;
    }

    public void setTaskPhrase(String taskPhrase) {
        this.taskPhrase = taskPhrase;
    }

    public QuestType getQuestType() {
        return questType;
    }

    public void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    public ObjectMap<QuestTaskPropertyType, Object> getTaskProperties() {
        return taskProperties;
    }

    public void setTaskProperties(ObjectMap<QuestTaskPropertyType, Object> taskProperties) {
        this.taskProperties = taskProperties;
    }

    public boolean isTaskComplete(){
        if( !taskProperties.containsKey(QuestTaskPropertyType.IS_TASK_COMPLETE) ){
            setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
            return false;
        }
        String val = taskProperties.get(QuestTaskPropertyType.IS_TASK_COMPLETE).toString();
        return Boolean.parseBoolean(val);
    }

    public void setTaskComplete(){
        setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE, "true");
    }

    public void resetAllProperties(){
        taskProperties.put(QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
    }

    public void setPropertyValue(QuestTaskPropertyType key, String value){
        taskProperties.put(key, value);
    }

    public String getPropertyValue(QuestTaskPropertyType key){
        Object propertyVal = taskProperties.get(key);
        if( propertyVal == null ) return new String();
        return propertyVal.toString();
    }

    public String toString(){
        return taskPhrase;
    }



}

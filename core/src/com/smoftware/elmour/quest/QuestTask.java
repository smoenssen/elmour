package com.smoftware.elmour.quest;

public class QuestTask {

    public enum QuestTaskType {
        FETCH,
        KILL,
        DELIVERY,
        GUARD,
        ESCORT,
        RETURN,
        DISCOVER,
        QUEST
    }

    public enum QuestTaskStatus {
        NOT_STARTED,
        STARTED,
        COMPLETE
    }

    public String yedNodeId;
    private String id;
    private QuestTaskType questTaskType;
    private QuestTaskStatus questTaskStatus;
    private String targetType;
    private String targetLocation;
    private String taskPhrase;
    private int targetNumber;
    private QuestList subQuestList;

    public QuestTask() { /* defaulting variables here could mess up serialization to .json */ }

    public String getId() { return id; }

    public void setId(String id){ this.id = id; }

    public String getTaskPhrase() { return taskPhrase; }

    public void setTaskPhrase(String taskPhrase) { this.taskPhrase = taskPhrase; }

    public QuestTaskType getQuestTaskType() { return questTaskType; }

    public void setQuestTaskType(QuestTaskType questTaskType) { this.questTaskType = questTaskType; }

    public QuestTaskStatus getQuestTaskStatus() { return questTaskStatus; }

    public void setQuestTaskStatus(QuestTaskStatus status) { questTaskStatus = status; }

    public String getTargetLocation() { return targetLocation; }

    public void setTargetLocation(String location) { targetLocation = location; }

    public String getTargetType() { return targetType; }

    public void setTargetType(String type) { targetType = type; }

    public void setTargetNumber(int number) { targetNumber = number; }

    public int getTargetNumber() { return targetNumber; }

    public QuestList getSubQuestList() { return subQuestList; }

    public void addSubQuest(QuestGraph questGraph) {
        if (subQuestList == null) {
            subQuestList = new QuestList(true);
        }

        subQuestList.addQuest(questGraph); }

    // convenience functions
    public boolean isTaskStarted(){ return questTaskStatus == QuestTaskStatus.STARTED || questTaskStatus == QuestTaskStatus.COMPLETE; }

    public boolean isTaskComplete(){ return questTaskStatus == QuestTaskStatus.COMPLETE; }

    public void setTaskStarted() { questTaskStatus = QuestTaskStatus.STARTED; }

    public void setTaskComplete() { questTaskStatus = QuestTaskStatus.COMPLETE; }

    public String toString(){ return taskPhrase; }
}

package com.smoftware.elmour.quest;

import com.smoftware.elmour.EntityConfig;

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
    private String targetEntity;
    private EntityConfig.ConversationType conversationType;
    private EntityConfig.ConversationType postTaskConversationType;
    private String taskPhrase;
    private int targetNumber;
    private boolean isSpoiler;
    private QuestList subQuestList;
    private String parentQuestId;

    public QuestTask() { /* defaulting variables here could mess up serialization to .json */ }

    public String getId() { return id; }

    public void setId(String id){ this.id = id; }

    public String getTaskPhrase() { return taskPhrase; }

    public void setTaskPhrase(String taskPhrase) { this.taskPhrase = taskPhrase; }

    public QuestTaskType getQuestTaskType() { return questTaskType; }

    public void setQuestTaskType(QuestTaskType questTaskType) { this.questTaskType = questTaskType; }

    public QuestTaskStatus getQuestTaskStatus() { return questTaskStatus; }

    public void setQuestTaskStatus(QuestTaskStatus status) { questTaskStatus = status; }

    public EntityConfig.ConversationType getConversationType() { return conversationType; }

    public void setConversationType(EntityConfig.ConversationType conversationType) { this.conversationType = conversationType; }

    public EntityConfig.ConversationType getPostTaskConversationType() { return postTaskConversationType; }

    public void setPostTaskConversationType(EntityConfig.ConversationType postTaskConversationType) { this.postTaskConversationType = postTaskConversationType; }

    public String getTargetEntity() { return targetEntity; }

    public void setTargetEntity(String entity) { targetEntity = entity; }

    public void setTargetNumber(int number) { targetNumber = number; }

    public int getTargetNumber() { return targetNumber; }

    public void setIsSpoiler(boolean isSpoiler) { this.isSpoiler = isSpoiler; }

    public boolean isSpoiler() { return isSpoiler; }

    public QuestList getSubQuestList() { return subQuestList; }

    public void addSubQuest(QuestGraph questGraph) {
        if (subQuestList == null) {
            subQuestList = new QuestList(true);
        }

        subQuestList.addQuest(questGraph);
    }

    public void setParentQuestId(String questTaskId) { parentQuestId = questTaskId; }

    public String getParentQuestId() { return parentQuestId; }

    // convenience functions
    public boolean isTaskStarted(){ return questTaskStatus == QuestTaskStatus.STARTED || questTaskStatus == QuestTaskStatus.COMPLETE; }

    public boolean isTaskComplete(){ return questTaskStatus == QuestTaskStatus.COMPLETE; }

    public void setTaskStarted() { questTaskStatus = QuestTaskStatus.STARTED; }

    public void setTaskComplete() {
        questTaskStatus = QuestTaskStatus.COMPLETE; }

    public String toString(){ return taskPhrase; }
}

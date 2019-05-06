package com.smoftware.elmour.quest;

public class QuestDependency {
    private String sourceId;
    private String destinationId;

    public QuestDependency(){}

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId){
        this.sourceId = sourceId;
    }

    public String getDestinationId() { return destinationId; }

    public void setDestinationId(String destinationId){
        this.destinationId = destinationId;
    }
}

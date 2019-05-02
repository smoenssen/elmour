package com.smoftware.elmour.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityConfig.ConversationConfig;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

public class QuestGraph {
    private static final String TAG = QuestGraph.class.getSimpleName();

    public enum QuestStatus { NOT_STARTED, IN_PROGRESS, COMPLETE }

    private Hashtable<String, QuestTask> questTasks;
    private Hashtable<String, ArrayList<QuestTaskDependency>> questTaskDependencies;
    private String questTitle;
    private String questID;
    private String questGiver;
    private boolean isQuestComplete;
    private QuestStatus questStatus;
    private int chapter;
    private int goldReward;
    private int xpReward;

    public void setChapter(int chapter) { this.chapter = chapter; }

    public int getChapter() { return chapter; }

    public int getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(int goldReward) {
        this.goldReward = goldReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public void setQuestStatus(QuestStatus questStatus) { this.questStatus = questStatus; }

    public QuestStatus getQuestStatus() { return questStatus; }

    public String getQuestID() { return questID; }

    public void setQuestID(String questID) {
        this.questID = questID;
    }

    public String getQuestGiver() { return questGiver; }

    public void setQuestGiver(String questGiver) { this.questGiver = questGiver; }

    public String getQuestTitle() {
        return questTitle;
    }

    public void setQuestTitle(String questTitle) {
        this.questTitle = questTitle;
    }

    public boolean isQuestComplete() { return questStatus == QuestStatus.COMPLETE; }

    public void setQuestComplete() {
        questStatus = QuestStatus.COMPLETE;

        // need to set all associated NPCs to post-quest dialog for this quest
        ArrayList<QuestTask> tasks = getAllQuestTasks();
        for( QuestTask task: tasks ){
            String targetType = task.getTargetType();
            Entity entity;

            try {
                entity = EntityFactory.getInstance().getEntityByName(targetType);
            }
            catch (NullPointerException e) {
                continue;
            }

            Array<ConversationConfig> conversationConfigs = entity.getEntityConfig().getConversationConfigs();
            for (ConversationConfig conversationConfig : conversationConfigs) {
                if (conversationConfig.type == EntityConfig.ConversationType.POST_QUEST_DIALOG) {
                    ProfileManager.getInstance().setProperty(
                            entity.getEntityConfig().getEntityID() + ConversationConfig.class.getSimpleName(), conversationConfig);
                    break;
                }
            }
        }
    }

    public boolean areAllTasksComplete(){
        ArrayList<QuestTask> tasks = getAllQuestTasks();
        for( QuestTask task: tasks ){
            if( !task.isTaskComplete() ){
                return false;
            }
        }
        return true;
    }

    public void setTasks(Hashtable<String, QuestTask> questTasks) {
        if( questTasks.size() < 0 ){
            throw new IllegalArgumentException("Can't have a negative amount of conversations");
        }

        this.questTasks = questTasks;
        this.questTaskDependencies = new Hashtable<String, ArrayList<QuestTaskDependency>>(questTasks.size());

        for( QuestTask questTask: questTasks.values() ){
            questTaskDependencies.put(questTask.getId(), new ArrayList<QuestTaskDependency>());
        }
    }

    public ArrayList<QuestTask> getAllQuestTasks(){
        Enumeration<QuestTask> enumeration = questTasks.elements();
        return Collections.list(enumeration);
    }

    public void clear(){
        questTasks.clear();
        questTaskDependencies.clear();
    }

    public boolean isValid(String taskID){
        QuestTask questTask = questTasks.get(taskID);
        if( questTask == null ) return false;
        return true;
    }

    public boolean isReachable(String sourceID, String sinkID){
        if( !isValid(sourceID) || !isValid(sinkID) ) return false;
        if( questTasks.get(sourceID) == null ) return false;

        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(sourceID);
        if( list == null ) return false;
        for(QuestTaskDependency dependency: list){
            if(     dependency.getSourceId().equalsIgnoreCase(sourceID) &&
                    dependency.getDestinationId().equalsIgnoreCase(sinkID) ){
                return true;
            }
        }
        return false;
    }

    public QuestTask getQuestTaskByID(String id){
        if( !isValid(id) ){
            //System.out.println("Id " + id + " is not valid!");
            return null;
        }
        return questTasks.get(id);
    }

    public void addDependency(QuestTaskDependency questTaskDependency){
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(questTaskDependency.getSourceId());
        if( list == null) return;

        //Will not add if creates cycles
        if( doesCycleExist(questTaskDependency) ){
            //System.out.println("Cycle exists! Not adding");
            return;
        }

        list.add(questTaskDependency);
    }

    public boolean doesCycleExist(QuestTaskDependency questTaskDep){
        Set<String> keys = questTasks.keySet();
        for( String id: keys ){
            if( doesQuestTaskHaveDependencies(id) &&
                    questTaskDep.getDestinationId().equalsIgnoreCase(id)){
                    //System.out.println("ID: " + id + " destID: " + questTaskDep.getDestinationId());
                    return true;
                }
            }
        return false;
    }

    public boolean doesQuestTaskHaveDependencies(String id){
        QuestTask task = getQuestTaskByID(id);
        if( task == null) return false;
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(id);

        if( list.isEmpty() || list.size() == 0){
            return false;
        }else{
            return true;
        }
    }

    public boolean updateQuestForReturn(){
        ArrayList<QuestTask> tasks = getAllQuestTasks();
        QuestTask readyTask = null;

        //First, see if all tasks are available, meaning no blocking dependencies
        for( QuestTask task : tasks){
            if( !isQuestTaskAvailable(task.getId())){
                return false;
            }
            if( !task.isTaskComplete() ){
                if( task.getQuestTaskType().equals(QuestTask.QuestTaskType.RETURN) ){
                    readyTask = task;
                }else{
                    return false;
                }
            }
        }
        if( readyTask == null ) return false;
        readyTask.setTaskComplete();
        return true;
    }

    public boolean isQuestTaskAvailable(String id){
        QuestTask task = getQuestTaskByID(id);
        if( task == null) return false;
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(id);

        for(QuestTaskDependency dep: list){
            QuestTask depTask = getQuestTaskByID(dep.getDestinationId());
            if( depTask == null || depTask.isTaskComplete() ) continue;
            if( dep.getSourceId().equalsIgnoreCase(id) ){
                return false;
            }
        }
        return true;
    }

    public void setQuestTaskStarted(String id){
        QuestTask task = getQuestTaskByID(id);
        if( task == null) return;
        task.setTaskStarted();
    }

    public void setQuestTaskComplete(String id){
        QuestTask task = getQuestTaskByID(id);
        if( task == null) return;
        task.setTaskComplete();
    }

    public void update(MapManager mapMgr){
        ArrayList<QuestTask> allQuestTasks = getAllQuestTasks();
        for( QuestTask questTask: allQuestTasks ) {

            if( questTask.isTaskComplete() ) continue;

            //We first want to make sure the task is available and is relevant to current location
            if (!isQuestTaskAvailable(questTask.getId())) continue;

            String taskLocation = questTask.getTargetLocation();
            if (taskLocation == null ||
                    taskLocation.isEmpty() ||
                    !taskLocation.equalsIgnoreCase(mapMgr.getCurrentMapType().toString())) continue;

            switch (questTask.getQuestTaskType()) {
                case FETCH:
                    String taskConfig = questTask.getTargetType();
                    if( taskConfig == null || taskConfig.isEmpty() ) break;
                    EntityConfig config = Entity.getEntityConfig(taskConfig);

                    Array<Vector2> questItemPositions = ProfileManager.getInstance().getProperty(config.getEntityID(), Array.class);
                    if( questItemPositions == null ) break;

                    //Case where all the items have been picked up
                    if( questItemPositions.size == 0 ){
                        questTask.setTaskComplete();
                        Gdx.app.debug(TAG, "TASK : " + questTask.getId() + " is complete of Quest: " + questID);
                        Gdx.app.debug(TAG, "INFO : " + questTask.getTargetType());
                    }
                    break;
                case KILL:
                    break;
                case DELIVERY:
                    break;
                case GUARD:
                    break;
                case ESCORT:
                    break;
                case RETURN:
                    break;
                case DISCOVER:
                    break;
            }
        }
    }

    public void init(MapManager mapMgr){
        ArrayList<QuestTask> allQuestTasks = getAllQuestTasks();
        for( QuestTask questTask: allQuestTasks ) {

            if( questTask.isTaskComplete() ) continue;

            //We first want to make sure the task is available and is relevant to current location
            if (!isQuestTaskAvailable(questTask.getId())) continue;

            String taskLocation = questTask.getTargetLocation();
            if (     taskLocation == null ||
                     taskLocation.isEmpty() ||
                    !taskLocation.equalsIgnoreCase(mapMgr.getCurrentMapType().toString())) continue;

            switch (questTask.getQuestTaskType()) {
                case FETCH:
                    Array<Entity> questEntities = new Array<Entity>();
                    Array<Vector2> positions = mapMgr.getQuestItemSpawnPositions(questID, questTask.getId());
                    String taskConfig = questTask.getTargetType();
                    if( taskConfig == null || taskConfig.isEmpty() ) break;
                    EntityConfig config = Entity.getEntityConfig(taskConfig);

                    Array<Vector2> questItemPositions = ProfileManager.getInstance().getProperty(config.getEntityID(), Array.class);

                    if( questItemPositions == null ){
                        questItemPositions = new Array<Vector2>();
                        for( Vector2 position: positions ){
                            questItemPositions.add(position);
                            Entity entity = Entity.initEntity(config, position);
                            entity.getEntityConfig().setCurrentQuestID(questID);
                            questEntities.add(entity);
                        }
                    }else{
                        for( Vector2 questItemPosition: questItemPositions ){
                            Entity entity = Entity.initEntity(config, questItemPosition);
                            entity.getEntityConfig().setCurrentQuestID(questID);
                            questEntities.add(entity);
                        }
                    }

                    mapMgr.addMapQuestEntities(questEntities);
                    ProfileManager.getInstance().setProperty(config.getEntityID(), questItemPositions);
                    break;
                case KILL:
                    break;
                case DELIVERY:
                    break;
                case GUARD:
                    break;
                case ESCORT:
                    break;
                case RETURN:
                    break;
                case DISCOVER:
                    break;
            }
        }
    }

    public String toString(){
        return questTitle;
    }

    public String toJson(){
        Json json = new Json();
        return json.prettyPrint(this);
    }

}

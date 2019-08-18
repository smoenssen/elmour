package com.smoftware.elmour.quest;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.entities.EntityConfig.ConversationConfig;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.UI.huds.PlayerHUD;
import com.smoftware.elmour.maps.MapManager;

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
    public String yedNodeId;
    private String questTitle;
    private String questID;
    private String questGiver;
    private boolean isQuestComplete;
    private QuestStatus questStatus;
    private int chapter;
    private int goldReward;
    private int xpReward;
    private long timestamp;

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

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getTimestamp() { return timestamp; }

    public boolean isQuestComplete() { return questStatus == QuestStatus.COMPLETE; }

    public void setQuestComplete() {
        questStatus = QuestStatus.COMPLETE;

        // need to set all associated NPCs to post-quest dialog for this quest
        // this is so the NPC will display the correct dialog long after
        // the quest has been completed
        ArrayList<QuestTask> tasks = getAllQuestTasks();
        for( QuestTask task: tasks ){
            Entity entity;

            try {
                entity = EntityFactory.getInstance().getEntityByName(task.getTargetEntity());
            }
            catch (NullPointerException e) {
                continue;
            }

            Array<ConversationConfig> conversationConfigs = entity.getEntityConfig().getConversationConfigs();
            for (ConversationConfig conversationConfig : conversationConfigs) {
                if (conversationConfig.type == EntityConfig.ConversationType.POST_QUEST_DIALOG) {
                    PlayerHUD.saveLatestEntityConversationConfig(entity, conversationConfig);
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

    /*
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
*/

    public QuestTask getQuestTaskByID(String id){
        QuestTask questTask = questTasks.get(id);

        if (questTask != null) {
            return questTask;
        }
        else {
            // search all sub quest list tasks
            ArrayList<QuestTask> tasks = getAllQuestTasks();
            for (QuestTask task: tasks) {
                QuestList subQuestList = task.getSubQuestList();
                if (subQuestList != null) {
                    ArrayList<QuestGraph> questGraphs = subQuestList.getAllQuestGraphs();
                    for (QuestGraph questGraph : questGraphs) {
                        questTask = questGraph.getQuestTaskByID(id);
                        if (questTask != null) {
                            return questTask;
                        }
                    }
                }
            }
        }

        return null;
    }

    public void addDependency(QuestTaskDependency questTaskDependency){
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(questTaskDependency.getSourceId());
        if( list == null) return;
/*
        //todo: doesCycleExist has a bug so don't use it if we can get away without it.
        //      Have seen issues when parsing yEd graphs that are correct
        //      doesCycleExist assumes dependencies are added from top down

        //Will not add if creates cycles
        if( doesCycleExist(questTaskDependency) ){
            //System.out.println("Cycle exists! Not adding");
            return;
        }
*/
        list.add(questTaskDependency);
    }

    //todo: remove?
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

    public ArrayList<QuestTask> getVisibleTaskList(ArrayList<QuestTask> taskList) {
        // Set list to available or completed tasks up until and not including a spoiler that has not been completed
        // Also don't show any sub quests if they have dependencies that aren't completed
        // Note: List has already been sorted by dependency
        ArrayList<QuestTask> visibleTaskList = new ArrayList<>();

        for (QuestTask questTask : taskList) {
            if (questTask.isTaskComplete()) {
                visibleTaskList.add(questTask);
            }
            else if (isTaskVisible(questTask)) {
                visibleTaskList.add(questTask);
            }
        }

        return  visibleTaskList;
    }

    public boolean isTaskVisible(QuestTask questTask) {
        return (!questTask.isSpoiler() && isNoIncompleteSpoilerDownStream(questTask) && taskIsSubQuestTaskThatHasNoIncompleteDependency(questTask));
    }

    private boolean isNoIncompleteSpoilerDownStream(QuestTask questTask) {
        ArrayList<QuestTaskDependency> depList = questTaskDependencies.get(questTask.getId());

        if (depList == null) return true;

        for (QuestTaskDependency dep : depList) {
            QuestTask destQuestTask = getQuestTaskByID(dep.getDestinationId());
            if (destQuestTask.isSpoiler() && !destQuestTask.isTaskComplete()) {
                return false;
            }
            else if (destQuestTask.getSubQuestList() != null) {
                QuestList subQuestList = destQuestTask.getSubQuestList();

                QuestGraph subQuestGraph = subQuestList.getQuestByID(questTask.getId());

                if (subQuestGraph != null) {
                    ArrayList<QuestTask> subQuestTaskList = subQuestGraph.getAllQuestTasks();

                    for (QuestTask subQuestTask : subQuestTaskList) {
                        return isNoIncompleteSpoilerDownStream(subQuestTask);
                    }
                }
            }
            else {
                return isNoIncompleteSpoilerDownStream(destQuestTask);
            }
        }

        return  true;
    }

    private boolean taskIsSubQuestTaskThatHasNoIncompleteDependency(QuestTask questTask) {
        if (questTask.getSubQuestList() != null) {
            // this is a sub quest
            ArrayList<QuestTaskDependency> depList = questTaskDependencies.get(questTask.getId());

            if (depList == null) return true;

            for (QuestTaskDependency dep : depList) {
                QuestTask destQuestTask = getQuestTaskByID(dep.getDestinationId());
                if (!destQuestTask.isTaskComplete()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean doesTask1DependOnTask2(QuestTask task1, QuestTask task2) {
        // recursive check to see if there is a path from task1 to task2
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(task1.getId());

        if (list == null) return false;

        for (QuestTaskDependency dep : list) {
            if (dep.getDestinationId().equals(task2.getId())) {
                return true;
            }
            else if (doesTask1DependOnTask2(getQuestTaskByID(dep.getDestinationId()), task2)) {
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

    public boolean isQuestTaskComplete(String id) {
        QuestTask task = getQuestTaskByID(id);
        return task.isTaskComplete();
    }

    private QuestGraph getQuestGraph(String questID) {
        ArrayList<QuestTask> tasks = getAllQuestTasks();
        for (QuestTask task: tasks) {
            QuestList subQuestList = task.getSubQuestList();
            if (subQuestList != null) {
                ArrayList<QuestGraph> questGraphs = subQuestList.getAllQuestGraphs();
                for (QuestGraph questGraph : questGraphs) {
                    if (questGraph.getQuestID().equals(questID)) {
                        return questGraph;
                    }
                }
            }
        }

        return null;
    }

    public boolean isQuestTaskAvailable(String id){
        // A task is available if it has no dependencies
        QuestTask task = getQuestTaskByID(id);
        if( task == null) return false;
        ArrayList<QuestTaskDependency> list = questTaskDependencies.get(id);

        if (list != null) {
            return isQuestTaskAvailable(id, list);
        }
        else {
            // check for sub quest list dependencies
            String parentQuestId = task.getParentQuestId();
            if (parentQuestId != null) {
                QuestGraph subQuestGraph = getQuestGraph(parentQuestId);
                list = subQuestGraph.questTaskDependencies.get(id);
                return isQuestTaskAvailable(id, list);
            }
            else {
                return true;
            }
        }
    }

    public boolean isQuestTaskAvailable(String taskId, ArrayList<QuestTaskDependency> depList) {
        // A task is available if it has no dependencies
        if (depList != null) {
            for (QuestTaskDependency dep : depList) {
                QuestTask depTask = getQuestTaskByID(dep.getDestinationId());
                if (depTask == null || depTask.isTaskComplete()) continue;
                if (dep.getSourceId().equalsIgnoreCase(taskId)) {
                    return false;
                }
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
/*
            String taskLocation = questTask.getTargetLocation();
            if (taskLocation == null ||
                    taskLocation.isEmpty() ||
                    !taskLocation.equalsIgnoreCase(mapMgr.getCurrentMapType().toString())) continue;
*/
            switch (questTask.getQuestTaskType()) {
                case FETCH:
                    //todo: not used?
                    /*
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
                    */
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
/*
            String taskLocation = questTask.getTargetLocation();
            if (     taskLocation == null ||
                     taskLocation.isEmpty() ||
                    !taskLocation.equalsIgnoreCase(mapMgr.getCurrentMapType().toString())) continue;
*/
            switch (questTask.getQuestTaskType()) {
                case FETCH:
                    //todo: not used?
                    /*
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
                    */
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

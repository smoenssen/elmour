package com.smoftware.elmour.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by steve on 5/1/19.
 */

public class QuestList implements ProfileObserver {
    private static final String TAG = com.smoftware.elmour.quest.QuestList.class.getSimpleName();

    public enum QuestID {
        TeddyBear
    }

    private Hashtable<QuestID, QuestGraph> quests;

    public static final String QUEST_TASK_DELIMITER = ";";
    public static final String QUEST_DELIMITER = "::";
    public static final String TASK_DELIMITER = ",";

    //todo QUESTS
    public static final String TEDDY_BEAR_CONFIG = "RPGGame/maps/Game/Quests/TeddyBear.json";

    private Json json;
    //private Array<QuestGraph> quests;

    public QuestList() {
        json = new Json();
        quests = new Hashtable<>();
        ProfileManager.getInstance().addObserver(this);

        //todo QUESTS
        quests.put(QuestID.TeddyBear, getQuestGraph(TEDDY_BEAR_CONFIG));
    }

    public void questTaskStarted(String questID, String questTaskID) {
        QuestGraph questGraph = quests.get(QuestID.valueOf(questID));
        if (questGraph != null) {
            if (questGraph.isQuestTaskAvailable(questTaskID)) {
                questGraph.setQuestTaskStarted(questTaskID);
            }
        }
    }

    public void questTaskComplete(String questID, String questTaskID){
        QuestGraph questGraph = quests.get(QuestID.valueOf(questID));
        if (questGraph != null) {
            if (questGraph.isQuestTaskAvailable(questTaskID)) {
                questGraph.setQuestTaskComplete(questTaskID);
            }
        }
    }
/*
    public void loadAllQuestsForMap(MapManager mapMgr) {
        MapLayer mapSpawnsLayer =  mapMgr.getSpawnsLayer();

        if (mapSpawnsLayer == null) { return; }

        for( MapObject object : mapSpawnsLayer.getObjects()) {
            if (object != null) {
                String taskIDs = (String) object.getProperties().get("taskIDs");

                if (taskIDs != null) {
                    String[] questAndTaskIDs = taskIDs.split(QUEST_TASK_DELIMITER);

                    for (String questAndTaskID : questAndTaskIDs) {
                        String[] quests = questAndTaskID.split(QUEST_DELIMITER);

                        if (getQuestByID(quests[0]) == null) {

                        }
                    }
                }
            }
        }
    }
*/

    public QuestGraph getQuestGraph(String questConfigPath){
        if (questConfigPath.isEmpty() || !Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.debug(TAG, "Quest file does not exist!");
            return null;
        }

        return json.fromJson(QuestGraph.class, Gdx.files.internal(questConfigPath));
    }

    public boolean isQuestReadyForReturn(String questID){
        if (questID.isEmpty()) {
            Gdx.app.debug(TAG, "Quest ID not valid");
            return false;
        }

        QuestGraph graph = getQuestByID(questID);
        if (graph == null) return false;

        if (graph.updateQuestForReturn()) {
            graph.setQuestComplete();
        }
        else {
            return false;
        }
        return true;
    }

    public QuestGraph getQuestByID(String questID) {
        return quests.get(QuestID.valueOf(questID));
    }
/*
    //todo?
    public void initQuests(MapManager mapMgr){
        mapMgr.clearAllMapQuestEntities();

        //populate items if quests have them
        for( QuestGraph quest : quests){
            if( !quest.isQuestComplete() ){
                quest.init(mapMgr);
            }
        }
        ProfileManager.getInstance().setProperty("playerQuests", quests);
    }

    //todo?
    public void updateQuests(MapManager mapMgr){
        for( QuestGraph quest : quests){
            if( !quest.isQuestComplete() ){
                quest.update(mapMgr);
            }
        }
        ProfileManager.getInstance().setProperty("playerQuests", quests);
    }
*/

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        Array<QuestID> completedQuests;

        switch (event) {
            case PROFILE_LOADED:
                // get all quests that are in progress or complete from profile and update the internal list
                for (Map.Entry<QuestID, QuestGraph> entry : quests.entrySet()) {
                    QuestID questID = entry.getKey();

                    QuestGraph questGraphInProfile = ProfileManager.getInstance().getProperty(questID.toString(), QuestGraph.class);
                    if (questGraphInProfile != null) {
                        quests.put(questID, questGraphInProfile);
                    }
                }

                completedQuests = ProfileManager.getInstance().getProperty("CompletedQuests", Array.class);
                for (QuestID completedQuest : completedQuests) {
                    QuestGraph completedQuestGraph = quests.get(completedQuest);
                    completedQuestGraph.setQuestComplete();
                }

                break;
            case SAVING_PROFILE:
                // write all quests that are in progress or complete to profile
                for (Map.Entry<QuestID, QuestGraph> entry : quests.entrySet()) {
                    QuestID questID = entry.getKey();
                    QuestGraph questGraph = entry.getValue();

                    if (questGraph.getQuestStatus() == QuestGraph.QuestStatus.IN_PROGRESS) {
                        ProfileManager.getInstance().setProperty(questID.toString(), questGraph);
                    }
                    else if (questGraph.isQuestComplete()) {
                        completedQuests = ProfileManager.getInstance().getProperty("CompletedQuests", Array.class);
                        completedQuests.add(questID);
                        ProfileManager.getInstance().setProperty("CompletedQuests", completedQuests);
                    }
                }
                break;
        }
    }
}


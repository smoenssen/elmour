package com.smoftware.elmour.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.maps.MapManager;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.profile.ProfileObserver;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by steve on 5/1/19.
 */

public class QuestList implements ProfileObserver {
    private static final String TAG = com.smoftware.elmour.quest.QuestList.class.getSimpleName();

    public enum QuestID {
        TeddyBear,
        BuyDogs,
        DogsQuest,
        DogsQuest2,
        DogsQuest3
    }

    private Hashtable<QuestID, QuestGraph> quests;
    private Hashtable<String, Array<QuestDependency>> questDependencies;

    public static final String QUEST_TASK_DELIMITER = ";";
    public static final String QUEST_DELIMITER = "::";
    public static final String TASK_DELIMITER = ",";
    public static final String QUEST_GIVER = "QUEST_GIVER";

    //todo: Add QUESTS here
    public static final String TEDDY_BEAR_CONFIG = "RPGGame/maps/Game/Quests/TeddyBear.json";
    public static final String DOGS_QUEST_CONFIG = "RPGGame/maps/Game/Quests/DogsQuest.json";
    public static final String DOGS_QUEST2_CONFIG = "RPGGame/maps/Game/Quests/DogsQuest2.json";
    public static final String DOGS_QUEST3_CONFIG = "RPGGame/maps/Game/Quests/DogsQuest3.json";
    public static final String BUY_DOGS = "RPGGame/maps/Game/Quests/BuyDogs.json";

    private Json json;
    //private Array<QuestGraph> quests;

    public QuestList() {
        json = new Json();
        quests = new Hashtable<>();
        ProfileManager.getInstance().addObserver(this);
        questDependencies = json.fromJson(Hashtable.class, Gdx.files.internal("RPGGame/maps/Game/Quests/QuestDependencies.json"));

        //todo: Add QUESTS here
        quests.put(QuestID.TeddyBear, getQuestGraph(TEDDY_BEAR_CONFIG));
        quests.put(QuestID.DogsQuest, getQuestGraph(DOGS_QUEST_CONFIG));
        quests.put(QuestID.DogsQuest2, getQuestGraph(DOGS_QUEST2_CONFIG));
        quests.put(QuestID.DogsQuest3, getQuestGraph(DOGS_QUEST3_CONFIG));
        quests.put(QuestID.BuyDogs, getQuestGraph(BUY_DOGS));

        // TEST CODE ////////////////////////////////////////
        boolean available = isQuestAvailable("TeddyBear");
        available = isQuestAvailable("DogsQuest2");
        available = isQuestAvailable("DogsQuest3");
        QuestGraph myQuest = getQuestByID("BuyDogs");
        myQuest.setQuestComplete();
        available = isQuestAvailable("DogsQuest2");
        available = isQuestAvailable("DogsQuest3");
        available = isQuestAvailable("DogsQuest");
        myQuest = getQuestByID("DogsQuest2");
        myQuest.setQuestComplete();
        available = isQuestAvailable("DogsQuest");
        myQuest = getQuestByID("DogsQuest3");
        myQuest.setQuestComplete();
        available = isQuestAvailable("DogsQuest");
        ///////////////////////////////////////////////////////
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

        if (!graph.updateQuestForReturn()) {
            return false;
        }

        return true;
    }

    public boolean isQuestAvailable(String id){
        // A quest is available if it has no dependencies
        Array<QuestDependency> dependencies = questDependencies.get(id);

        if (dependencies == null) return true;

        for (QuestDependency questDependency : dependencies) {
            QuestGraph depQuest = getQuestByID(questDependency.getDestinationId());
            if (depQuest == null || depQuest.isQuestComplete()) continue;
            if (questDependency.getSourceId().equalsIgnoreCase(id)) {
                return false;
            }
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
                if (completedQuests != null) {
                    for (QuestID completedQuest : completedQuests) {
                        QuestGraph completedQuestGraph = quests.get(completedQuest);
                        completedQuestGraph.setQuestComplete();
                    }
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
                        if (completedQuests == null) {
                            completedQuests = new Array<>();
                        }
                        completedQuests.add(questID);
                        ProfileManager.getInstance().setProperty("CompletedQuests", completedQuests);
                        ProfileManager.getInstance().removeProperty(questID.toString());
                    }
                }
                break;
        }
    }
}


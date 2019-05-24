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
import java.util.Set;

/**
 * Created by steve on 5/1/19.
 */

public class QuestList implements ProfileObserver {
    private static final String TAG = com.smoftware.elmour.quest.QuestList.class.getSimpleName();

    private Hashtable<String, QuestGraph> quests;
    private Hashtable<String, String> questTitleMap;
    private Hashtable<String, Array<QuestDependency>> questDependencies;

    public static final String QUEST_TASK_DELIMITER = ";";
    public static final String QUEST_DELIMITER = "::";
    public static final String TASK_DELIMITER = ",";
    public static final String QUEST_GIVER = "QUEST_GIVER";

    private Json json;
    private boolean isSubQuestList = false;

    public QuestList() {
        create(true);
        this.isSubQuestList = false;
    }

    public QuestList(boolean isSubQuestList) {
        create(true);
        this.isSubQuestList = isSubQuestList;
    }

    public QuestList(Hashtable<String, QuestGraph> quests, boolean isSubQuestList) {
        create(false);
        this.quests = quests;
        this.isSubQuestList = isSubQuestList;
        buildQuestTitleMap();
    }

    private void create(boolean createQuestsHashtable) {
        if (createQuestsHashtable) quests = new Hashtable<>();
        json = new Json();
        questTitleMap = new Hashtable<>();
        ProfileManager.getInstance().addObserver(this);
        questDependencies = json.fromJson(Hashtable.class, Gdx.files.internal("RPGGame/maps/Game/Quests/QuestDependencies.json"));
    }

    private void buildQuestTitleMap() {
        Set<String> keys = quests.keySet();
        for (String id: keys) {
            QuestGraph questGraph = getQuestByID(id);
            questTitleMap.put(questGraph.getQuestTitle(), id);
        }
    }

    public void questTaskStarted(String questID, String questTaskID) {
        QuestGraph questGraph = quests.get(questID);
        if (questGraph != null) {
            if (questGraph.isQuestTaskAvailable(questTaskID)) {
                questGraph.setQuestTaskStarted(questTaskID);
            }
        }
    }

    public void questTaskComplete(String questID, String questTaskID){
        QuestGraph questGraph = quests.get(questID);
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
        return quests.get(questID);
    }

    public QuestGraph getQuestByQuestTitle(String title) {
        return getQuestByID(questTitleMap.get(title));
    }

    public ArrayList<String> getAllQuestIDs() {
        ArrayList<String> questIDs = new ArrayList<>();

        Set<String> keys = quests.keySet();
        for (String id: keys) {
            questIDs.add(id.toString());
        }

        return questIDs;
    }

    public void addQuest(QuestGraph questGraph) {
        quests.put(questGraph.getQuestID(), questGraph);
        questTitleMap.put(questGraph.getQuestTitle(), questGraph.getQuestID());
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
        Array<String> completedQuests;

        switch (event) {
            case PROFILE_LOADED:
                if (!isSubQuestList) {
                    // get all main quests that are in progress or complete from profile and update the internal list
                    for (Map.Entry<String, QuestGraph> entry : quests.entrySet()) {
                        String questID = entry.getKey();

                        QuestGraph questGraphInProfile = ProfileManager.getInstance().getProperty(questID, QuestGraph.class);
                        if (questGraphInProfile != null) {
                            quests.put(questID, questGraphInProfile);
                        }
                    }

                    completedQuests = ProfileManager.getInstance().getProperty("CompletedQuests", Array.class);
                    if (completedQuests != null) {
                        for (String completedQuest : completedQuests) {
                            QuestGraph completedQuestGraph = quests.get(completedQuest);
                            completedQuestGraph.setQuestComplete();
                        }
                    }
                }

                break;
            case SAVING_PROFILE:
                if (!isSubQuestList) {
                    // write all main quests that are in progress or complete to profile
                    for (Map.Entry<String, QuestGraph> entry : quests.entrySet()) {
                        String questID = entry.getKey();
                        QuestGraph questGraph = entry.getValue();

                        if (questGraph.getQuestStatus() == QuestGraph.QuestStatus.IN_PROGRESS) {
                            ProfileManager.getInstance().setProperty(questID, questGraph);
                        } else if (questGraph.isQuestComplete()) {
                            completedQuests = ProfileManager.getInstance().getProperty("CompletedQuests", Array.class);
                            if (completedQuests == null) {
                                completedQuests = new Array<>();
                            }
                            completedQuests.add(questID);
                            ProfileManager.getInstance().setProperty("CompletedQuests", completedQuests);
                            ProfileManager.getInstance().removeProperty(questID);
                        }
                    }
                }
                break;
        }
    }
}


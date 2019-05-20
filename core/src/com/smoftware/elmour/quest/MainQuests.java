package com.smoftware.elmour.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.Hashtable;

public class MainQuests {
    private static final String TAG = com.smoftware.elmour.quest.MainQuests.class.getSimpleName();

    private Json json;
    private Hashtable<QuestID, QuestGraph> quests;

    public MainQuests() {
        json = new Json();
        quests = new Hashtable<>();

        //todo: Add QUESTS here (3 of 3)
        quests.put(QuestID.TeddyBear, getQuestGraph(QuestConfig.TEDDY_BEAR_CONFIG));
        quests.put(QuestID.DogsQuest, getQuestGraph(QuestConfig.DOGS_QUEST_CONFIG));
    }

    private QuestGraph getQuestGraph(String questConfigPath){
        if (questConfigPath.isEmpty() || !Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.error(TAG, "Quest file does not exist!");
            return null;
        }

        return json.fromJson(QuestGraph.class, Gdx.files.internal(questConfigPath));
    }

    public Hashtable<QuestID, QuestGraph> getQuests() {
        return quests;
    }
}

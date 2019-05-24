package com.smoftware.elmour.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Quest {
    private static final String TAG = Quest.class.getSimpleName();

    static Json json = new Json();

    public static Map<String, String> quests;
    static {
        Map<String, String> map = new HashMap<>();

        map.put("CloningStep1", "RPGGame/maps/Game/Quests/CloningStep1.json");
        map.put("DogsQuest",    "RPGGame/maps/Game/Quests/DogsQuest.json");
        map.put("TeddyBear",    "RPGGame/maps/Game/Quests/TeddyBear.json");

        quests = Collections.unmodifiableMap(map);
    }

    public static Hashtable<String, QuestGraph> getAllQuestGraphs() {
        Hashtable<String, QuestGraph> questGraphs = new Hashtable<>();
        Set<String> keys = quests.keySet();
        for (String id: keys) {
            QuestGraph questGraph = getQuestGraph(id);
            if (questGraph != null)
                questGraphs.put(id, questGraph);
        }

        return questGraphs;
    }

    public static QuestGraph getQuestGraph(String questID){
        String questConfigPath = quests.get(questID);
        if (!Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.debug(TAG, "Quest file does not exist!");
            return null;
        }

        return json.fromJson(QuestGraph.class, Gdx.files.internal(questConfigPath));
    }
}

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

    public enum ID {
        CloningStep1,
        DogsQuest,
        GetWires,
        TeddyBear
    }

    static Json json = new Json();

    public static Map<ID, String> mainQuests;
    static {
        Map<ID, String> map = new HashMap<>();

        map.put(ID.DogsQuest, "RPGGame/maps/Game/Quests/DogsQuest.json");
        map.put(ID.TeddyBear, "RPGGame/maps/Game/Quests/TeddyBear.json");
        map.put(ID.CloningStep1, "RPGGame/maps/Game/Quests/CloningStep1.json");

        mainQuests = Collections.unmodifiableMap(map);
    }

    public static Map<ID, String> subQuests;
    static {
        Map<ID, String> map = new HashMap<>();

        map.put(ID.CloningStep1, "RPGGame/maps/Game/Quests/CloningStep1.json");
        map.put(ID.GetWires, "RPGGame/maps/Game/Quests/GetWires.json");

        subQuests = Collections.unmodifiableMap(map);
    }

    public static Hashtable<Quest.ID, QuestGraph> getAllMainQuestGraphs() {
        Hashtable<Quest.ID, QuestGraph> questGraphs = new Hashtable<>();
        Set<ID> keys = mainQuests.keySet();
        for (Quest.ID id: keys) {
            QuestGraph questGraph = getMainQuestGraph(id);
            if (questGraph != null)
                questGraphs.put(id, questGraph);
        }

        return questGraphs;
    }

    public static Hashtable<Quest.ID, QuestGraph> getAllSubQuestGraphs() {
        Hashtable<Quest.ID, QuestGraph> questGraphs = new Hashtable<>();
        Set<ID> keys = subQuests.keySet();
        for (Quest.ID id: keys) {
            QuestGraph questGraph = getMainQuestGraph(id);
            if (questGraph != null)
                questGraphs.put(id, questGraph);
        }

        return questGraphs;
    }

    public static QuestGraph getMainQuestGraph(ID questID){
        String questConfigPath = mainQuests.get(questID);
        if (!Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.debug(TAG, "Quest file does not exist!");
            return null;
        }

        return json.fromJson(QuestGraph.class, Gdx.files.internal(questConfigPath));
    }

    public static QuestGraph getSubQuestGraph(ID questID){
        String questConfigPath = subQuests.get(questID);
        if (!Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.debug(TAG, "Quest file does not exist!");
            return null;
        }

        return json.fromJson(QuestGraph.class, Gdx.files.internal(questConfigPath));
    }
}

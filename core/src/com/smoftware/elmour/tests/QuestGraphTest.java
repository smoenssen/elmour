package com.smoftware.elmour.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestTask;
import com.smoftware.elmour.quest.QuestTaskDependency;

import java.util.Hashtable;

public class QuestGraphTest {
    static Hashtable<String, QuestTask> _questTasks;
    static QuestGraph _graph;
    static Json _json;
    static String quit = "q";
    static String _input = "";


    public static void main (String[] arg) {
        _json = new Json();

        _questTasks = new Hashtable<String, QuestTask>();
        _graph = new QuestGraph();

        _graph.setQuestTitle("Get Teddy Bear");
        _graph.setQuestID("GetTeddyBear");
        _graph.setChapter(1);
        _graph.setQuestComplete(false);
        _graph.setGoldReward(20);
        _graph.setXpReward(5);

        QuestTask q1 = new QuestTask();
        q1.setId("ReturnTeddyBear");
        q1.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q1.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "MAP1");
        q1.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_TYPE, "OPHION");
        q1.setQuestType(QuestTask.QuestType.RETURN);
        q1.setTaskPhrase("Return teddy bear");

        QuestTask q2 = new QuestTask();
        q2.setId("FindTeddyBear");
        q2.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q2.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "COMPASS");
        q2.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_TYPE, "JUSTIN");
        q2.setQuestType(QuestTask.QuestType.FETCH);
        q2.setTaskPhrase("Find teddy bear");

        _questTasks.put(q1.getId(), q1);
        _questTasks.put(q2.getId(), q2);

        _graph.setTasks(_questTasks);

        QuestTaskDependency qDep1 = new QuestTaskDependency();
        qDep1.setSourceId(q1.getId());
        qDep1.setDestinationId(q2.getId());

        _graph.addDependency(qDep1);

        String fileData = _json.prettyPrint(_graph);

        if( Gdx.files.isLocalStorageAvailable() ) {
            FileHandle file = Gdx.files.local("RPGGame/maps/Game/Quests/GetTeddyBear.json");
            file.writeString(fileData, false);
        }

        _questTasks.clear();
        _graph.clear();

    }
}

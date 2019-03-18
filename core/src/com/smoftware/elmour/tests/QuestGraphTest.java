package com.smoftware.elmour.tests;

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

        _graph.setQuestTitle("Exploring Elmour");
        _graph.setQuestID("ExploreElmour");
        _graph.setQuestComplete(false);

        QuestTask q0 = new QuestTask();
        q0.setId("GoExplore");
        q0.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q0.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_TYPE, "RPGGame/maps/Game/Text/Dialog/GoExploreQuestReturn.json");
        q0.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "Elmour");
        q0.setQuestType(QuestTask.QuestType.RETURN);
        q0.setTaskPhrase("Go explore!");

        QuestTask q1 = new QuestTask();
        q1.setId("Armory");
        q1.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q1.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "Elmour");
        q1.setQuestType(QuestTask.QuestType.DISCOVER);
        q1.setTaskPhrase("Go to the Armory");

        QuestTask q2 = new QuestTask();
        q2.setId("Woodshop");
        q2.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q2.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "Elmour");
        q2.setQuestType(QuestTask.QuestType.DISCOVER);
        q2.setTaskPhrase("Go to the Wood Shop");

        QuestTask q3 = new QuestTask();
        q3.setId("Foodcourt");
        q3.setPropertyValue(QuestTask.QuestTaskPropertyType.IS_TASK_COMPLETE, "false");
        q3.setPropertyValue(QuestTask.QuestTaskPropertyType.TARGET_LOCATION, "Elmour");
        q3.setQuestType(QuestTask.QuestType.DISCOVER);
        q3.setTaskPhrase("Go to the Food Court");

        _questTasks.put(q0.getId(), q0);
        _questTasks.put(q1.getId(), q1);
        _questTasks.put(q2.getId(), q2);
        _questTasks.put(q3.getId(), q3);

        _graph.setTasks(_questTasks);

        QuestTaskDependency qDep1 = new QuestTaskDependency();
        qDep1.setSourceId(q0.getId());
        qDep1.setDestinationId(q1.getId());

        QuestTaskDependency qDep2 = new QuestTaskDependency();
        qDep2.setSourceId(q0.getId());
        qDep2.setDestinationId(q2.getId());

        QuestTaskDependency qDep3 = new QuestTaskDependency();
        qDep3.setSourceId(q0.getId());
        qDep3.setDestinationId(q3.getId());

        _graph.addDependency(qDep1);
        _graph.addDependency(qDep2);
        _graph.addDependency(qDep3);

        System.out.println(_json.prettyPrint(_graph));

        _questTasks.clear();
        _graph.clear();

    }
}

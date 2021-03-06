package com.smoftware.elmour.UI.huds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.UI.components.MyTextArea;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.quest.Quest;
import com.smoftware.elmour.quest.QuestGraph;
import com.smoftware.elmour.quest.QuestList;
import com.smoftware.elmour.quest.QuestTask;
import com.smoftware.elmour.quest.QuestTaskDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by steve on 5/14/19.
 */

public class QuestHUD implements Screen, QuestHudSubject {
    private static final String TAG = QuestHUD.class.getSimpleName();

    private final String NO_QUESTS_AVAILABLE = "No quests available";
    private final String SORT_BY_QUEST_NAME = " Sort by quest name";
    private final String SORT_BY_ORDER_OBTAINED = " Sort by order obtained";
    private final String SHOW_COMPLETED = " Show completed quests";
    private final String SHOW_NOT_COMPLETED = " Show uncompleted quests";
    private final String SHOW_ALL_QUESTS = " Show all quests";

    private final String SORT_QUEST_ORDER_PREFERENCE = "sortQuestOrderPreference";
    private final String SORT_ORDER_ASCENDING = "ascending";
    private final String SORT_ORDER_DESCENDING = "descending";
    private final String SORT_QUEST_BY_PREFERENCE = "sortQuestByPreference";
    private final String SHOW_QUEST_PREFERENCE = "showQuestPreference";

    private PlayerHUD playerHUD;
    private Stage stage;
    private Array<QuestHudObserver> observers;
    private static QuestList questList;
    private QuestList visibleQuestList;

    private Image downButton;
    private Image upButton;
    private MyTextArea sortPanelBackground;

    SelectBox<String> sortingSelectBox;
    SelectBox<String> showQuestsSelectBox;
    private Image showQuestsSelectBtn;
    private Image sortingSelectBtn;

    private String sortByPreference;
    private String showQuestsPreference;
    private String sortOrderPreference;

    private TextButton closeButton;

    private TextButton labelQuests;
    private Table questListTableView;
    private ScrollPane questScrollPaneList;
    private MyTextArea questBackground;
    private float questListWidth;
    private float questListHeight;
    float questListRowPadding = 2;

    private TextButton labelTasks;
    private Table taskTableView;
    private ScrollPane taskScrollPaneList;
    private MyTextArea taskBackground;
    private float taskListWidth;
    private float taskListHeight;

    private Table mainTable;

    private ImageTextButton selectedImageTextButton;

    private Hashtable<QuestTask, Boolean> showTaskHintMap;

    private boolean isDebug;
    private long touchTimer;

    public class QuestNameComparatorAscending implements Comparator<String> {
        @Override
        public int compare(String arg0, String arg1) {
            String questTitle0 = questList.getQuestByID(arg0).toString();
            String questTitle1 = questList.getQuestByID(arg1).toString();
            return questTitle0.compareTo(questTitle1);
        }
    }

    public class QuestNameComparatorDescending implements Comparator<String> {
        @Override
        public int compare(String arg0, String arg1) {
            String questTitle0 = questList.getQuestByID(arg0).toString();
            String questTitle1 = questList.getQuestByID(arg1).toString();
            return  questTitle0.compareTo(questTitle1) * -1;
        }
    }

    public class QuestTimestampComparatorAscending implements Comparator<QuestGraph> {
        @Override
        public int compare(QuestGraph arg0, QuestGraph arg1) {
            long timestamp0 = arg0.getTimestamp();
            long timestamp1 = arg1.getTimestamp();
            if (timestamp0 > timestamp1) {
                return 1;
            }
            else if (timestamp0 == timestamp1) {
                return 0;
            }
            else {
                return -1;
            }
        }
    }

    public class QuestTimestampComparatorDescending implements Comparator<QuestGraph> {
        @Override
        public int compare(QuestGraph arg0, QuestGraph arg1) {
            long timestamp0 = arg0.getTimestamp();
            long timestamp1 = arg1.getTimestamp();
            if (timestamp0 > timestamp1) {
                return -1;
            }
            else if (timestamp0 == timestamp1) {
                return 0;
            }
            else {
                return 1;
            }
        }
    }

    public class TaskDependencyComparator implements Comparator<QuestTask> {
        QuestGraph questGraph;

        public TaskDependencyComparator(QuestGraph questGraph) {
            this.questGraph = questGraph;
        }

        @Override
        public int compare(QuestTask arg0, QuestTask arg1) {
            if (questGraph.doesTask1DependOnTask2(arg0, arg1)) {
                return 1;
            }
            else if (questGraph.doesTask1DependOnTask2(arg1, arg0)) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    public QuestHUD(final PlayerHUD playerHUD, Stage stage) {
        this.playerHUD = playerHUD;
        this.stage = stage;
        observers = new Array<>();

        questList = new QuestList(Quest.getAllQuestGraphs(), false);
        visibleQuestList = new QuestList();

        showTaskHintMap = new Hashtable<>();

        isDebug = false;
        touchTimer = 0;

        float sortPanelHeight = 40;
        float labelHeight = 35;
        float closeButtonHeight = 35;
        float selectBoxHeight = 30;
        float margin = 15;

        if (ElmourGame.isAndroid()) {
            sortPanelHeight = 35;
            labelHeight = 30;
            closeButtonHeight = 30;
            selectBoxHeight = 25;
            margin = 10;
        }

        float btnWidth = selectBoxHeight/2;
        float btnHeight = selectBoxHeight/2;
        questListWidth = stage.getWidth()/3 + 2;
        taskListWidth = stage.getWidth() - questListWidth - (2 * margin);
        float listHeight = (stage.getHeight() - sortPanelHeight - labelHeight - (2 * margin));
        taskListHeight = listHeight;
        questListHeight = listHeight - closeButtonHeight + 4;
        float listWidth = questListWidth + taskListWidth;
        float listTopPadding = 6;
        float selectBoxPadding = (sortPanelHeight - selectBoxHeight) / 2;
        float selectBoxWidth = (listWidth - 3 * selectBoxPadding) / 2;



        /*
        **  SORT PANEL
        */
        WidgetGroup groupSortPanel = new WidgetGroup();

        showQuestsSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        showQuestsSelectBox.setItems(SHOW_NOT_COMPLETED, SHOW_COMPLETED, SHOW_ALL_QUESTS);
        showQuestsSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        sortingSelectBox = new SelectBox<>(Utility.ELMOUR_UI_SKIN);
        sortingSelectBox.setItems(SORT_BY_QUEST_NAME, SORT_BY_ORDER_OBTAINED);
        sortingSelectBtn = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button_Down.png"));

        WidgetGroup groupCompletedSelect = new WidgetGroup();
        groupCompletedSelect.addActor(showQuestsSelectBox);
        groupCompletedSelect.addActor(showQuestsSelectBtn);

        WidgetGroup groupSortingSelect = new WidgetGroup();
        groupSortingSelect.addActor(sortingSelectBox);
        groupSortingSelect.addActor(sortingSelectBtn);

        sortPanelBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        sortPanelBackground.setTouchable(Touchable.disabled);

        showQuestsSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupCompletedSelect.setPosition(selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

        showQuestsSelectBtn.setSize(btnWidth, btnHeight);
        showQuestsSelectBtn.setPosition(selectBoxWidth - showQuestsSelectBtn.getWidth() * 1.5f, (selectBoxHeight - showQuestsSelectBtn.getHeight())/2);

        sortingSelectBox.setSize(selectBoxWidth, selectBoxHeight);
        groupSortingSelect.setPosition(groupCompletedSelect.getX() + showQuestsSelectBox.getWidth() + selectBoxPadding, (sortPanelHeight - selectBoxHeight)/2);

        sortingSelectBtn.setSize(btnWidth, btnHeight);
        sortingSelectBtn.setPosition(selectBoxWidth - sortingSelectBtn.getWidth() * 1.5f, (selectBoxHeight - sortingSelectBtn.getHeight())/2);

        sortPanelBackground.setSize(questListWidth + taskListWidth, sortPanelHeight);

        groupSortPanel.addActor(sortPanelBackground);
        groupSortPanel.addActor(groupCompletedSelect);
        groupSortPanel.addActor(groupSortingSelect);

        /*
        **  QUEST LIST
        */
        WidgetGroup groupQuestLabel = new WidgetGroup();
        WidgetGroup groupQuests = new WidgetGroup();
        questListTableView = new Table();

        labelQuests = new TextButton("Quests", Utility.ELMOUR_UI_SKIN, "battle");
        questScrollPaneList = new ScrollPane(questListTableView);
        questBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        questBackground.setTouchable(Touchable.disabled);

        downButton = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Down_Button.png"));
        upButton = new Image(new Texture("RPGGame/maps/Game/Icons/Buttons/Up_Button.png"));
        upButton.setVisible(false);

        WidgetGroup groupDownBtn = new WidgetGroup();
        groupDownBtn.addActor(downButton);

        WidgetGroup groupUpBtn = new WidgetGroup();
        groupUpBtn.addActor(upButton);

        closeButton = new TextButton("Close", Utility.ELMOUR_UI_SKIN);

        labelQuests.setSize(questListWidth, labelHeight - 2);

        downButton.setSize(btnWidth, btnHeight);
        upButton.setSize(btnWidth, btnHeight);

        groupDownBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);
        groupUpBtn.setPosition((labelHeight - btnHeight)/2, (labelHeight - btnHeight)/2);

        groupQuestLabel.addActor(labelQuests);
        groupQuestLabel.addActor(groupDownBtn);
        groupQuestLabel.addActor(groupUpBtn);

        closeButton.setWidth(questListWidth);
        closeButton.setHeight(closeButtonHeight);

        questBackground.setSize(questListWidth, questListHeight);
        questBackground.setY(closeButtonHeight - 2);
        questScrollPaneList.setSize(questListWidth - 4, listHeight - sortPanelHeight - listTopPadding);
        questScrollPaneList.setX(2);
        questScrollPaneList.setY(2 + closeButtonHeight);

        groupQuests.addActor(questBackground);
        groupQuests.addActor(questScrollPaneList);
        groupQuests.addActor(closeButton);

        /*
        **  TASK LIST
        */
        WidgetGroup groupTasks = new WidgetGroup();
        taskTableView = new Table();
        //taskTableView.debugAll();
        labelTasks = new TextButton("Tasks", Utility.ELMOUR_UI_SKIN, "battle");
        labelTasks.setTouchable(Touchable.disabled);
        taskScrollPaneList = new ScrollPane(taskTableView);

        taskBackground = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battle");
        taskBackground.setTouchable(Touchable.disabled);

        taskBackground.setSize(taskListWidth, listHeight + 2);
        taskScrollPaneList.setSize(taskListWidth - 4, listHeight - listTopPadding);
        taskScrollPaneList.setX(2);
        taskScrollPaneList.setY(2);

        groupTasks.addActor(taskBackground);
        groupTasks.addActor(taskScrollPaneList);

        //taskTableView.debugAll();

        /*
        **  MAIN TABLE
        */
        // Note: padding -1 is so boxes can be overlapping to keep the border to 2 pixels
        mainTable = new Table();
        mainTable.row().width(stage.getWidth()).height(sortPanelHeight - 2);
        mainTable.add(groupSortPanel).pad(-1).colspan(2).width(listWidth);

        mainTable.row().width(stage.getWidth()).height(labelHeight - 2);
        mainTable.add(groupQuestLabel).pad(-1).width(questListWidth);
        mainTable.add(labelTasks).pad(-1).width(taskListWidth);

        mainTable.row().width(stage.getWidth()).height(stage.getHeight() - sortPanelHeight - (2 * margin) - labelHeight + 2);
        mainTable.add(groupQuests).pad(-1).width(questListWidth);
        mainTable.add(groupTasks).pad(-1).width(taskListWidth);

        mainTable.pack();
        mainTable.setPosition(margin, margin);
        //mainTable.debugAll();

        //show();

        labelQuests.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        if (sortOrderPreference.equals(SORT_ORDER_ASCENDING)) {
                                            downButton.setVisible(false);
                                            upButton.setVisible(true);
                                            setSortingOrder(SORT_ORDER_DESCENDING);
                                            sortQuestList();
                                        }
                                        else {
                                            upButton.setVisible(false);
                                            downButton.setVisible(true);
                                            setSortingOrder(SORT_ORDER_ASCENDING);
                                            sortQuestList();
                                        }
                                    }
                                }
        );

        downButton.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         downButton.setVisible(false);
                                         upButton.setVisible(true);
                                         setSortingOrder(SORT_ORDER_DESCENDING);
                                         sortQuestList();
                                     }
                                 }
        );

        upButton.addListener(new ClickListener() {
                                     @Override
                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                         return true;
                                     }

                                     @Override
                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                         upButton.setVisible(false);
                                         downButton.setVisible(true);
                                         setSortingOrder(SORT_ORDER_ASCENDING);
                                         sortQuestList();
                                     }
                                 }
        );

        showQuestsSelectBtn.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        showQuestsSelectBox.showList();
                                    }
                                }
        );

        sortingSelectBtn.addListener(new ClickListener() {
                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                               sortingSelectBox.showList();
                                           }
                                       }
        );

        sortingSelectBox.addListener(new ChangeListener() {
                                        @Override
                                        public void changed(ChangeEvent event, Actor actor) {
                                            if (sortOrderPreference == null) return;

                                            String selection = sortingSelectBox.getSelected();
                                            if (selection != null) {
                                                setSortByPreference(selection);
                                                sortQuestList();
                                            }
                                        }
                                     }
        );

        showQuestsSelectBox.addListener(new ChangeListener() {
                                        @Override
                                        public void changed(ChangeEvent event, Actor actor) {
                                            if (sortOrderPreference == null) return;

                                            String selection = showQuestsSelectBox.getSelected();
                                            if (selection != null) {
                                                setShowQuestsPreference(selection);
                                                sortQuestList();
                                            }
                                        }
                                    }
        );

        closeButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            hide();
                                        }
                                    }
        );

        questListTableView.addListener(new ClickListener() {
                                           @Override
                                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                               return true;
                                           }

                                           @Override
                                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                               if (selectedImageTextButton != null) {
                                                   // deselect previous button
                                                   QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());
                                                   if (questGraph.isQuestComplete()) {
                                                       selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("grayed_out", ImageTextButton.ImageTextButtonStyle.class));
                                                   }
                                                   else {
                                                       selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("default", ImageTextButton.ImageTextButtonStyle.class));
                                                   }
                                               }

                                               if (event.getTarget() instanceof Label) {
                                                   Label label = (Label) event.getTarget();
                                                   selectedImageTextButton = (ImageTextButton) label.getParent();
                                               } else if (event.getTarget() instanceof ImageTextButton) {
                                                   selectedImageTextButton = (ImageTextButton) event.getTarget();
                                               }

                                               QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());

                                               if (questGraph.isQuestComplete()) {
                                                   selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down_grayed_out", ImageTextButton.ImageTextButtonStyle.class));
                                               }
                                               else {
                                                   selectedImageTextButton.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", ImageTextButton.ImageTextButtonStyle.class));
                                               }

                                               setTaskListViewItems(questGraph.getAllQuestTasks(), questGraph.getQuestID());
                                           }
                                       }
        );

        taskScrollPaneList.addListener(new ClickListener() {
                                                     @Override
                                                     public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                                         touchTimer = Utility.getStartTime();
                                                         if (event.getTarget() instanceof Label) {
                                                             Label label = (Label) event.getTarget();
                                                             label.setStyle(Utility.ELMOUR_UI_SKIN.get("force_down", Label.LabelStyle.class));
                                                         }
                                                         return true;
                                                     }

                                                     @Override
                                                     public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                                         if (event.getTarget() instanceof Label) {
                                                             Label label = (Label) event.getTarget();

                                                             if (Utility.getElapsedTime(touchTimer) > 250) {
                                                                 // detected long press
                                                                 if (isDebug) {
                                                                     QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());
                                                                     QuestTask questTask = (QuestTask) label.getUserObject();
                                                                     if (questTask != null) {
                                                                         if (questTask.isTaskComplete()) {
                                                                             // set task and all parent tasks to inomplete
                                                                             setQuestTaskIncompleteDebug(questGraph, questTask);
                                                                         } else {
                                                                             // set task and all sub-tasks to complete
                                                                             setQuestTaskCompleteDebug(questGraph, questTask);
                                                                         }

                                                                         setTaskListViewItems(questGraph.getAllQuestTasks(), questGraph.getQuestID());
                                                                     }
                                                                 }
                                                                 else {
                                                                     QuestTask questTask = (QuestTask)label.getUserObject();
                                                                     if (questTask != null) {
                                                                         label.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", Label.LabelStyle.class));

                                                                         String hint = questTask.getHint();
                                                                         if (hint != null) {
                                                                             Boolean showHint = showTaskHintMap.get(questTask);
                                                                             if (showHint != null && showHint.equals(Boolean.TRUE)) {
                                                                                 showTaskHintMap.put(questTask, Boolean.FALSE);
                                                                             }
                                                                             else {
                                                                                 showTaskHintMap.put(questTask, Boolean.TRUE);
                                                                             }
                                                                             QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());
                                                                             setTaskListViewItems(questGraph.getAllQuestTasks(), questGraph.getQuestID());
                                                                         }
                                                                     }

                                                                     unhighlightTaskLabel(label);
                                                                 }
                                                             }
                                                             else {
                                                                 unhighlightTaskLabel(label);
                                                             }
                                                         }
                                                         else if (event.getTarget() instanceof Image) {
                                                            // collapse or expand if this is a sub-quest list of tasks
                                                             Image image = (Image) event.getTarget();
                                                             QuestTask questTask = (QuestTask)image.getUserObject();
                                                             if (questTask.getSubQuestList() != null) {
                                                                 questTask.setIsExpanded(!questTask.getIsExpanded());
                                                                 QuestGraph questGraph = questList.getQuestByQuestTitle(selectedImageTextButton.getText().toString());
                                                                 setTaskListViewItems(questGraph.getAllQuestTasks(), questGraph.getQuestID());
                                                             }
                                                         }
                                                     }
                                                 }
        );
    }

    private void unhighlightTaskLabel(Label label) {
        QuestTask questTask = (QuestTask)label.getUserObject();
        if (questTask != null) {
            if (!questTask.isTaskComplete()) {
                label.setStyle(Utility.ELMOUR_UI_SKIN.get("battle", Label.LabelStyle.class));
            }
            else {
                label.setStyle(Utility.ELMOUR_UI_SKIN.get("grayed_out", Label.LabelStyle.class));
            }
        }
    }
/*
    private void setQuestTaskIncompleteDebug(QuestGraph questGraph, QuestTask questTask) {
        // handle this task
        questTask.setTaskNotStarted();

        // handle sub-tasks
        QuestList subQuestList = questTask.getSubQuestList();
        if (subQuestList != null) {
            QuestGraph subQuestGraph = subQuestList.getQuestByID(questTask.getId());
            ArrayList<QuestTask> subQuestTaskList = subQuestGraph.getAllQuestTasks();
            for (QuestTask subTask : subQuestTaskList) {
                subTask.setTaskNotStarted();
            }
        }

        // handle parent task
        String parentQuestId = questTask.getParentQuestId();
        if (parentQuestId != null) {
            QuestTask parentQuestTask = questGraph.getQuestTaskByID(parentQuestId);
            if (parentQuestTask != null) {
                parentQuestTask.setTaskNotStarted();
            }
        }

        // handle main quest
        boolean allTasksAreNotStarted = true;
        ArrayList<QuestTask> tasks = questGraph.getAllQuestTasks();
        for (QuestTask task: tasks) {
            if (task.getQuestTaskStatus() == QuestTask.QuestTaskStatus.NOT_STARTED) {
                allTasksAreNotStarted = false;
                break;
            }
        }

        if (allTasksAreNotStarted) {
            playerHUD.unAcceptQuest(questGraph.getQuestID());
        }

        // handle any tasks dependent on this task
        Hashtable<String, ArrayList<QuestTaskDependency>> all = questGraph.getAllQuestTaskDependencies();
        Set<String> keys = all.keySet();
        for (String id: keys) {

            if (subQuestList != null) {
                QuestGraph subQuestGraph = subQuestList.getQuestByID(id);
                Hashtable<String, ArrayList<QuestTaskDependency>> allSub = subQuestGraph.getAllQuestTaskDependencies();
                ArrayList<QuestTaskDependency> depList = allSub.get(id);
                for (QuestTaskDependency dep : depList) {
                    if (dep.getDestinationId().equals(questTask.getId())) {
                        // recursive call to set tasks that depend on this incomplete
                        setQuestTaskIncompleteDebug(subQuestGraph, subQuestGraph.getQuestTaskByID(dep.getSourceId()));
                    }
                }
            }

            ArrayList<QuestTaskDependency> depList = all.get(id);
            for (QuestTaskDependency dep : depList) {
                if (dep.getDestinationId().equals(questTask.getId())) {
                    // recursive call to set tasks that depend on this incomplete
                    setQuestTaskIncompleteDebug(questGraph, questGraph.getQuestTaskByID(dep.getSourceId()));
                }
            }
        }
    }
*/

    private void setQuestTaskIncompleteDebug(QuestGraph questGraph, QuestTask questTask) {
        // handle this task
        questTask.setTaskNotStarted();

        // handle sub-tasks
        QuestList subQuestList = questTask.getSubQuestList();
        if (subQuestList != null) {
            QuestGraph subQuestGraph = subQuestList.getQuestByID(questTask.getId());
            ArrayList<QuestTask> subQuestTaskList = subQuestGraph.getAllQuestTasks();
            for (QuestTask subTask : subQuestTaskList) {
                subTask.setTaskNotStarted();
            }
        }

        // handle any tasks dependent on this task
        ArrayList<QuestTask> allMainQuestTasks = questGraph.getAllQuestTasks();
        for (QuestTask mainTask: allMainQuestTasks) {
            if (questGraph.doesTask1DependOnTask2(mainTask, questTask)) {
                mainTask.setTaskNotStarted();
            }

            QuestList subList = mainTask.getSubQuestList();
            if (subList != null) {
                ArrayList<QuestGraph> subQuestGraphs = subList.getAllQuestGraphs();
                for (QuestGraph subQuestGraph : subQuestGraphs) {
                    ArrayList<QuestTask> allSubQuestTasks = subQuestGraph.getAllQuestTasks();
                    for (QuestTask subTask: allSubQuestTasks) {
                        if (subQuestGraph.doesTask1DependOnTask2(subTask, questTask)) {
                            subTask.setTaskNotStarted();
                        }
                    }
                }
            }
        }

        // handle parent task
        String parentQuestId = questTask.getParentQuestId();
        if (parentQuestId != null) {
            QuestTask parentQuestTask = questGraph.getQuestTaskByID(parentQuestId);
            if (parentQuestTask != null) {
                parentQuestTask.setTaskNotStarted();
                // recursive call to set tasks that depend on this incomplete
                //setQuestTaskIncompleteDebug(questGraph, parentQuestTask);
            }
        }

        // handle main quest
        boolean allTasksAreNotStarted = true;
        ArrayList<QuestTask> tasks = questGraph.getAllQuestTasks();
        for (QuestTask task: tasks) {
            if (task.getQuestTaskStatus() == QuestTask.QuestTaskStatus.NOT_STARTED) {
                allTasksAreNotStarted = false;
                break;
            }
        }

        if (allTasksAreNotStarted) {
            playerHUD.unAcceptQuest(questGraph.getQuestID());
        }
    }
    
    private void setQuestTaskCompleteDebug(QuestGraph questGraph, QuestTask questTask) {
        questGraph.setQuestTaskComplete(questTask.getId());

        if (questGraph.doesQuestTaskHaveDependencies(questTask.getId())) {
            ArrayList<QuestTaskDependency> depList = questGraph.getQuestTaskDependencies(questTask.getId());
            for (QuestTaskDependency dep : depList) {
                // recursive call to set dependency task(s) complete
                setQuestTaskCompleteDebug(questGraph, questGraph.getQuestTaskByID(dep.getDestinationId()));
            }
        }

        QuestList subQuestList = questTask.getSubQuestList();
        if (subQuestList != null) {
            QuestGraph subQuestGraph = subQuestList.getQuestByID(questTask.getId());
            ArrayList<QuestTask> subQuestTaskList = subQuestGraph.getAllQuestTasks();
            for (QuestTask subTask : subQuestTaskList) {
                subTask.setTaskComplete();
            }
        }

        // set quest active if it isn't already
        if (questGraph.getQuestStatus() == QuestGraph.QuestStatus.NOT_STARTED) {
            playerHUD.acceptQuest(questGraph.getQuestID());
        }
    }

    private void setSortingOrder(String sortingOrder) {
        sortOrderPreference = sortingOrder;
        ProfileManager.getInstance().setProperty(SORT_QUEST_ORDER_PREFERENCE, sortOrderPreference);
    }

    private void setSortByPreference(String sortBy) {
        sortByPreference = sortBy;
        ProfileManager.getInstance().setProperty(SORT_QUEST_BY_PREFERENCE, sortByPreference);
    }

    private void setShowQuestsPreference(String showQuests) {
        showQuestsPreference = showQuests;
        ProfileManager.getInstance().setProperty(SHOW_QUEST_PREFERENCE, showQuestsPreference);
    }

    private void getSortingPreferences() {
        // SORT BY PREFERENCE
        sortByPreference = ProfileManager.getInstance().getProperty(SORT_QUEST_BY_PREFERENCE, String.class);
        if (sortByPreference == null) {
            sortByPreference = SORT_BY_QUEST_NAME;
            ProfileManager.getInstance().setProperty(SORT_QUEST_BY_PREFERENCE, sortByPreference);
        }

        sortingSelectBox.setSelected(sortByPreference);

        // SHOW QUESTS PREFERENCE
        showQuestsPreference = ProfileManager.getInstance().getProperty(SHOW_QUEST_PREFERENCE, String.class);
        if (showQuestsPreference == null) {
            showQuestsPreference = SHOW_ALL_QUESTS;
            ProfileManager.getInstance().setProperty(SHOW_QUEST_PREFERENCE, showQuestsPreference);
        }

        showQuestsSelectBox.setSelected(showQuestsPreference);

        // SORT ORDER PREFERENCE
        sortOrderPreference = ProfileManager.getInstance().getProperty(SORT_QUEST_ORDER_PREFERENCE, String.class);
        if (sortOrderPreference == null) {
            sortOrderPreference = SORT_ORDER_ASCENDING;
            ProfileManager.getInstance().setProperty(SORT_QUEST_ORDER_PREFERENCE, sortOrderPreference);
        }

        if (sortOrderPreference.equals(SORT_ORDER_ASCENDING)) {
            upButton.setVisible(false);
            downButton.setVisible(true);
        }
        else {
            upButton.setVisible(true);
            downButton.setVisible(false);
        }

        // Now sort the list!
        sortQuestList();
    }

    private void sortQuestList() {
        questListTableView.clearChildren();
        taskTableView.clearChildren();

        visibleQuestList.clear();

        if (isDebug) {
            setQuestListItems(questList.getAllQuestIDs());
        }
        else {
            setQuestListItems(questList.getAllQuestIDsInProgressOrComplete());
        }

        if (visibleQuestList.size() == 0) {
            ImageTextButton button = new ImageTextButton(NO_QUESTS_AVAILABLE, Utility.ELMOUR_UI_SKIN);
            button.align(Align.center);
            button.setTouchable(Touchable.disabled);
            questListTableView.add(button);
        }
        else {
            ArrayList<String> strings = new ArrayList<>();

            if (sortByPreference.equals(SORT_BY_QUEST_NAME)) {
                strings = visibleQuestList.getAllQuestIDs();
                if (sortOrderPreference.equals(SORT_ORDER_ASCENDING))
                    Collections.sort(strings, new QuestNameComparatorAscending());
                else
                    Collections.sort(strings, new QuestNameComparatorDescending());
            } else if (sortByPreference.equals(SORT_BY_ORDER_OBTAINED)) {
                ArrayList<QuestGraph> questGraphs = visibleQuestList.getAllQuestGraphs();
                if (sortOrderPreference.equals(SORT_ORDER_ASCENDING))
                    Collections.sort(questGraphs, new QuestTimestampComparatorAscending());
                else
                    Collections.sort(questGraphs, new QuestTimestampComparatorDescending());

                for (QuestGraph questGraph : questGraphs) {
                    strings.add(questGraph.getQuestID());
                }
            }

            ArrayList<String> questIDs = new ArrayList<>();

            if (showQuestsPreference.equals(SHOW_NOT_COMPLETED)) {
                for (String string : strings) {
                    if (string.trim().length() > 0) {
                        QuestGraph questGraph = questList.getQuestByID(string);
                        if (!questGraph.isQuestComplete()) {
                            questIDs.add(questGraph.getQuestID());
                        }
                    }
                }
            } else if (showQuestsPreference.equals(SHOW_COMPLETED)) {
                for (String string : strings) {
                    if (string.trim().length() > 0) {
                        QuestGraph questGraph = questList.getQuestByID(string);
                        if (questGraph.isQuestComplete()) {
                            questIDs.add(questGraph.getQuestID());
                        }
                    }
                }
            } else if (showQuestsPreference.equals(SHOW_ALL_QUESTS)) {
                for (String string : strings) {
                    if (string.trim().length() > 0) {
                        QuestGraph questGraph = questList.getQuestByID(string);
                        questIDs.add(questGraph.getQuestID());
                    }
                }
            }

            setQuestListItems(questIDs);

            questScrollPaneList.layout();
        }
    }

    private void addQuestListViewItem(QuestGraph questGraph) {
        Image noCheck = new Image(new Texture("graphics/noCheckmark.png"));
        Image check = new Image(new Texture("graphics/blackCheckmark.png"));
        check.setSize(16, 16);

        ImageTextButton button;


        if (questGraph.isQuestComplete()) {
            button = new ImageTextButton(questGraph.getQuestTitle(), Utility.ELMOUR_UI_SKIN, "grayed_out");
            button.align(Align.left);
            button.clearChildren();
            button.add(check).pad(5);
        }
        else {
            button = new ImageTextButton(questGraph.getQuestTitle(), Utility.ELMOUR_UI_SKIN);
            button.align(Align.left);
            button.clearChildren();
            button.add(noCheck).pad(5);
        }

        button.setUserObject(questGraph);
        button.add(button.getLabel());

        questListTableView.row().width(questListWidth);
        questListTableView.add(button).pad(questListRowPadding).width(questListWidth).align(Align.left);

        questListTableView.layout();
        questScrollPaneList.layout();

        visibleQuestList.addQuest(questGraph);
    }

    private void setQuestListItems(ArrayList<String> items) {
        questListTableView.clearChildren();
        visibleQuestList.clear();

        for (String questID : items) {
            QuestGraph questGraph = questList.getQuestByID(questID);
            addQuestListViewItem(questGraph);
        }

        // hack to fill a dummy row to get first row at top of scroll panel, otherwise table is vertically centered
        float usedSpace = 0;
        float rowHeight = 0;

        if (questListTableView.getRows() > 0) {
            rowHeight = questListTableView.getRowHeight(0);
            usedSpace = (questListTableView.getRows()) * (rowHeight);
        }

        float remainingSpace = questListTableView.getHeight() - usedSpace;

        if (remainingSpace > 0 && visibleQuestList.size() > 0) {
            questScrollPaneList.setScrollingDisabled(true, true);

            ImageTextButton dummy = new ImageTextButton("", Utility.ELMOUR_UI_SKIN);
            dummy.setTouchable(Touchable.disabled);
            questListTableView.row().width(questListWidth).height(remainingSpace);
            questListTableView.add(dummy).pad(questListRowPadding).width(questListWidth).align(Align.left);
        }
        else {
            questScrollPaneList.setScrollingDisabled(false, false);
        }
    }

    private void setTaskListViewItems(ArrayList<QuestTask> taskList, String questID) {
        taskTableView.clear();

        float usedSpace = 16;

        // Order tasks by dependencies
        Collections.sort(taskList, new TaskDependencyComparator(questList.getQuestByID(questID)));
        Collections.reverse(taskList);

        // Only show available or completed tasks
        QuestGraph questGraph = questList.getQuestByID(questID);

        if (isDebug) {
            taskList = questGraph.getAllQuestTasks();
        }
        else {
            taskList = questGraph.getVisibleTaskList(taskList);
        }

        for (QuestTask questTask : taskList) {
            Image bullet;
            Image subBullet;
            Label text;

            if (questTask.isTaskComplete() || questGraph.isQuestComplete()) {
                if (questTask.getSubQuestList() == null) {
                    bullet = new Image(new Texture("graphics/blackCheckmark.png"));
                }
                else if (questTask.getIsExpanded()) {
                    bullet = new Image(new Texture("graphics/blackCheckmark_minus.png"));
                }
                else {
                    bullet = new Image(new Texture("graphics/blackCheckmark_plus.png"));
                }
                text = new Label(getTaskText(questTask), Utility.ELMOUR_UI_SKIN, "grayed_out");
            } else {
                if (questTask.getSubQuestList() == null) {
                    bullet = new Image(new Texture("graphics/bullet.png"));
                }
                else if (questTask.getIsExpanded()) {
                    bullet = new Image(new Texture("graphics/bullet_minus.png"));
                }
                else {
                    bullet = new Image(new Texture("graphics/bullet_plus.png"));
                }
                text = new Label(getTaskText(questTask), Utility.ELMOUR_UI_SKIN, "battle");
            }

            bullet.setUserObject(questTask);
            text.setUserObject(questTask);

            text.setWrap(true);
            text.setAlignment(Align.topLeft);
            text.pack();

            int bulletTopMargin = 7;
            int bulletBottomMargin = 0;
            int bulletLeftMargin = 24;
            int bulletRightMargin = 2;
            int bulletMargin = bulletLeftMargin + bulletRightMargin;

            taskTableView.row().align(Align.top).expandY().fillY();
            taskTableView.add(bullet).align(Align.top).pad(bulletTopMargin, bulletLeftMargin, bulletBottomMargin, bulletRightMargin).width(24).height(16);
            taskTableView.add(text).pad(5).align(Align.left).width(taskListWidth - bullet.getWidth() - bulletMargin);

            usedSpace += text.getHeight();

            QuestList subQuestList = questTask.getSubQuestList();
            if (subQuestList != null && questTask.getIsExpanded()) {
                QuestGraph subQuestGraph = subQuestList.getQuestByID(questTask.getId());
                Table subTable = new Table();
                ArrayList<QuestTask> subQuestTaskList = subQuestGraph.getAllQuestTasks();

                // Order tasks by dependencies
                Collections.sort(subQuestTaskList, new TaskDependencyComparator(subQuestGraph));
                Collections.reverse(subQuestTaskList);

                if (isDebug) {
                    subQuestTaskList = subQuestGraph.getAllQuestTasks();
                }
                else {
                    // Only show available or completed tasks
                    subQuestTaskList = subQuestGraph.getVisibleTaskList(subQuestTaskList);
                }

                for (QuestTask subQuestTask : subQuestTaskList) {
                    if (subQuestTask.isTaskComplete()) {
                        subBullet = new Image(new Texture("graphics/blackCheckmark_subTask.png"));
                        text = new Label(getTaskText(subQuestTask), Utility.ELMOUR_UI_SKIN, "grayed_out");
                    } else {
                        subBullet = new Image(new Texture("graphics/bullet_subTask.png"));
                        text = new Label(getTaskText(subQuestTask), Utility.ELMOUR_UI_SKIN, "battle");
                    }

                    text.setUserObject(subQuestTask);

                    text.setWrap(true);
                    text.setAlignment(Align.topLeft);
                    text.pack();

                    int subBulletTopMargin = 7;
                    int subBulletBottomMargin = 5;
                    int subBulletLeftMargin = 2;
                    int subBulletRightMargin = 2;
                    int subBulletMargin = subBulletLeftMargin + subBulletRightMargin;

                    subTable.row().align(Align.top).width(taskListWidth - 40).expandY().fillY();
                    subTable.add(subBullet).align(Align.top).pad(subBulletTopMargin, subBulletLeftMargin, subBulletBottomMargin, subBulletRightMargin).width(16).height(16);
                    subTable.add(text).pad(5).align(Align.left).width(taskListWidth - bullet.getWidth() - bulletMargin - subBullet.getWidth() - subBulletMargin);
                    //subTable.debugAll();

                    usedSpace += text.getHeight();
                }

                Image blank = new Image(new Texture("graphics/blank_16x16.png"));
                taskTableView.row().align(Align.top).expandY().fillY();
                taskTableView.add(blank).align(Align.top).pad(7, 9, 0, 2).width(16).height(16);
                taskTableView.add(subTable).align(Align.left);

                usedSpace += 30;
            }
        }

        // hack to fill in dummy rows to get first row at top of scroll panel, otherwise table is vertically centered
        float remainingSpace = taskListHeight - usedSpace;

        if (remainingSpace > 3) {
            taskScrollPaneList.setScrollingDisabled(true, true);

            int numDummyRowsNeeded = (int) (remainingSpace / 23);//dummyText.getHeight());

            for (int i = 0; i < numDummyRowsNeeded; i++) {
                Image dummy = new Image();
                Label text = new Label("", Utility.ELMOUR_UI_SKIN, "grayed_out");

                taskTableView.row().align(Align.top).width(taskListWidth).height(text.getHeight()).expandY().fillY();
                taskTableView.add(dummy).align(Align.top).pad(7, 9, 0, 2).width(16).height(16);
                taskTableView.add(text).pad(5).width(taskListWidth - 30);
            }
        }
        else {
            taskScrollPaneList.setScrollingDisabled(true, false);
        }

        taskTableView.layout();
        taskScrollPaneList.layout();

        //taskTableView.debug();
    }

    private String getTaskText(QuestTask questTask) {
        String taskPhrase = questTask.getTaskPhrase();
        String [] sa = taskPhrase.split(";");
        String hint = "";

        if (questTask.getHint() != null) {
            Boolean showHint = showTaskHintMap.get(questTask);
            if (showHint != null && showHint.equals(Boolean.TRUE)) {
                hint = "\n(" + questTask.getHint() + ")";
            }
        }

        if (sa.length > 1) {
            if (questTask.isTaskComplete()) {
                return sa[1] + hint;
            }
            else {
                return sa[0] + hint;
            }
        }
        else {
            return taskPhrase + hint;
        }
    }

    public static QuestGraph getQuestByID(String questID) {
        return questList.getQuestByID(questID);
    }

    public boolean isQuestReadyForReturn(String questID) {
        return questList.isQuestReadyForReturn(questID);
    }

    public void setQuestTaskStarted(String questID, String questTaskID) {
        questList.questTaskStarted(questID, questTaskID);
    }

    public void setQuestTaskComplete(String questID, String questTaskID) {
        questList.questTaskComplete(questID, questTaskID);
    }

    public void showDebug() {
        isDebug = true;
        stage.addActor(mainTable);
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_SHOWN);

        setQuestListItems(questList.getAllQuestIDs());
        getSortingPreferences();
    }

    @Override
    public void show() {
        isDebug = false;
        stage.addActor(mainTable);
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_SHOWN);

        setQuestListItems(questList.getAllQuestIDsInProgressOrComplete());
        getSortingPreferences();
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        mainTable.remove();
        notify(QuestHudObserver.QuestHudEvent.QUEST_HUD_HIDDEN);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void addObserver(QuestHudObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(QuestHudObserver observer) {
        observers.removeValue(observer, true);
    }

    @Override
    public void notify(QuestHudObserver.QuestHudEvent event) {
        for(QuestHudObserver observer: observers){
            observer.onNotify(event);
        }
    }
}

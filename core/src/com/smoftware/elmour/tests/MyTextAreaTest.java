package com.smoftware.elmour.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.smoftware.elmour.UI.dialog.ConversationChoice;
import com.smoftware.elmour.UI.dialog.ConversationGraph;
import com.smoftware.elmour.UI.dialog.ConversationGraphObserver;
import com.smoftware.elmour.UI.huds.PlayerHUD;
import com.smoftware.elmour.main.Utility;

import java.util.ArrayList;

/**
 * Created by steve on 9/8/19.
 */

public class MyTextAreaTest implements ConversationGraphObserver {
    private static final String TAG = MyTextAreaTest.class.getSimpleName();

    private PlayerHUD playerHUD;
    private boolean running = false;
    private boolean paused = false;
    private MyTextAreaTest thisMyTextAreatTest;
    private Thread interactionThread;
    private boolean interactionThreadExited = false;

    private int numCycles = 0;
    private int numTimesToRunTest = 100;
    private float interactionDelayTime = 0.5f;
    private float restartTestDelayTime = 1.0f;

    public MyTextAreaTest(PlayerHUD playerHUD) {
        this.playerHUD = playerHUD;
        thisMyTextAreatTest = this;
    }

    public void run() {
        Gdx.app.log(TAG, "Starting test...");

        if (!runTest().isScheduled()) {
            Timer.schedule(runTest(), 0);
        }

    }

    private Timer.Task doConversationTimer() {
        return new Timer.Task() {
            @Override
            public void run() {
                playerHUD.doConversation();
            }
        };
    }

    private Timer.Task runTest() {
        return new Timer.Task() {
            @Override
            public void run() {
                restartTestDelayTime = MathUtils.random(0.1f, 2.0f);
                Gdx.app.log(TAG, "******** RUNNING TEST - CYCLE NUMBER " + (numCycles + 1) + " ********");
                Gdx.app.log(TAG, "restartTestDelayTime = " + restartTestDelayTime);
                playerHUD.loadConversationForCutScene("RPGGame/maps/Game/Text/Dialog/MyTextAreaTest.json", thisMyTextAreatTest);
                playerHUD.doConversation();

                if (!paused) {
                    startInteractionThread();
                }
                else {
                    paused = false;
                }
            }
        };
    }

    private void startInteractionThread() {
        running = true;

        Runnable r = new Runnable() {
            public void run() {
                while (running) {
                    if (!paused) {
                        interactionDelayTime = MathUtils.random(0.1f, 1.0f);
                        Gdx.app.log(TAG, "/////////////////////// interactionDelayTime = " + interactionDelayTime);
                    }

                    try {
                        Thread.sleep((int)(interactionDelayTime * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!paused) {
                        //playerHUD.doConversation();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                // On the main thread
                                playerHUD.doConversation();
                            }
                        });
                    }
                }

                interactionThreadExited = true;
                Gdx.app.log(TAG, "InteractionThread exiting...");
            }
        };

        interactionThread = new Thread(r);
        interactionThread.start();
    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event) {

    }

    @Override
    public void onNotify(ConversationGraph graph, ConversationCommandEvent event, String data) {
        Gdx.app.log(TAG, "Received notification " + event.toString());

        switch(event) {
            case EXIT_CHAT:
                numCycles++;
                Gdx.app.log(TAG, "******** END TEST - CYCLE NUMBER " + numCycles + " ********");
                if (numCycles > numTimesToRunTest) {
                    running = false;
                }
                else {
                    paused = true;

                    if (!runTest().isScheduled()) {
                        Timer.schedule(runTest(), restartTestDelayTime);
                    }
                }
        }
    }

    @Override
    public void onNotify(ConversationGraph graph, ArrayList<ConversationChoice> choices) {

    }

    @Override
    public void onNotify(String value, ConversationCommandEvent event) {

    }
}

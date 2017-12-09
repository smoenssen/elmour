package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.dialog.Conversation;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;

import java.util.ArrayList;

/**
 * Created by steve on 12/8/17.
 */

public class ConversationPopUp extends Window {
    private static final String TAG = ConversationPopUp.class.getSimpleName();

    private enum State {HIDDEN, LISTENING}

    class Dialog {
        public String name;
        public Array<String> lineStrings;
    }

    private ConversationGraph graph;
    private String currentEntityID;
    private Json json;
    private Dialog dialog;
    private String fullText;
    private boolean displayText = true;
    private String currentText;
    private MyTextArea textArea;
    private State state = State.HIDDEN;
    private boolean interactReceived = false;
    private boolean isReady = false;

    public ConversationPopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "default");

        dialog = new Dialog();

        json = new Json();
        graph = new ConversationGraph();
        hide();
    }

    public boolean isVisible() { return state != State.HIDDEN; }

    public boolean isReady() { return isReady; }

    public void interact() {

        //Gdx.app.log(TAG, "popup interact cur state = " + state.toString());

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
                    isReady = false;
                    this.setVisible(true);
                    state = State.LISTENING;
                    startInteractionThread();
                }
                break;
            case LISTENING:
                interactReceived = true;
                break;
        }

        //Gdx.app.log(TAG, "popup interact new state = " + state.toString());
    }

    public void hide() {
        this.reset();
        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();
        fullText = "";

        // set isReady to false so that full text doesn't flash on popup at first
        isReady = false;

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
        this.setVisible(false);
        state = State.HIDDEN;

        //Gdx.app.debug(TAG, "popup interact new state = " + state.toString());
    }

    private void setTextForUIThread(String text, boolean displayText) {
        currentText = text;
        this.displayText = displayText;
    }

    public void update() {
        // called from UI thread
        textArea.setText(currentText, displayText);
        //Gdx.app.log(TAG, "currentText = " + currentText);
    }

    public String getCurrentEntityID() {
        return currentEntityID;
    }

    public void loadConversation(EntityConfig entityConfig){
        String fullFilenamePath = entityConfig.getConversationConfigPath();
        this.getTitleLabel().setText("");

        if( fullFilenamePath.isEmpty() || !Gdx.files.internal(fullFilenamePath).exists() ){
            Gdx.app.debug(TAG, "Conversation file does not exist!");
            return;
        }

        currentEntityID = entityConfig.getEntityID();

        ConversationGraph graph = json.fromJson(ConversationGraph.class, Gdx.files.internal(fullFilenamePath));
        setConversationGraph(graph);
    }

    public void setConversationGraph(ConversationGraph graph){
        if( graph != null ) graph.removeAllObservers();
        this.graph = graph;
        populateConversationDialog(graph.getCurrentConversationID());
    }

    public ConversationGraph getCurrentConversationGraph(){
        return this.graph;
    }

    public void populateConversationDialog(String conversationID){
        Conversation conversation = graph.getConversationByID(conversationID);
        if( conversation == null ) return;
        graph.setCurrentConversation(conversationID);
        fullText = conversation.getDialog();
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                if (dialog.lineStrings == null || dialog.lineStrings.size == 0) {
                    // set full text so that the total number of lines can be figured out
                    setTextForUIThread(fullText, false);
                    isReady = true;

                    // wait up to 5 sec to make sure lines are populated
                    int numLines = textArea.getLines();
                    for (int q = 0; q < 100 && numLines == 0; q++) {
                        Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        numLines = textArea.getLines();
                        Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));
                    }

                    Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

                    dialog.lineStrings = textArea.getLineStrings();
                    Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", dialog.lineStrings.size));
                }

                boolean delay = true;

                // loop through lines
                for (int lineIdx = 0; lineIdx < dialog.lineStrings.size; lineIdx++) {
                    String line = dialog.lineStrings.get(lineIdx);
                    int len = line.length();
                    Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

                    // display line char by char for next two lines
                    String currentTextBeforeNextLine = currentVisibleText;
                    for (int i = 0; i < line.length(); i++) {

                        if (interactReceived || delay == false) {
                            Gdx.app.log(TAG, "interactReceived || delay == false");
                            interactReceived = false;
                            delay = false;
                            currentVisibleText = currentTextBeforeNextLine + line;
                            setTextForUIThread(currentVisibleText, true);
                            break;
                        }
                        else {
                            currentChar = line.charAt(i);
                            //Gdx.app.log(TAG, String.format("line.charAt(i) %c", line.charAt(i)));

                            currentVisibleText += currentChar;
                            setTextForUIThread(currentVisibleText, true);

                            // add EOL char to text so that pending text isn't displayed as chars are added
                            if (i == line.length() - 1) {
                                currentVisibleText += '\n';
                                setTextForUIThread(currentVisibleText, true);
                            }

                            // delay for each character
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    ArrayList<ConversationChoice> choices = graph.getCurrentChoices();
                    if( choices != null )
                        graph.notify(graph, choices);

                    if (state == State.HIDDEN)
                        // break out of loop and exit thread if we were hidden
                        break;
                    else
                        // go into listening mode
                        state = State.LISTENING;

                    if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == dialog.lineStrings.size - 1) {
                        // done populating current box so need to pause for next interaction
                        while (!interactReceived && state == State.LISTENING) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (lineIdx == dialog.lineStrings.size - 1) {
                            hide();
                            state = State.HIDDEN;
                            break;
                        }

                        // reset for next iteration
                        interactReceived = false;
                        delay = true;

                        if (state == State.HIDDEN)
                            break;

                        currentVisibleText = "";
                        setTextForUIThread(currentVisibleText, true);
                    }
                }

                // total reset
                currentText = "";
                displayText = false;
                interactReceived = false;
                Gdx.app.log(TAG, "Exiting InteractionThread");
            }
        };

        new Thread(r).start();
    }
}

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
import com.smoftware.elmour.dialog.ConversationGraphObserver;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;

/**
 * Created by steve on 12/8/17.
 */

public class ConversationPopUp extends Window {
    private static final String TAG = ConversationPopUp.class.getSimpleName();

    private enum State {HIDDEN, LISTENING, SHOWING_ECHO}

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
    private String currentCharacter;
    private State state = State.HIDDEN;
    private boolean interactReceived = false;
    private boolean isReady = false;
    private boolean isEcho = false;
    private boolean conversationIsActive = false;

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

    public void interact(boolean isEcho) {

        this.isEcho = isEcho;

        Gdx.app.log(TAG, "popup interact cur state = " + state.toString());
        Gdx.app.log(TAG, "interact   fullText = " + fullText);
        if (isEcho)
            Gdx.app .log(TAG, "isEcho");

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
                    isReady = false;
                    this.setVisible(true);
                    conversationIsActive = true;
                    state = State.LISTENING;
                    conversationIsActive = true;
                    startInteractionThread();
                }
                break;
            case LISTENING:
                interactReceived = true;
                break;
           /* case SHOWING_ECHO:
                state = State.LISTENING;
                dialog.lineStrings.clear();
                textArea.clear();
                setTextForUIThread(fullText, false);
                startInteractionThread();
                interactReceived = true;
                break;*/
        }

        Gdx.app.log(TAG, "popup interact new state = " + state.toString());
    }

    public void cleanupTextArea() {
        this.reset();
        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.disabled = true;
        textArea.layout();
        //fullText = "";

        // set isReady to false so that full text doesn't flash on popup at first
        isReady = false;

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
    }

    public void hide() {
        cleanupTextArea();
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
         //Gdx.app.log(TAG, currentText);
        textArea.setText(currentText, displayText);
        //Gdx.app.log(TAG, "currentText = " + currentText);
    }

    public String getCurrentEntityID() {
        return currentEntityID;
    }

    public void loadConversationFromConfig(EntityConfig entityConfig){
        String fullFilenamePath = entityConfig.getConversationConfigPath();
        currentEntityID = entityConfig.getEntityID();
        loadConversationFromJson(fullFilenamePath);
    }

    public void loadConversationFromJson(String jsonFilePath) {
        this.getTitleLabel().setText("");

        if( jsonFilePath.isEmpty() || !Gdx.files.internal(jsonFilePath).exists() ){
            Gdx.app.debug(TAG, "Conversation file does nstate = State.SHOWING_ECHOot exist!");
            return;
        }

        ConversationGraph graph = json.fromJson(ConversationGraph.class, Gdx.files.internal(jsonFilePath));
        setConversationGraph(graph);
    }

    public void setConversationGraph(ConversationGraph graph){
        if( graph != null ) graph.removeAllObservers();
        this.graph = graph;
        populateConversationDialogById(graph.getCurrentConversationID());
    }

    public ConversationGraph getCurrentConversationGraph(){
        return this.graph;
    }

    public void populateConversationDialogById(String conversationID){
        Conversation conversation = graph.getConversationByID(conversationID);
        if( conversation == null ) return;
        graph.setCurrentConversation(conversationID);
        fullText = conversation.getDialog();
        currentCharacter = conversation.getCharacter();
        Gdx.app.log(TAG, "populating fullText = " + fullText);

        if (fullText.equals("EXIT_CONVERSATION")) {
            graph.notify(graph, ConversationGraphObserver.ConversationCommandEvent.EXIT_CONVERSATION);
        }
    }

    public void populateConversationDialogByText(String text, String character){
        fullText = text;
        currentCharacter = character;
    }

    public void endConversation() {
        conversationIsActive = false;
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");

                while (conversationIsActive) {
                    char currentChar = ' ';
                    String currentVisibleText = "";

                    // need slight delay here so previous dialog can cleanup
                    pause(100);

                    if (dialog.lineStrings == null || dialog.lineStrings.size == 0) {
                        // set full text so that the total number of lines can be figured out
                        // send false so that text isn't displayed
                        Gdx.app.log(TAG, "setting text for UI thread = " + fullText);
                        setTextForUIThread(fullText, false);
                        isReady = true;

                        // wait up to 5 sec to make sure lines are populatedisEcho
                        int numLines = textArea.getLines();
                        for (int q = 0; q < 100 && numLines == 0; q++) {
                            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
                            pause(50);

                            numLines = textArea.getLines();
                            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));
                        }

                        //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

                        dialog.lineStrings = textArea.getLineStrings();
                        Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", dialog.lineStrings.size));
                    }

                    boolean delay = true;

                    graph.notify(currentCharacter, ConversationGraphObserver.ConversationCommandEvent.CHARACTER_NAME);

                    // loop through lines
                    for (int lineIdx = 0; lineIdx < dialog.lineStrings.size; lineIdx++) {
                        String line = dialog.lineStrings.get(lineIdx);
                        int len = line.length();
                        Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

                        // display line char by char for next two lines
                        String currentTextBeforeNextLine = currentVisibleText;
                        for (int i = 0; i < line.length(); i++) {

                            if (!isEcho && (interactReceived || delay == false)) {
                                Gdx.app.log(TAG, "interactReceived || delay == false");
                                if (interactReceived) {
                                    //isReady = true;
                                    Gdx.app.log(TAG, "interactReceived");
                                }
                                if (!delay)
                                    Gdx.app.log(TAG, "delay == false");
                                if (!isEcho)
                                    Gdx.app.log(TAG, "isEcho == false");

                                interactReceived = false;
                                delay = false;
                                currentVisibleText = currentTextBeforeNextLine + line;
                                setTextForUIThread(currentVisibleText, true);
                                Gdx.app.log(TAG, "currentVisibleText = " + currentVisibleText);
                                break;
                            } else {
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
                                pause(50);
                            }
                        }

                        if (state == State.HIDDEN)
                            // break out of loop and exit thread if we were hidden
                            break;
                        else
                            // go into listening mode
                            state = State.LISTENING;

                        // show choices now if this is the last line of the dialog
                        //todo: get character name
                        if (currentCharacter != "Me" && lineIdx == dialog.lineStrings.size - 1) {
                            ArrayList<ConversationChoice> choices = graph.getCurrentChoices();
                            if (choices != null) {
                                // remove any choices that are no longer available based on profile settings
                                for (int i = choices.size() - 1; i >= 0; i--) {
                                    ConversationChoice choice = choices.get(i);
                                    String commandEvent = choice.getConversationCommandEvent().toString();
                                    String profileSetting = ProfileManager.getInstance().getProperty(commandEvent, String.class);
                                    if (profileSetting != null) {
                                        choices.remove(i);
                                    }
                                }
                                graph.notify(graph, choices);
                            }
                        }

                        if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == dialog.lineStrings.size - 1) {
                            // done populating current box so need to pause for next interaction
                            while (!interactReceived && state == State.LISTENING) {
                                pause(100);
                            }

                            if (lineIdx == dialog.lineStrings.size - 1) {
                                //if (okToHide) {
                                //hide();
                                //state = State.HIDDEN;
                                //}
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

                    // if this is an echo, then keep the text displayed until next interaction
                    interactReceived = false;
                    while (isEcho && !interactReceived) {
                        pause(100);
                    }

                    // total reset
                    currentText = "";
                    displayText = false;
                    interactReceived = false;
                    dialog.lineStrings.clear();
                    cleanupTextArea();
                }

                Gdx.app.log(TAG, "Exiting InteractionThread");
                hide();
            }
        };

        new Thread(r).start();
    }

    void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

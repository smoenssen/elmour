package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 7/7/18.
 */

public class BattleTextArea extends Window {
    private static final String TAG = SignPopUp.class.getSimpleName();

    private enum State {HIDDEN, LISTENING}

    public Array<String> lineStrings;
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

    public BattleTextArea() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "default");

        lineStrings = new Array<>();
        hide();
    }

    public boolean isVisible() { return state != State.HIDDEN; }

    public boolean isReady() { return isReady; }

    public void populateText(String text) {
        fullText = text;
    }

    public void interact(boolean isEcho) {

        this.isEcho = isEcho;

        Gdx.app.log(TAG, "popup interact cur state = " + state.toString());
        Gdx.app.log(TAG, "interact   fullText = " + fullText);
        if (isEcho)
            Gdx.app .log(TAG, "isEcho");

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
                    Gdx.app.log(TAG, "setting isReady to false in interact");
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
        Gdx.app.log(TAG, "setting isReady to false in cleanupTextArea");
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

        conversationIsActive = false;

        //Gdx.app.debug(TAG, "popup interact new state = " + state.toString());
    }

    public boolean isListening() {
        if (state == State.LISTENING) {
            Gdx.app.log(TAG, "IS LISTENING");
            return true;
        }
        else {
            Gdx.app.log(TAG, "IS NOT LISTENING");
            return false;
        }
    }

    private void setTextForUIThread(String text, boolean displayText) {
        currentText = text;
        this.displayText = displayText;
    }

    public void update() {
        // called from UI thread
        // make sure there are no embedded line returns (borderline bug in MyTextArea)
        currentText = currentText.replace("\n", "");
        textArea.setText(currentText, displayText);
        //Gdx.app.log(TAG, "currentText = " + currentText);
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                if (lineStrings == null || lineStrings.size == 0) {
                    // set full text so that the total number of lines can be figured out
                    setTextForUIThread(fullText, false);
                    isReady = true;

                    // wait up to 5 sec to make sure lines are populated
                    int numLines = textArea.getLines();
                    for (int q = 0; q < 100 && numLines == 0; q++) {
                        Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        numLines = textArea.getLines();
                        Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));
                    }

                    Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

                    lineStrings = textArea.getLineStrings();
                    Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", lineStrings.size));
                }

                boolean delay = true;

                // loop through lines
                for (int lineIdx = 0; lineIdx < lineStrings.size; lineIdx++) {
                    String line = lineStrings.get(lineIdx);
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
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (state == State.HIDDEN)
                        // break out of loop and exit thread if we were hidden
                        break;
                    else
                        // go into listening mode
                        state = State.LISTENING;

                    if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == lineStrings.size - 1) {
                        // done populating current box so need to pause for next interaction
                        while (!interactReceived && state == State.LISTENING) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (lineIdx == lineStrings.size - 1) {
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

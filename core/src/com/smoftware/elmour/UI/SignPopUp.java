package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 9/16/17.
 */

public class SignPopUp extends Window {
    private static final String TAG = SignPopUp.class.getSimpleName();

    private enum State {HIDDEN, LISTENING}

    class SignPost {
        public String name;
        public Array<String> lineStrings;
    }

    private Array<SignPost> signPostArray;
    private SignPost currentSignPost;
    private String fullText;
    //private Array<String> lineStrings = null;
    private boolean displayText = true;
    private String currentText;
    private MyTextArea textArea;
    private State state = State.HIDDEN;
    private boolean interactReceived = false;
    private boolean isReady = false;

    public SignPopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        super("", Utility.ELMOUR_UI_SKIN, "default");
        /*textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
        */

        signPostArray = new Array<>();
        currentSignPost = new SignPost();
    }

    public boolean isVisible() { return state != State.HIDDEN; }

    public boolean isReady() { return isReady; }

    public void interact() {

        Gdx.app.log(TAG, "popup interact cur state = " + state.toString());

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
                    isReady = false;
                    this.setVisible(true);
                    state = State.LISTENING;
                    startInteractionThread();
                }
                else {
                    Gdx.app.log(TAG, "ERROR: popup text not initialized!");
                }
                break;
            case LISTENING:
                interactReceived = true;
                break;
        }

        Gdx.app.log(TAG, "popup interact new state = " + state.toString());
    }

    public void hide() {
        this.reset();
        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
        this.setVisible(false);
        state = State.HIDDEN;

        Gdx.app.debug(TAG, "popup interact new state = " + state.toString());
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

    public void setTextForInteraction(final Entity.Interaction interaction) {
        currentSignPost.name = "";

        if (currentSignPost.lineStrings != null)
            currentSignPost.lineStrings.clear();

        // see if this sign has been loaded yet
        boolean loaded = false;
        for (SignPost sign : signPostArray) {
            Gdx.app.log(TAG, "name = " + sign.name + ", interaction = " + interaction.toString());
            if (sign.name.equals(interaction.toString())) {
                currentSignPost.name = sign.name;
                currentSignPost.lineStrings = new Array<>(sign.lineStrings);
                loaded = true;
                break;
            }
        }

        if (!loaded) {
            currentSignPost.name = interaction.toString();
            FileHandle file = Gdx.files.internal("RPGGame/text/" + interaction.toString() + ".txt");
            fullText = file.readString();
            Gdx.app.log(TAG, "file text = " + fullText);
        }

        //if (lineStrings != null)
        //    lineStrings.clear();
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                if (currentSignPost.lineStrings == null || currentSignPost.lineStrings.size == 0) {
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

                    currentSignPost.lineStrings = textArea.getLineStrings();
                    Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", currentSignPost.lineStrings.size));

                    // add this sign post to the ones we've seen
                    signPostArray.add(currentSignPost);
                }

                boolean delay = true;

                // loop through lines
                for (int lineIdx = 0; lineIdx < currentSignPost.lineStrings.size; lineIdx++) {
                    String line = currentSignPost.lineStrings.get(lineIdx);
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

                    if (state == State.HIDDEN)
                        // break out of loop and exit thread if we were hidden
                        break;
                    else
                        // go into listening mode
                        state = State.LISTENING;

                    if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == currentSignPost.lineStrings.size - 1) {
                        // done populating current box so need to pause for next interaction
                        while (!interactReceived && state == State.LISTENING) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (lineIdx == currentSignPost.lineStrings.size - 1) {
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

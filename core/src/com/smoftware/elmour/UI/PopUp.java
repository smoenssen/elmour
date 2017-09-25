package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 9/16/17.
 */

public class PopUp extends Window {
    private static final String TAG = PopUp.class.getSimpleName();

    private enum State {HIDDEN, SHOWING, LISTENING}


    private String fullText;
    private boolean displayText = true;
    private String currentText;
    private MyTextArea textArea;
    private State state = State.HIDDEN;
    private boolean interactReceived = false;

    public PopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in ElmourGame class
        super("", Utility.ELMOUR_UI_SKIN, "default");
        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
    }

    public void interact() {

        Gdx.app.log(TAG, "popup interact cur state = " + state.toString());

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
                    this.setVisible(true);
                    state = State.SHOWING;
                    startInteractionThread();
                }
                else {
                    Gdx.app.log(TAG, "ERROR: popup text not initialized!");
                }
                break;
            case SHOWING:
                state = State.LISTENING;
                interactReceived = true;
                break;
            case LISTENING:
                interactReceived = true;
                break;
        }

        Gdx.app.log(TAG, "popup interact new state = " + state.toString());
    }

    public void hide() {
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
    }

    public void setTextForInteraction(final Entity.Interaction interaction) {
        FileHandle file = Gdx.files.internal("RPGGame/text/" + interaction.toString() + ".txt");
        fullText = file.readString();
        Gdx.app.log(TAG, "file text = " + fullText);
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                // set full text so that the total number of lines can be figured out
                setTextForUIThread(fullText, false);

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

                final Array<String> lines = textArea.getLineStrings();
                boolean delay = true;

                // loop through lines
                for (int lineIdx = 0; lineIdx < lines.size; lineIdx++) {
                    String line = lines.get(lineIdx);
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
                        break;
                    else
                        // go into listening mode
                        state = State.LISTENING;

                    if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == lines.size - 1) {
                        // done populating current box so need to pause for next interaction
                        while (!interactReceived && state == State.LISTENING) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (lineIdx == lines.size - 1) {
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

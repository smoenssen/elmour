package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 9/16/17.
 */

public class PopUp extends Window {
    private static final String TAG = PopUp.class.getSimpleName();

    private enum State { HIDDEN, SHOWING, LISTENING }

    String text;
    TextArea textArea;
    State state = State.HIDDEN;

    public PopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in ElmourGame class
        super("", Utility.ELMOUR_UI_SKIN, "default");
        textArea = new TextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
    }

    public void interact (Entity.Interaction interaction){
        switch (state) {
            case HIDDEN:
                loadTextForInteraction(interaction);
                this.setVisible(true);
                state = State.SHOWING;
                break;
            case SHOWING:
                break;
            case LISTENING:
                break;
        }
    }

    public void hide() {
        this.setVisible(false);
        state = State.HIDDEN;
    }

    private void loadTextForInteraction(Entity.Interaction interaction) {
        FileHandle file = Gdx.files.internal("RPGGame/text/" + interaction.toString() + ".txt");
        text = file.readString();
        Gdx.app.debug(TAG, "file text = " + text);

        Runnable r = new Runnable() {
            public void run() {
                String pendingText = "";
                String currentVisibleText = "";
                char currentChar = ' ';
                boolean isEndOfLine = false;
                boolean addSpaceToPendingText = false;
                int numLines = 0;

                for (int i = 0; i < text.length(); i++) {

                    if (addSpaceToPendingText) {
                        pendingText += ' ';
                        addSpaceToPendingText = false;
                    }

                    if (!isEndOfLine) {
                        currentChar = text.charAt(i);
                        pendingText += currentChar;
                        textArea.setText(pendingText);
                        numLines = textArea.getLines();
                    }
                    else {
                        // back up to previous character
                        isEndOfLine = false;
                        i--;
                    }

                    if (numLines > 2 || currentChar == '\n') {
                        // done populating current box
                        // wrapped to third line so need to pause for next request
                        textArea.setText(currentVisibleText);

                        //Gdx.app.log(TAG, "pending text = " + pendingText);
                        //Gdx.app.log(TAG, String.format("numLines = %d, currentaChar = %c ", numLines, currentChar));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // reset pending text pointer
                        if (currentChar != '\n') {
                            int k = pendingText.lastIndexOf(' ');
                            if (k == pendingText.length() - 1) {
                                // NOTE: Special case where the pending text has a space
                                // right where the length of the text pushes it into the next line
                                String temp = pendingText.substring(0, k);
                                k = temp.lastIndexOf(' ');
                                if (k > 0) {
                                    pendingText = temp.substring(k);
                                    //Gdx.app.log(TAG, "0.5. set pending text = " + pendingText);
                                }
                                addSpaceToPendingText = true;
                            }
                            else if (k >= 0) {
                                pendingText = pendingText.substring(k);
                                //Gdx.app.log(TAG, "1. set pending text = " + pendingText);
                            }
                        }
                        else if (currentChar == '\n' && numLines > 2) {
                            // this is the end of what we need to display for this box
                            // so set current text minus the EOL character
                            if (numLines > 3) {
                                // special case. set EOL to true so next iteration this gets flagged correctly
                                int k = pendingText.lastIndexOf(' ');
                                if (k >= 0) {
                                    pendingText = pendingText.substring(k);
                                    isEndOfLine = true;
                                    numLines = 3;
                                    //Gdx.app.log(TAG, "2. set pending text = " + pendingText);
                                    continue;
                                }
                            }
                            else
                            {
                                int k = pendingText.lastIndexOf('\n');
                                if (k >= 0) {
                                    currentVisibleText = pendingText.substring(0, pendingText.lastIndexOf('\n'));
                                    textArea.setText(currentVisibleText);
                                    //Gdx.app.log(TAG, "3. set currentVisibleText = " + currentVisibleText);
                                }
                                pendingText = "";
                                //Gdx.app.log(TAG, "3. set pending text = " + pendingText);
                                continue;
                            }
                        }
                        else {
                            pendingText = "";
                            //Gdx.app.log(TAG, "4. set pending text = " + pendingText);
                        }

                        currentVisibleText = "";
                        textArea.setText(currentVisibleText);
                    }
                    else {
                        currentVisibleText = pendingText;
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(r).start();
    }
}

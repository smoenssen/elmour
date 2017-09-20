package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
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

    private enum State { HIDDEN, SHOWING, LISTENING }

    String text;
    MyTextArea textArea;
    IntArray lineBreaks;
    State state = State.HIDDEN;

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

    public void interact (){

        Gdx.app.log(TAG, "popup interact cur state = " + state.toString());

        switch (state) {
            case HIDDEN:
                this.setVisible(true);
                state = State.SHOWING;
                break;
            case SHOWING:
                state = State.LISTENING;
                break;
            case LISTENING:
                break;
        }

        Gdx.app.log(TAG, "popup interact new state = " + state.toString());
    }

    public void hide() {
        this.setVisible(false);
        state = State.HIDDEN;

        //Gdx.app.debug(TAG, "popup interact new state = " + state.toString());
    }

    /** SRM - returns an array of strings **/
    public Array<String> getLineStrings() {
        Array<String> strings = new Array<String>();
        String currString = new String();

        lineBreaks = new IntArray(textArea.getLines());

        int currLineBreak = 1;
        for (int i = 0; i < text.length(); i++) {
            if (currLineBreak < lineBreaks.size) {
                if (i > 0 && i == lineBreaks.get(currLineBreak)) {
                    // new line
                    strings.add(currString);
                    Gdx.app.log("tag", String.format("adding currString = %s", currString));
                    currString = "";
                    currLineBreak += 2;
                }
            }

            currString += text.charAt(i);
        }

        if (currString.length() > 0)
            strings.add(currString);

        return strings;
    }

    public void loadTextForInteraction(Entity.Interaction interaction) {
        FileHandle file = Gdx.files.internal("RPGGame/text/" + interaction.toString() + ".txt");
        text = file.readString();
        Gdx.app.log(TAG, "file text = " + text);

        String pendingText = "";
        boolean isEndOfLine = false;
        boolean addSpaceToPendingText = false;
        //int numLines = 0;

        textArea.setText(text, true);
        final Array<String> lines = textArea.getLineStrings();
        textArea.setText("", true);

        Runnable r = new Runnable() {
            public void run() {
                char currentChar = ' ';
                String currentVisibleText = "";

                for (int lineIdx = 0; lineIdx < lines.size; lineIdx++) {
                    String line = lines.get(lineIdx);

                    int len = line.length();
                    Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

                    for (int i = 0; i < line.length(); i++) {

                        currentChar = line.charAt(i);
                        Gdx.app.log(TAG, String.format("line.charAt(i) %c", line.charAt(i)));

                        if (currentChar != '\n') {
                            currentVisibleText += currentChar;
                            textArea.setText(currentVisibleText, true);
                        }

                        if ((lineIdx != 0 && lineIdx % 2 == 0) || currentChar == '\n') {
                            // done populating current box
                            // wrapped to third line so need to pause for next request
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            currentVisibleText = "";
                        }

                        if (i == line.length() - 1) {
                            currentVisibleText += '\n';
                            textArea.setText(currentVisibleText, true);
                        }

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
/*
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
                */
            }
        };

        new Thread(r).start();
    }
}

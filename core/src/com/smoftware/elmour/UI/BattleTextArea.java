package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 7/7/18.
 */

public class BattleTextArea extends Window {
    private static final String TAG = BattleTextArea.class.getSimpleName();

    private enum State {HIDDEN, VISIBLE}

    public Array<String> lineStrings;
    private String fullText;
    private boolean displayText = true;
    private String currentText;
    private MyTextArea textArea;
    private State state = State.HIDDEN;
    private boolean isReady = false;

    public BattleTextArea() {
        //Notes:
        //font is set in the Utility class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "battle");

        lineStrings = new Array<>();
        hide();
    }

    public boolean isVisible() { return state != State.HIDDEN; }

    public boolean isReady() { return isReady; }

    public void populateText(String text) {
        fullText = text;
        startTextScrollThread();
    }

    public void show() {
        this.setVisible(true);
        state = State.VISIBLE;
        //startTextScrollThread();
    }

    public void cleanupTextArea() {
        lineStrings.clear();
        this.reset();
        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN, "battletext");
        textArea.disabled = true;
        textArea.layout();
        //fullText = "";

        // set isReady to false so that full text doesn't flash on battle text at first
        Gdx.app.log(TAG, "setting isReady to false in cleanupTextArea");
        isReady = false;

        //layout
        this.add();
        this.defaults().expand().fill().pad(10, 10, 0, 10);
        this.add(textArea);
    }

    public void hide() {
        cleanupTextArea();
        this.setVisible(false);

        state = State.HIDDEN;
        //Gdx.app.debug(TAG, "battle text interact new state = " + state.toString());
    }

    private void setTextForUIThread(String text, boolean displayText) {
        currentText = text;
        this.displayText = displayText;
    }

    public void update() {
        // called from UI thread
        // make sure there are no embedded line returns (borderline bug in MyTextArea)
        if (textArea != null && currentText != null) {
            currentText = currentText.replace("\n", "");
            textArea.setText(currentText, displayText);
            //Gdx.app.log(TAG, "currentText = " + currentText);
        }
    }

    private void startTextScrollThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting TextScrollThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                if (lineStrings == null || lineStrings.size == 0) {
                    // set full text so that the total number of lines can be figured out
                    setTextForUIThread(fullText, false);
                    isReady = true;

                    // wait up to 5 sec to make sure lines are populated
                    int numLines = textArea.getLines();
                    for (int q = 0; q < 100 && numLines == 0; q++) {
                        //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        numLines = textArea.getLines();
                        //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));
                        //Gdx.app.log(TAG, "fulltext = " + fullText);
                    }

                    Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

                    try {
                        lineStrings = textArea.getLineStrings();
                        Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", lineStrings.size));
                    }
                    catch (StringIndexOutOfBoundsException e) {
                        Gdx.app.log(TAG, "StringIndexOutOfBoundsException caught!!!");
                    }
                }

                Gdx.app.log(TAG, "fulltext = " + fullText);

                // loop through lines
                for (int lineIdx = 0; lineIdx < lineStrings.size; lineIdx++) {
                    String line = lineStrings.get(lineIdx);
                    int len = line.length();
                    Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

                    // display line char by char
                    for (int i = 0; i < line.length(); i++) {
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
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (state == State.HIDDEN)
                        // break out of loop and exit thread if we are hidden
                        break;
                }

                Gdx.app.log(TAG, "Exiting TextScrollThread");
            }
        };

        new Thread(r).start();
    }
}

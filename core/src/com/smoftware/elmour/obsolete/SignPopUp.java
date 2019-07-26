package com.smoftware.elmour.obsolete;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.Entity;
import com.smoftware.elmour.UI.components.MyTextArea;
import com.smoftware.elmour.UI.dialog.PopUpObserver;
import com.smoftware.elmour.UI.dialog.PopUpSubject;
import com.smoftware.elmour.main.Utility;

/**
 * Created by steve on 9/16/17.
 */

public class SignPopUp extends Window implements PopUpSubject {
    private static final String TAG = SignPopUp.class.getSimpleName();

    private enum State {HIDDEN, LISTENING}

    class SignPost {
        public String name;
        public Array<String> lineStrings;
    }

    private Array<PopUpObserver> observers;
    private Array<SignPost> signPostArray;
    private SignPost currentSignPost;
    private String fullText;
    private boolean displayText = true;
    private String currentText;
    private MyTextArea textArea;
    private State state = State.HIDDEN;
    private boolean interactReceived = false;
    private boolean isReady = false;
    private long setVisibleDelay = 0;

    public SignPopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "default");

        observers = new Array<PopUpObserver>();
        signPostArray = new Array<>();
        currentSignPost = new SignPost();
    }

    public boolean isVisible() { return state != State.HIDDEN; }

    public boolean isReady() { return isReady; }

    @Override
    public void addObserver(PopUpObserver popUpObserver) {
        observers.add(popUpObserver);
    }

    @Override
    public void removeObserver(PopUpObserver popUpObserver) {
        observers.removeValue(popUpObserver, true);
    }

    @Override
    public void removeAllObservers() {
        for(PopUpObserver observer: observers){
            observers.removeValue(observer, true);
        }
    }

    @Override
    public void notify(int value, PopUpObserver.PopUpEvent event) {
        for(PopUpObserver observer: observers){
            observer.onNotify(value, event);
        }
    }

    public void interact() {

        //Gdx.app.log(TAG, "popup interact cur state = " + state.toString());

        switch (state) {
            case HIDDEN:
                if (fullText != "") {
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
        textArea.setDisabled(true);
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
            FileHandle file = Gdx.files.internal("RPGGame/maps/Game/Text/Signs/" + interaction.toString() + ".txt");
            fullText = file.readString();
            Gdx.app.log(TAG, "file text = " + fullText);

            if (fullText.contains(";")) {
                // need to parse out delay time from beginning of file
                String[] sa = fullText.split(";");
                setVisibleDelay = Integer.parseInt(sa[0]);
                fullText = sa[1];
            }
            else {
                setVisibleDelay = 0;
            }
        }
    }

    private void startInteractionThread() {
        Runnable r = new Runnable() {
            public void run() {
                Gdx.app.log(TAG, "Starting InteractionThread...");
                char currentChar = ' ';
                String currentVisibleText = "";

                try { Thread.sleep(setVisibleDelay); } catch (InterruptedException e) { e.printStackTrace(); }

                isReady = false;
                state = State.LISTENING;

                if (currentSignPost.lineStrings == null || currentSignPost.lineStrings.size == 0) {
                    // set full text so that the total number of lines can be figured out
                    // send false so that text isn't displayed

                    // replace \r\n chars with \n so number of lines is figured out correctly
                    // otherwise, a new line is created for each \r character
                    // later they will be removed if necessary while text is being processed
                    fullText = fullText.replace("\r\n", "\n");

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
                    setVisible(true);

                    currentSignPost.lineStrings = textArea.getLineStrings();
                    //Gdx.app.log(TAG, String.format("textArea.getLineStrings() returned %d strings", currentSignPost.lineStrings.size));

                    // add this sign post to the ones we've seen
                    signPostArray.add(currentSignPost);
                }

                boolean delay = true;

                // loop through lines
                for (int lineIdx = 0; lineIdx < currentSignPost.lineStrings.size; lineIdx++) {
                    String line = currentSignPost.lineStrings.get(lineIdx);
                    int len = line.length();
                    Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

                    String currentTextBeforeNextLine = currentVisibleText;
                    for (int i = 0; i < line.length(); i++) {

                        if (interactReceived || delay == false) {
                            Gdx.app.log(TAG, "interactReceived || delay == false");
                            interactReceived = false;
                            delay = false;
                            /*
                            if (currentTextBeforeNextLine.length() > 1 && currentTextBeforeNextLine.charAt(currentTextBeforeNextLine.length() - 1) == '\n') {
                                // chop off last \n
                                // this is to take care of issue where if there is a \n at end of line and
                                // an interaction is received, then a double \n was occurring.
                                currentTextBeforeNextLine = currentTextBeforeNextLine.substring( 0, currentTextBeforeNextLine.length() - 2);
                            }
                            */

                            currentVisibleText = currentTextBeforeNextLine + line;
                            currentVisibleText = currentVisibleText.trim();
                            setTextForUIThread(currentVisibleText, true);
                            Gdx.app.log(TAG, "currentVisibleText = " + currentVisibleText);
                            break;
                        }
                        else {
                            currentChar = line.charAt(i);
                            //Gdx.app.log(TAG, String.format("line.charAt(i) %c", line.charAt(i)));

                            // ignore new line chars since they are not needed here, they are only needed if
                            // interaction is received prematurely to complete the 2 line text area
                            if (currentChar != '\n') {
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
                SignPopUp.this.notify(0, PopUpObserver.PopUpEvent.INTERACTION_THREAD_EXIT);
            }
        };

        new Thread(r).start();
    }
}

package com.smoftware.elmour.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.UI.MyTextArea;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;

/**
 * Created by steve on 12/8/17.
 */

public class ChoicePopUp extends Window {
    private static final String TAG = ChoicePopUp.class.getSimpleName();

    private MyTextArea textArea;
    private ConversationGraph graph;
    ConversationChoice choice;
    private Vector2 screenPos;
    private Vector2 localPos;
    private boolean isVisible;

    public ChoicePopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        super("", Utility.ELMOUR_UI_SKIN, "default");

        isVisible = false;

        screenPos = new Vector2();
        localPos = new Vector2();

        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.setDisabled(true);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);

        this.textArea.addListener(new ClickListener() {
              @Override
              public void clicked (InputEvent event, float x, float y) {
                  //graph.notify(graph, choice.getConversationCommandEvent());
                  //graph.notify(choice.getDestinationId(), ConversationGraphObserver.ConversationCommandEvent.NEXT_CONVERSATION_ID);
                  //graph.notify(choice.getChoicePhrase(), ConversationGraphObserver.ConversationCommandEvent.PLAYER_RESPONSE);
              }
          }
        );
    }

    public void update() {
        if(Gdx.input.justTouched()) {
            if (choice != null && isVisible) {
                // Get the touch point in screen coordinates.
                screenPos.set(Gdx.input.getX(), Gdx.input.getY());

                // Convert the touch point into local coordinates
                localPos.set(screenPos);
                localPos = getParent().screenToLocalCoordinates(localPos);

                Rectangle textAreaRect = new Rectangle(this.getX(), this.getY(), textArea.getWidth(), textArea.getHeight());

                // See if this window was touched
                if (Utility.pointInRectangle(textAreaRect, localPos.x, localPos.y)) {
                    interact();

                    // save any SET_ commands as profile properties for persistence
                    if (choice.getConversationCommandEvent().toString().startsWith("SET_")) {
                        ProfileManager.getInstance().setProperty(choice.getConversationCommandEvent().toString(), "true");
                    }
                }
            }
        }
    }

    public void show() {
        this.setVisible(true);
        isVisible = true;
    }

    public void hide() {
        this.setVisible(false);
        isVisible = false;
    }

    public void interact() {
        if (choice != null) {
            graph.notify(graph, choice.getConversationCommandEvent());
            graph.notify(choice.getDestinationId(), ConversationGraphObserver.ConversationCommandEvent.NEXT_CONVERSATION_ID);
            graph.notify(choice.getChoicePhrase(), ConversationGraphObserver.ConversationCommandEvent.PLAYER_RESPONSE);
        }
    }

    public void setChoice(ConversationChoice choice) {
        this.choice = choice;

        // set full text so that the total number of lines can be figured out
        // send displayText = false so that text isn't displayed
        textArea.setText(this.choice.getChoicePhrase(), false);

        Array<String> lines = calculateLineStrings(this.choice.getChoicePhrase());

        String fullText = "";
        for (String line : lines) {
            fullText += line.replace('|', '\'');
        }

        textArea.setText(fullText, true);
        this.choice.setChoicePhrase(fullText);
    }

    private Array<String> calculateLineStrings(String currFullText) {
                // remove "\r" and "\n" line returns
        // then replace "¶" with "\n" so number of lines is figured out correctly
        currFullText = currFullText.replace("\r\n", " ");
        currFullText = currFullText.replace("\r", " ");
        currFullText = currFullText.replace("\n", " ");
        currFullText = currFullText.replace("¶", "\n");

        // wait up to 5 sec to make sure lines are populated
        int numLines = textArea.getLines();
        for (int q = 0; q < 100 && numLines == 0; q++) {
            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
            pause(50);

            numLines = textArea.getLines();
            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

            // reassemble lines to make sure we got everything
            String check = "";
            for (String str : textArea.getLineStrings()) {
                check += str;
            }

            if (check.equals(currFullText)) {
                Gdx.app.log(TAG, "Got all line strings");
                break;
            }
        }

        pause(100);
        return textArea.getLineStrings();
    }

    public ConversationChoice getChoice() { return this.choice; }

    public void setConversationGraph(ConversationGraph graph){
        //if( graph != null ) graph.removeAllObservers();
        this.graph = graph;
    }

    public void clear() {
        choice = null;
    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

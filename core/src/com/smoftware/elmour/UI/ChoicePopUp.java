package com.smoftware.elmour.UI;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.dialog.ConversationChoice;
import com.smoftware.elmour.dialog.ConversationGraph;

/**
 * Created by steve on 12/8/17.
 */

public class ChoicePopUp extends Window {
    private static final String TAG = ChoicePopUp.class.getSimpleName();

    private MyTextArea textArea;
    private ConversationGraph graph;
    ConversationChoice choice;

    public ChoicePopUp() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "default");

        textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);

        this.textArea.addListener(new ClickListener() {
              @Override
              public void clicked (InputEvent event, float x, float y) {
                  graph.notify(graph, choice.getConversationCommandEvent());
                  graph.notify(choice.getDestinationId());
              }
          }
        );
    }

    public void setChoice(ConversationChoice choice) {
        this.choice = choice;
        textArea.setText(choice.getChoicePhrase(), true);
    }

    public void setConversationGraph(ConversationGraph graph){
        //if( graph != null ) graph.removeAllObservers();
        this.graph = graph;
    }
}

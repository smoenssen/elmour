package com.smoftware.elmour.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 12/9/17.
 */

public class ConversationLabel extends Window {

    private Label label;

    public ConversationLabel() {
        super("", Utility.ELMOUR_UI_SKIN, "default");

        label = new Label("", Utility.ELMOUR_UI_SKIN);
        label.setAlignment(Align.center, Align.top);
        label.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(label);
    }

    public void setText(String text) {
        label.setText(text);
    }
}

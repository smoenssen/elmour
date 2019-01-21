package com.smoftware.elmour.dialog;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 12/9/17.
 */

public class PopUpLabel extends Window {

    private Label label;

    public PopUpLabel() {
        super("", Utility.ELMOUR_UI_SKIN, "default");

        label = new Label("", Utility.ELMOUR_UI_SKIN, "label_tab");
        label.setAlignment(Align.bottom, Align.bottom);
        label.layout();

        //layout
        ///this.add(label);
        this.defaults().expand().fill();
        this.add(label);

        //this.debug();
        this.pack();
    }

    public void setText(String text) {
        label.setText(text);
    }
}

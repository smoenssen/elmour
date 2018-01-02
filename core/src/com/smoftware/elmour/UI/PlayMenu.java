package com.smoftware.elmour.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 12/31/17.
 */

public class PlayMenu extends Window {
    private static final String TAG = PlayMenu.class.getSimpleName();

    private TextButton partyButton;
    private TextButton inventoryButton;
    private TextButton optionsButton;
    private TextButton saveButton;

    public PlayMenu() {
        //Notes:
        //font is set in the Utility class
        //popup is created in PlayerHUD class
        //textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
        super("", Utility.ELMOUR_UI_SKIN, "default");

        Stack table = new Stack();
        table.setFillParent(true);

        partyButton = new TextButton("Party", Utility.ELMOUR_UI_SKIN);
        inventoryButton = new TextButton("Inventory", Utility.ELMOUR_UI_SKIN);
        optionsButton = new TextButton("Options", Utility.ELMOUR_UI_SKIN);
        saveButton = new TextButton("Save", Utility.ELMOUR_UI_SKIN);


        //table.add(partyButton);
        //table.add(inventoryButton);
        //table.add(optionsButton);
        //table.add(saveButton);

        partyButton.setPosition(0, 0);
        inventoryButton.setPosition(0, 20);

        //layout
        //this.add();
        //this.add(_closeButton);
        //this.row();

        //this.defaults().expand().fill();
        //this.add(partyButton);
        //this.add(inventoryButton);

        //this.debug();
        //this.pack();
    }

    public TextButton getPartyButton() { return partyButton; }
}

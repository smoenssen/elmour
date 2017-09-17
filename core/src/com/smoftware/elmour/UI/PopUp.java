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

    String text;
    TextArea textArea;

    public PopUp() {
        super("", Utility.ELMOUR_UI_SKIN, "default");

        float signHeight;
        float padding;
        String text = "This is me testing the popup This is me testing the popup This is me testing the popup This is me testing the popup";
        textArea = new TextArea("", Utility.ELMOUR_UI_SKIN);

        /*
        if (Gdx.app.getType() == Application.ApplicationType.Android)  {
            signHeight = 180;
            padding = 64;
        }
        else {
            signHeight = 148;
            padding = 48;
        }
*/
        //float signWidth = Gdx.graphics.getWidth()- (padding * 2);

        //textArea.setX(padding);
        //srm top of screen: textArea.setY(Gdx.graphics.getHeight() - padding - signHeight);
        //textArea.setY(padding);
        //textArea.setWidth(signWidth);
        //textArea.setHeight(signHeight);
        //textArea.setAlignment(Align.center);
        textArea.layout();

        //layout
        this.add();
        this.defaults().expand().fill();
        this.add(textArea);
        //this.row();
        //this.add(scrollPane).pad(10,10,10,10);

        //this.debug();
        //this.pack();
    }

    public void loadTextForInteraction(Entity.Interaction interaction) {
        String filePath = "RPGGame/text/" + interaction.toString() + ".txt";
        boolean doesProfileFileExist = Gdx.files.internal(filePath).exists();

        if( !Gdx.files.local(filePath).exists() ){
            text = "Oops!";
        }
        else {
            FileHandle file = Gdx.files.internal(filePath);
            text = file.readString();
            Gdx.app.debug("tag", "file text = " + text);
        }

        textArea.setText(text);
    }
}

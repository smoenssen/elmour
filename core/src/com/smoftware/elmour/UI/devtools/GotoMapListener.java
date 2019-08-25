package com.smoftware.elmour.UI.devtools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.maps.MapFactory;
import com.smoftware.elmour.maps.MapManager;


/**
 * Created by steve on 8/24/19.
 */

public class GotoMapListener implements Input.TextInputListener {
    private static final String TAG = GotoMapListener.class.getSimpleName();

    private Stage stage;
    private MapManager mapMgr;
    private MapFactory.MapType mapType;

    public GotoMapListener(Stage stage, MapManager mapMgr) {
        this.stage = stage;
        this.mapMgr = mapMgr;
    }

    @Override
    public void input (String text) {
        Gdx.app.log(TAG, "input = " + text);

        boolean validInput = false;

        if (text.equals("")) {
            displayErrorDialog("Map name cannot be null!");
        } else {
            try {
                mapType = MapFactory.MapType.valueOf(text.toUpperCase());
                validInput = true;
            } catch (Exception ex) {
                displayErrorDialog("\"" + text + "\"" + " is not a valid map!");
            }
        }

        if (validInput) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    // Load map on the main thread
                    mapMgr.loadMap(mapType);
                }
            });
        }
    }

    @Override
    public void canceled () {
    }

    private void displayErrorDialog(String text) {
        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, "message_box");

        final Dialog dialog = new Dialog("", Utility.ELMOUR_UI_SKIN, "message_box"){
            @Override
            public float getPrefWidth() {
                // force dialog width
                return stage.getWidth() / 1.1f;
            }

            @Override
            public float getPrefHeight() {
                // force dialog height
                return 125f;
            }
        };
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);

        btnOK.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dialog.cancel();
                dialog.hide();
                return true;
            }
        });

        float btnHeight = 30f;
        float btnWidth = 100f;
        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label1 = new Label(text, Utility.ELMOUR_UI_SKIN, "message_box");
        label1.setAlignment(Align.center);
        dialog.getContentTable().add(label1).padTop(5f);

        t.add(btnOK).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, 25);

        dialog.setName("errorDialog");
        stage.addActor(dialog);
    }
}

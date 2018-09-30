package com.smoftware.elmour.UI;

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
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 9/30/18.
 */

public class AdjustInventoryInputListener implements Input.TextInputListener {

    private enum InputState { GET_QUANTITY, GET_ITEM, DONE, CANCEL }
    private InputState state = InputState.GET_QUANTITY;
    private Stage stage;

    public AdjustInventoryInputListener() {
    }

    public AdjustInventoryInputListener(Stage stage) {
        this.stage = stage;
    }

    public AdjustInventoryInputListener(Stage stage, InputState state) {
        this.stage = stage;
        this.state = state;
    }

    @Override
    public void input (String text) {
        Gdx.app.log("tag", "state = " + state.toString());

        switch(state) {
            case GET_QUANTITY:
                if (text.equals("")) {
                    displayErrorDialog();
                }
                    else {
                    Gdx.app.log("tag", "Input: " + text);
                    AdjustInventoryInputListener listener = new AdjustInventoryInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Dialog Title 2", "", "");
                }
                break;
            case GET_ITEM:
                Gdx.app.log("tag", "Input: " + text);
                state = InputState.DONE;
                break;
            case DONE:
                Gdx.app.log("tag", "Done");
                break;
            case CANCEL:
                state = InputState.GET_QUANTITY;
                break;
        }
    }

    @Override
    public void canceled () {
        state = InputState.GET_QUANTITY;
    }

    private void displayErrorDialog() {
        TextButton btnYes = new TextButton("Yes", Utility.ELMOUR_UI_SKIN, "message_box");
        TextButton btnNo = new TextButton("No", Utility.ELMOUR_UI_SKIN, "message_box");

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

        btnYes.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                dialog.cancel();
                dialog.hide();
                //todo: is this necessary?
                //dialog.remove();
                return true;
            }
        });

        btnNo.addListener(new InputListener() {
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

        Label label1 = new Label("What the?", Utility.ELMOUR_UI_SKIN, "message_box");
        label1.setAlignment(Align.center);
        dialog.getContentTable().add(label1).padTop(5f);

        t.add(btnYes).width(btnWidth).height(btnHeight);
        t.add(btnNo).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, 25);

        dialog.setName("confirmDialog");
        stage.addActor(dialog);

    }
}

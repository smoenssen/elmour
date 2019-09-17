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
import com.smoftware.elmour.inventory.KeyItem;
import com.smoftware.elmour.inventory.KeyItemFactory;
import com.smoftware.elmour.inventory.PartyKeys;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.profile.ProfileManager;

public class AdjustKeyItemsInputListener implements Input.TextInputListener {
    private static final String TAG = AdjustInventoryInputListener.class.getSimpleName();

    private enum InputState { GET_QUANTITY, GET_ITEM, DONE }
    private InputState state = InputState.GET_QUANTITY;
    private Stage stage;
    private static int quantity;

    public AdjustKeyItemsInputListener(Stage stage) {
        this.stage = stage;
    }

    public AdjustKeyItemsInputListener(Stage stage, InputState state) {
        this.stage = stage;
        this.state = state;
    }

    @Override
    public void input(String text) {
        Gdx.app.log(TAG, "state = " + state.toString());
        Gdx.app.log(TAG, "input = " + text);

        boolean validInput = false;

        switch(state) {
            case GET_QUANTITY:
                if (text.equals("")) {
                    displayErrorDialog("Quantity cannot be null!");
                }
                else {
                    try {
                        quantity = Integer.parseInt(text);
                        validInput = true;
                    }
                    catch (NumberFormatException ex) {
                        displayErrorDialog("\"" + text + "\"" + " is not a valid integer!");
                    }
                }

                if (validInput) {
                    AdjustKeyItemsInputListener listener = new AdjustKeyItemsInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID", "", "");
                }
                else {
                    AdjustKeyItemsInputListener listener = new AdjustKeyItemsInputListener(stage, InputState.GET_QUANTITY);
                    Gdx.input.getTextInput(listener, "Enter Quantity", "", "");
                }

                break;

            case GET_ITEM:
                KeyItem.ID keyItemId = null;

                if (text.equals("")) {
                    displayErrorDialog("Key item ID cannot be null!");
                }
                else {
                    text = text.toUpperCase();

                    try {
                        keyItemId = KeyItem.ID.valueOf(text);
                        validInput = true;

                        KeyItem keyItem = KeyItemFactory.getInstance().getKeyItem(keyItemId);

                        if (keyItemId == null) {
                            // this should never happen since error condition would be caught in above exception handler
                            displayErrorDialog("\"" + text + "\"" + " is not a valid key item ID!");
                        } else {
                            // add quantity of this item to the key items
                            Gdx.app.log(TAG, String.format("Adding (%d) %s to key items", quantity, keyItemId.toString()));

                            if (quantity > 0) {
                                PartyKeys.getInstance().addItem(keyItem, quantity, true);
                            }
                            else if (quantity < 0) {
                                quantity *= -1;
                                PartyKeys.getInstance().removeItem(keyItem, quantity, true);
                            }
                            else {
                                return;
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        displayErrorDialog("\"" + text + "\"" + " is not a valid key item ID!");
                    }
                }

                if (validInput) {
                    ProfileManager.getInstance().setProperty(PartyKeys.getInstance().PROPERTY_NAME,
                            PartyKeys.getInstance().getKeyItemsProfileString());
                }
                else {
                    AdjustKeyItemsInputListener listener = new AdjustKeyItemsInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID", "", "");
                }

                if (validInput) {
                    state = InputState.DONE;
                }
                else {
                    AdjustKeyItemsInputListener listener = new AdjustKeyItemsInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID", "", "");
                }


                break;
            case DONE:
                Gdx.app.log(TAG, "Done");
                break;
        }
    }

    @Override
    public void canceled() {

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

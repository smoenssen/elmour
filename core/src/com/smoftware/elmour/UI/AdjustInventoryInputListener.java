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
import com.smoftware.elmour.InventoryElement;
import com.smoftware.elmour.InventoryElementFactory;
import com.smoftware.elmour.PartyInventory;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.Hashtable;
import java.util.Set;

/**
 * Created by steve on 9/30/18.
 */

public class AdjustInventoryInputListener implements Input.TextInputListener {

    private static final String TAG = AdjustInventoryInputListener.class.getSimpleName();

    private enum InputState { GET_QUANTITY, GET_ITEM, DONE }
    private InputState state = InputState.GET_QUANTITY;
    private Stage stage;
    private static int quantity;
    //private static

    public AdjustInventoryInputListener(Stage stage) {
        this.stage = stage;
    }

    public AdjustInventoryInputListener(Stage stage, InputState state) {
        this.stage = stage;
        this.state = state;
    }

    @Override
    public void input (String text) {
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
                    AdjustInventoryInputListener listener = new AdjustInventoryInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID or ALL", "", "");
                }
                else {
                    AdjustInventoryInputListener listener = new AdjustInventoryInputListener(stage, InputState.GET_QUANTITY);
                    Gdx.input.getTextInput(listener, "Enter Quantity", "", "");
                }

                break;

            case GET_ITEM:
                InventoryElement.ElementID elementID = null;

                if (text.equals("")) {
                    displayErrorDialog("Item ID cannot be null!");
                }
                else {
                    text = text.toUpperCase();
                    if (text.equals("ALL")) {
                        validInput = true;
                        String profileString = "";

                        // add quantity of ALL items to the inventory
                        Gdx.app.log(TAG, String.format("Adding (%d) of ALL items to inventory", quantity));

                        Hashtable<InventoryElement.ElementID, InventoryElement> inventoryList = InventoryElementFactory.getInstance().getInventoryList();
                        Hashtable<InventoryElement.ElementID, InventoryElement> equipmentList = InventoryElementFactory.getInstance().getEquipmentList();

                        Set<InventoryElement.ElementID> inventoryKeys = inventoryList.keySet();
                        for(InventoryElement.ElementID key: inventoryKeys){
                            InventoryElement element = inventoryList.get(key);
                            // todo?
                            //InventoryElement element = InventoryElementFactory.getInstance().getInventoryElement(key);
                            PartyInventory.getInstance().addItem(element, quantity, true);
                        }

                        Set<InventoryElement.ElementID> equipmentKeys = equipmentList.keySet();
                        for(InventoryElement.ElementID key: equipmentKeys){
                            InventoryElement element = equipmentList.get(key);
                            // todo?
                            //InventoryElement element = InventoryElementFactory.getInstance().getInventoryElement(key);
                            PartyInventory.getInstance().addItem(element, quantity, true);
                        }
                    }
                    else {
                        try {
                            elementID = InventoryElement.ElementID.valueOf(text);
                            validInput = true;

                            InventoryElement element = InventoryElementFactory.getInstance().getInventoryElement(elementID);

                            if (element == null) {
                                // this should never happen since error condition would be caught in above exception handler
                                displayErrorDialog("\"" + text + "\"" + " is not a valid inventory item ID!");
                            } else {
                                // add quantity of this item to the inventory
                                Gdx.app.log(TAG, String.format("Adding (%d) %s to inventory", quantity, elementID.toString()));

                                PartyInventory.getInstance().addItem(element, quantity, true);
                            }
                        } catch (IllegalArgumentException ex) {
                            displayErrorDialog("\"" + text + "\"" + " is not a valid inventory item ID!");
                        }
                    }

                    if (validInput) {
                        String test = PartyInventory.getInstance().getInventoryProfileString();
                        ProfileManager.getInstance().setProperty(PartyInventory.getInstance().PROPERTY_NAME,
                                PartyInventory.getInstance().getInventoryProfileString());
                    }
                    else {
                        AdjustInventoryInputListener listener = new AdjustInventoryInputListener(stage, InputState.GET_ITEM);
                        Gdx.input.getTextInput(listener, "Enter Item ID or ALL", "", "");
                    }
                }

                if (validInput) {
                    state = InputState.DONE;
                }
                else {
                    AdjustInventoryInputListener listener = new AdjustInventoryInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID or ALL", "", "");
                }


                break;
            case DONE:
                Gdx.app.log(TAG, "Done");
                break;
        }
    }

    @Override
    public void canceled () {
        state = InputState.GET_QUANTITY;
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

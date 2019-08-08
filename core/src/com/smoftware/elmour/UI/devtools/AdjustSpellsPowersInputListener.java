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
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.entities.EntityConfig;
import com.smoftware.elmour.entities.EntityFactory;
import com.smoftware.elmour.inventory.PartyInventory;
import com.smoftware.elmour.inventory.SpellPowerElementFactory;
import com.smoftware.elmour.inventory.SpellPowerElement;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.Hashtable;
import java.util.Set;

public class AdjustSpellsPowersInputListener implements Input.TextInputListener {

    private static final String TAG = AdjustInventoryInputListener.class.getSimpleName();

    private enum InputState {GET_CHARACTER, GET_ITEM, DONE }
    private InputState state = InputState.GET_CHARACTER;
    private Stage stage;
    private static EntityFactory.EntityName character;

    public AdjustSpellsPowersInputListener(Stage stage) {
        this.stage = stage;
    }

    public AdjustSpellsPowersInputListener(Stage stage, InputState state) {
        this.stage = stage;
        this.state = state;
    }

    @Override
    public void input (String text) {
        Gdx.app.log(TAG, "state = " + state.toString());
        Gdx.app.log(TAG, "input = " + text);

        boolean validInput = false;

        switch(state) {
            case GET_CHARACTER:
                if (text.equals("")) {
                    displayErrorDialog("Character cannot be null!");
                }
                else {
                    try {
                        character = EntityFactory.EntityName.valueOf(text.toUpperCase());
                        validInput = true;
                    }
                    catch (Exception ex) {
                        displayErrorDialog("\"" + text + "\"" + " is not a valid character!");
                    }
                }

                if (validInput) {
                    AdjustSpellsPowersInputListener listener = new AdjustSpellsPowersInputListener(stage, InputState.GET_ITEM);
                    Gdx.input.getTextInput(listener, "Enter Item ID or ALL", "", "");
                }
                else {
                    AdjustSpellsPowersInputListener listener = new AdjustSpellsPowersInputListener(stage, InputState.GET_CHARACTER);
                    Gdx.input.getTextInput(listener, "Enter Character", "", "");
                }

                break;

            case GET_ITEM:
                SpellPowerElement.ElementID elementID = null;

                if (text.equals("")) {
                    displayErrorDialog("Item ID cannot be null!");
                }
                else {
                    text = text.toUpperCase();
                    if (text.equals("ALL")) {
                        validInput = true;

                        Hashtable<SpellPowerElement.ElementID, SpellPowerElement> spellList = SpellPowerElementFactory.getInstance().getSpellPowerList();

                        Array<SpellPowerElement.ElementID> array = new Array<>();
                        String propertyKey = character.toString() + EntityConfig.EntityProperties.SPELL_LIST.toString();

                        Set<SpellPowerElement.ElementID> spellKeys = spellList.keySet();
                        for(SpellPowerElement.ElementID key: spellKeys){
                            SpellPowerElement element = spellList.get(key);
                            array.add(element.id);
                        }

                        ProfileManager.getInstance().setProperty(propertyKey, array);
                    }
                    else {
                        try {
                            elementID = SpellPowerElement.ElementID.valueOf(text);
                            validInput = true;

                            if (elementID == null) {
                                // this should never happen since error condition would be caught in above exception handler
                                displayErrorDialog("\"" + text + "\"" + " is not a valid spell/power item ID!");
                            } else {
                                String propertyKey = character.toString().toUpperCase() + EntityConfig.EntityProperties.SPELL_LIST.toString();
                                Array<SpellPowerElement.ElementID> array = ProfileManager.getInstance().getProperty(propertyKey, Array.class);

                                if (array == null) {
                                    array = new Array<>();
                                    array.add(elementID);
                                    ProfileManager.getInstance().setProperty(propertyKey, array);
                                }
                                else if (!array.contains(elementID, true)) {
                                    array.add(elementID);
                                    ProfileManager.getInstance().setProperty(propertyKey, array);
                                }
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
                        AdjustSpellsPowersInputListener listener = new AdjustSpellsPowersInputListener(stage, InputState.GET_ITEM);
                        Gdx.input.getTextInput(listener, "Enter Item ID or ALL", "", "");
                    }
                }

                if (validInput) {
                    state = InputState.DONE;
                }
                else {
                    AdjustSpellsPowersInputListener listener = new AdjustSpellsPowersInputListener(stage, InputState.GET_ITEM);
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
        state = InputState.GET_CHARACTER;
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


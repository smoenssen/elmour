package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.smoftware.elmour.ElmourGame;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by steve on 6/30/19.
 */

public class AdjustStatsUI {
    private static final String TAG = AdjustStatsUI.class.getSimpleName();

    ElmourGame game;
    Stage stage;

    MyTextField inputFieldCharID;
    MyTextField inputFieldHP;
    MyTextField inputFieldHP_MAX;
    MyTextField inputFieldMP;
    MyTextField inputFieldMP_MAX;
    MyTextField inputFieldATK;
    MyTextField inputFieldMATK;
    MyTextField inputFieldDEF;
    MyTextField inputFieldMDEF;
    MyTextField inputFieldSPD;
    MyTextField inputFieldACC;
    MyTextField inputFieldLCK;
    MyTextField inputFieldAVO;
    MyTextField inputFieldLVL;
    MyTextField inputFieldDIBS;
    MyTextField inputFieldChapter;

    Entity entity = null;

    public AdjustStatsUI(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;

        // All of this is needed in order to get a blinking cursor
        MyTextField.TextFieldStyle tStyle = new MyTextField.TextFieldStyle();
        if (ElmourGame.isAndroid()) {
            tStyle.font = Utility.ELMOUR_UI_SKIN.getFont("myFontSuperSmall");
        }
        else {
            tStyle.font = Utility.ELMOUR_UI_SKIN.getFont("myFont");
        }
        tStyle.fontColor = Color.BLACK;
        tStyle.background = Utility.ELMOUR_UI_SKIN.getDrawable("textbutton");
        tStyle.cursor = Utility.ELMOUR_UI_SKIN.newDrawable("cursor", Color.BLACK);
        tStyle.cursor.setMinWidth(3.5f);
        tStyle.selection = Utility.ELMOUR_UI_SKIN.newDrawable("textbutton", 0.5f, 0.5f, 0.5f, 0.5f);

        inputFieldCharID = new MyTextField("", tStyle);
        inputFieldHP = new MyTextField("", tStyle);
        inputFieldHP_MAX = new MyTextField("", tStyle);
        inputFieldMP = new MyTextField("", tStyle);
        inputFieldMP_MAX = new MyTextField("", tStyle);
        inputFieldATK = new MyTextField("", tStyle);
        inputFieldMATK = new MyTextField("", tStyle);
        inputFieldDEF = new MyTextField("", tStyle);
        inputFieldMDEF = new MyTextField("", tStyle);
        inputFieldSPD = new MyTextField("", tStyle);
        inputFieldACC = new MyTextField("", tStyle);
        inputFieldLCK = new MyTextField("", tStyle);
        inputFieldAVO = new MyTextField("", tStyle);
        inputFieldLVL = new MyTextField("", tStyle);
        inputFieldDIBS = new MyTextField("", tStyle);
        inputFieldChapter = new MyTextField("", tStyle);
    }

    public void requestInput() {
        final String labelText = "Character ID:";
        final String labelHP = "HP = ";
        final String labelHP_MAX = "HP_MAX = ";
        final String labelMP = "MP = ";
        final String labelMP_MAX = "MP_MAX = ";
        final String labelATK = "ATK = ";
        final String labelMATK = "MATK = ";
        final String labelDEF = "DEF = ";
        final String labelMDEF = "MDEF = ";
        final String labelSPD = "SPD = ";
        final String labelACC = "ACC = ";
        final String labelLCK = "LCK = ";
        final String labelAVO = "AVO = ";
        final String labelLVL = "LVL = ";
        final String labelDIBS = "Party Dibs = ";
        final String labelChapter = "Chapter = ";

        String styleName;

        if (ElmourGame.isAndroid()) {
            styleName = "message_box_very_small";
        }
        else {
            styleName = "message_box";
        }

        TextButton btnGetStats = new TextButton("Get Stats", Utility.ELMOUR_UI_SKIN, styleName);
        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, styleName);
        TextButton btnApply = new TextButton("Apply", Utility.ELMOUR_UI_SKIN, styleName);
        TextButton btnCancel = new TextButton("Cancel", Utility.ELMOUR_UI_SKIN, styleName);

        final Dialog dialog = new Dialog("", Utility.ELMOUR_UI_SKIN, "message_box"){
            @Override
            public float getPrefWidth() {
                // force dialog width
                return stage.getWidth();
            }

            @Override
            public float getPrefHeight() {
                // force dialog height
                return stage.getHeight();
            }
        };
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);

        btnOK.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                apply();
                dialog.cancel();
                dialog.hide();
                clearStats();
                return true;
            }
        });

        btnApply.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                apply();
                return true;
            }
        });

        btnCancel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dialog.cancel();
                dialog.hide();
                clearStats();
                return true;
            }
        });

        btnGetStats.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                String input = inputFieldCharID.getText().toUpperCase();
                getCharacterStats(input);
                Gdx.input.setOnscreenKeyboardVisible(false);
                return true;
            }
        });

        float btnHeight;
        float btnWidth;
        float topPad;
        float fieldWidth;
        float charIdInputFieldWidth;
        float buttonPadTop;
        float buttonPadBottom;

        if (ElmourGame.isAndroid()) {
            btnHeight = 20f;
            btnWidth = 50f;
            topPad = 2f;
            fieldWidth = 65;
            charIdInputFieldWidth = 75;
            buttonPadTop = 5;
            buttonPadBottom = 2;
        }
        else {
            btnHeight = 30f;
            btnWidth = 100f;
            topPad = 5f;
            fieldWidth = 125;
            charIdInputFieldWidth = 150;
            buttonPadTop = 0;
            buttonPadBottom = 10;
        }

        Table t = new Table();
        if (ElmourGame.isAndroid()) {
            t.row().pad(1, 1, 0, 1);
        }
        else {
            t.row().pad(5, 5, 0, 5);
        }
        // t.debug();

        Label label1 = new Label(labelText, Utility.ELMOUR_UI_SKIN, styleName);
        dialog.getContentTable().add(label1).padTop(topPad);
        dialog.getContentTable().add(inputFieldCharID).width(charIdInputFieldWidth).padTop(topPad);
        dialog.getContentTable().add(btnGetStats).width(charIdInputFieldWidth).padTop(topPad).colspan(2);

        dialog.getContentTable().row();
        addField(dialog, labelHP, inputFieldHP, topPad, fieldWidth);
        addField(dialog, labelHP_MAX, inputFieldHP_MAX, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelMP, inputFieldMP, topPad, fieldWidth);
        addField(dialog, labelMP_MAX, inputFieldMP_MAX, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelATK, inputFieldATK, topPad, fieldWidth);
        addField(dialog, labelMATK, inputFieldMATK, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelDEF, inputFieldDEF, topPad, fieldWidth);
        addField(dialog, labelMDEF, inputFieldMDEF, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelSPD, inputFieldSPD, topPad, fieldWidth);
        addField(dialog, labelACC, inputFieldACC, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelLCK, inputFieldLCK, topPad, fieldWidth);
        addField(dialog, labelAVO, inputFieldAVO, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelLVL, inputFieldLVL, topPad, fieldWidth);

        dialog.getContentTable().row();
        addField(dialog, labelChapter, inputFieldChapter, topPad, fieldWidth);
        addField(dialog, labelDIBS, inputFieldDIBS, topPad, fieldWidth);

        //dialog.getContentTable().debugAll();

        t.add(btnOK).width(btnWidth).height(btnHeight);
        t.add(btnApply).width(btnWidth).height(btnHeight);
        t.add(btnCancel).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().setHeight(btnHeight);
        dialog.getButtonTable().add(t).center().padBottom(buttonPadBottom).padTop(buttonPadTop);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("inputDialog");
        stage.addActor(dialog);
        stage.setKeyboardFocus(inputFieldCharID);

        if (Gdx.input.getInputProcessor() == null) {
            Gdx.app.log(TAG, "Setting input processor to PlayerHUD stage in requestInput()");
            Gdx.input.setInputProcessor(stage);
        }

        stage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
                return true;
            }
        });
    }

    private void apply() {
        String characterName = inputFieldCharID.getText();
        characterName = characterName.trim().toUpperCase();
        boolean validCharacterName = true;

        if (characterName == null || characterName.isEmpty()) {
            invalidInputMessage(characterName + "Character ID is empty");
        }
        else {
            try {
                entity = EntityFactory.getInstance().getEntityByName(characterName);
                entity.setBattleEntityType(Entity.BattleEntityType.PARTY);
            } catch (NullPointerException ex) {
                invalidInputMessage(characterName + " is not a valid character ID");
                validCharacterName = false;
            }
        }

        if (validCharacterName) {
            String message = validateInput();
            if (message.isEmpty()) {
                setCharacterStats();
            }
            else {
                invalidInputMessage(message);
            }
        }
    }

    private void addField(Dialog dialog, String text, MyTextField inputField, float topPad, float fieldWidth) {
        Label label;

        if (ElmourGame.isAndroid()) {
            label = new Label(text, Utility.ELMOUR_UI_SKIN, "message_box_very_small");
        }
        else {
            label = new Label(text, Utility.ELMOUR_UI_SKIN, "message_box");
        }

        dialog.getContentTable().add(label).padTop(topPad).align(Align.right);
        dialog.getContentTable().add(inputField).width(fieldWidth).padTop(topPad).align(Align.left);
    }

    private void invalidInputMessage(String message) {
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

        float btnHeight;
        float btnWidth;

        if (ElmourGame.isAndroid()) {
            btnHeight = 15f;
            btnWidth = 50f;
        }
        else {
            btnHeight = 30f;
            btnWidth = 100f;
        }

        Table t = new Table();
        t.row().pad(5, 5, 0, 5);
        // t.debug();

        Label label;

        if (ElmourGame.isAndroid()) {
            label = new Label(message, Utility.ELMOUR_UI_SKIN, "message_box_very_small");
        }
        else {
            label = new Label(message, Utility.ELMOUR_UI_SKIN, "message_box");
        }

        dialog.getContentTable().add(label).padTop(5f);

        t.add(btnOK).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().add(t).center().padBottom(10f);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("invalidCharsDialog");
        stage.addActor(dialog);
    }

    private void getCharacterStats(String characterName) {
        if (characterName == null || characterName.isEmpty()) {
            invalidInputMessage(characterName + "Character ID is empty");
        }
        else {
            try {
                entity = EntityFactory.getInstance().getEntityByName(characterName);
                entity.setBattleEntityType(Entity.BattleEntityType.PARTY);

                int value = game.statusUI.getHPValue(entity);
                inputFieldHP.setText(String.format("%d", value), true);
                value = game.statusUI.getHPMaxValue(entity);
                inputFieldHP_MAX.setText(String.format("%d", value), true);
                value = game.statusUI.getMPValue(entity);
                inputFieldMP.setText(String.format("%d", value), true);
                value = game.statusUI.getMPMaxValue(entity);
                inputFieldMP_MAX.setText(String.format("%d", value), true);
                value = game.statusUI.getATKValue(entity);
                inputFieldATK.setText(String.format("%d", value), true);
                value = game.statusUI.getMATKValue(entity);
                inputFieldMATK.setText(String.format("%d", value), true);
                value = game.statusUI.getDEFValue(entity);
                inputFieldDEF.setText(String.format("%d", value), true);
                value = game.statusUI.getMDEFValue(entity);
                inputFieldMDEF.setText(String.format("%d", value), true);
                value = game.statusUI.getSPDValue(entity);
                inputFieldSPD.setText(String.format("%d", value), true);
                value = game.statusUI.getACCValue(entity);
                inputFieldACC.setText(String.format("%d", value), true);
                value = game.statusUI.getLCKValue(entity);
                inputFieldLCK.setText(String.format("%d", value), true);
                value = game.statusUI.getAVOValue(entity);
                inputFieldAVO.setText(String.format("%d", value), true);
                value = 0; // TODO: game.statusUI.getLevelValue(entity);
                inputFieldLVL.setText(String.format("%d", value), true);
                value = game.statusUI.getDibsValue();
                inputFieldDIBS.setText(String.format("%d", value), true);

                Integer currentChapter = ProfileManager.getInstance().getProperty("currentChapter", Integer.class);
                inputFieldChapter.setText(String.format("%d", currentChapter), true);

            } catch (NullPointerException ex) {
                invalidInputMessage(characterName + " is not a valid character ID");
            }
        }
    }

    private void setCharacterStats() {
        if (entity != null) {
            String value = "";
            int iVal = 0;

            value = inputFieldHP.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setHPValue(entity, iVal);
            value = inputFieldHP_MAX.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setHPMaxValue(entity, iVal);
            value = inputFieldMP.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setMPValue(entity, iVal);
            value = inputFieldMP_MAX.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setMPMaxValue(entity, iVal);
            value = inputFieldATK.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setATKValue(entity, iVal);
            value = inputFieldMATK.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setMATKValue(entity, iVal);
            value = inputFieldDEF.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setDEFValue(entity, iVal);
            value = inputFieldMDEF.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setMDEFValue(entity, iVal);
            value = inputFieldSPD.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setSPDValue(entity, iVal);
            value = inputFieldACC.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setACCValue(entity, iVal);
            value = inputFieldLCK.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setLCKValue(entity, iVal);
            value = inputFieldAVO.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setAVOValue(entity, iVal);
            value = inputFieldLVL.getText();
            iVal = Integer.parseInt(value);
            //todo: game.statusUI.setValue(entity, iVal);
            value = inputFieldDIBS.getText();
            iVal = Integer.parseInt(value);
            game.statusUI.setDibsValue(iVal);
            value = inputFieldChapter.getText();
            iVal = Integer.parseInt(value);
            ProfileManager.getInstance().setProperty("currentChapter", iVal);
        }
    }

    private void clearStats() {
        inputFieldHP.setText("", true);
        inputFieldHP_MAX.setText("", true);
        inputFieldMP.setText("", true);
        inputFieldMP_MAX.setText("", true);
        inputFieldATK.setText("", true);
        inputFieldMATK.setText("", true);
        inputFieldDEF.setText("", true);
        inputFieldMDEF.setText("", true);
        inputFieldSPD.setText("", true);
        inputFieldACC.setText("", true);
        inputFieldLCK.setText("", true);
        inputFieldAVO.setText("", true);
        inputFieldLVL.setText("", true);
        inputFieldDIBS.setText("", true);
        inputFieldChapter.setText("", true);

        entity = null;
    }

    private String validateInput() {
        String results = "";
        String value = "";

        try {
            value = inputFieldHP.getText();
            Integer.parseInt(value);
            value = inputFieldHP_MAX.getText();
            Integer.parseInt(value);
            value = inputFieldMP.getText();
            Integer.parseInt(value);
            value = inputFieldMP_MAX.getText();
            Integer.parseInt(value);
            value = inputFieldATK.getText();
            Integer.parseInt(value);
            value = inputFieldMATK.getText();
            Integer.parseInt(value);
            value = inputFieldDEF.getText();
            Integer.parseInt(value);
            value = inputFieldMDEF.getText();
            Integer.parseInt(value);
            value = inputFieldSPD.getText();
            Integer.parseInt(value);
            value = inputFieldACC.getText();
            Integer.parseInt(value);
            value = inputFieldLCK.getText();
            Integer.parseInt(value);
            value = inputFieldAVO.getText();
            Integer.parseInt(value);
            value = inputFieldLVL.getText();
            Integer.parseInt(value);
            value = inputFieldDIBS.getText();
            Integer.parseInt(value);
            value = inputFieldChapter.getText();
            Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            results = value + " is not an integer";
        }

        return results;
    }
}

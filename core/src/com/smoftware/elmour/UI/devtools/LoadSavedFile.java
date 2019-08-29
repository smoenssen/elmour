package com.smoftware.elmour.UI.devtools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.UI.components.MyTextArea;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Utility;
import com.smoftware.elmour.profile.ProfileManager;
import com.smoftware.elmour.screens.StartScreen;

public class LoadSavedFile {
    private static final String TAG = LoadSavedFile.class.getSimpleName();

    ElmourGame game;
    Stage stage;
    StartScreen startScreen;
    SelectBox fileList;

    private Table savedFilesTableView;

    public LoadSavedFile(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
        fileList = new SelectBox(Utility.ELMOUR_UI_SKIN);
    }

    public void requestInput(final StartScreen startScreen) {
        this.startScreen = startScreen;
        final String labelText = "Select File:";
        String styleName;

        if (ElmourGame.isAndroid()) {
            styleName = "message_box_very_small";
        }
        else {
            styleName = "message_box";
        }

        TextButton btnOK = new TextButton("OK", Utility.ELMOUR_UI_SKIN, styleName);
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
                String file = (String)fileList.getSelected();
                String profile = file.substring(0, file.indexOf(".sav"));
                //ProfileManager.getInstance().loadProfile(profile);
                startScreen.continueGameFromSaveFile(profile);
                dialog.remove();
                return true;
            }
        });

        btnCancel.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dialog.remove();
                return true;
            }
        });

        float btnHeight;
        float btnWidth;
        float topPad;
        float buttonPadTop;
        float buttonPadBottom;

        if (ElmourGame.isAndroid()) {
            btnHeight = 20f;
            btnWidth = 50f;
            topPad = 2f;
            buttonPadTop = 5;
            buttonPadBottom = 2;
        }
        else {
            btnHeight = 30f;
            btnWidth = 100f;
            topPad = 5f;
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

        Array<String> filesArray = new Array<>();

        // add files here
        FileHandle dirHandle;
        dirHandle = Gdx.files.local("");

        for (FileHandle entry: dirHandle.list()) {
            String fileName  = entry.name();
            if (fileName.contains(".sav")) {
                filesArray.add(fileName);
            }
        }

        fileList.setItems(filesArray);
        dialog.getContentTable().add(fileList);

        //dialog.getContentTable().debugAll();

        t.add(btnOK).width(btnWidth).height(btnHeight);
        t.add(btnCancel).width(btnWidth).height(btnHeight);

        dialog.getButtonTable().setHeight(btnHeight);
        dialog.getButtonTable().add(t).center().padBottom(buttonPadBottom).padTop(buttonPadTop);
        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, stage.getHeight() - dialog.getHeight() - 7);

        dialog.setName("inputDialog");
        stage.addActor(dialog);

        if (ElmourGame.isAndroid()) {
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
    }
}

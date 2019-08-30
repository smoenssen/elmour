package com.smoftware.elmour.UI.devtools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.main.Utility;

public class ManageSavedFiles {
    private static final String TAG = ManageSavedFiles.class.getSimpleName();

    ElmourGame game;
    Stage stage;
    String styleName;

    public ManageSavedFiles(final ElmourGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
    }

    public void show() {

        if (ElmourGame.isAndroid()) {
            styleName = "message_box_very_small";
        }
        else {
            styleName = "message_box";
        }

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

        TextButton btnClose = new TextButton("Close", Utility.ELMOUR_UI_SKIN, styleName);
        btnClose.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dialog.remove();
                return true;
            }
        });

        Table table = new Table();
        buildTable(table);

        dialog.getButtonTable().add(btnClose).width(100).padTop(5).colspan(2);

        ScrollPane scrollPane = new ScrollPane(table);
        dialog.getContentTable().add(scrollPane);

        dialog.show(stage).setPosition(stage.getWidth() / 2 - dialog.getWidth() / 2, stage.getHeight() - dialog.getHeight() - 7);
    }

    private void buildTable(Table table) {
        // add files here
        FileHandle dirHandle;
        dirHandle = Gdx.files.local("");

        for (FileHandle entry: dirHandle.list()) {
            String fileName  = entry.name();
            if (fileName.contains(".sav") && !fileName.equalsIgnoreCase("new_game.sav")) {
                addField(table, fileName);
                table.row();
            }
        }
    }

    private void addField(final Table table, String text) {
        int pad;
        if (ElmourGame.isAndroid()) {
            pad = 10;
        }
        else {
            pad = 15;
        }

        Label label = new Label(text, Utility.ELMOUR_UI_SKIN, styleName);
        table.add(label).pad(pad).align(Align.right);

        final TextButton btnDelete = new TextButton("Delete", Utility.ELMOUR_UI_SKIN, styleName);
        btnDelete.setUserObject(label);
        table.add(btnDelete);

        btnDelete.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Label label = (Label) btnDelete.getUserObject();
                deleteFile(label.getText().toString());
                table.reset();
                buildTable(table);
                return true;
            }
        });
    }

    private void deleteFile(String fileName) {
        FileHandle dirHandle;
        dirHandle = Gdx.files.local("");

        for (FileHandle entry: dirHandle.list()) {
            if (fileName.equals(entry.name())) {
                entry.delete();
            }
        }
    }
}

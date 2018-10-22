package com.smoftware.elmour.UI;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by steve on 10/7/18.
 */

public class MyActions {

    // Custom Actions
    public class setTextAreaText extends Action {
        MyTextField textArea = null;
        String text = "";

        public setTextAreaText(MyTextField textArea, String text) {
            this.textArea = textArea;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            textArea.setText(text, true);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonText extends Action {
        TextButton btn = null;
        String text = "";

        public setButtonText(TextButton btn, String text) {
            this.btn = btn;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            btn.setText(text);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonHeight extends Action {
        TextButton btn = null;
        float height = 0;

        public setButtonHeight(TextButton btn, float height) {
            this.btn = btn;
            this.height = height;
        }

        @Override
        public boolean act (float delta) {
            btn.setHeight(height);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonWidth extends Action {
        TextButton btn = null;
        float width = 0;

        public setButtonWidth(TextButton btn, float width) {
            this.btn = btn;
            this.width = width;
        }

        @Override
        public boolean act (float delta) {
            btn.setWidth(width);
            return true; // An action returns true when it's completed
        }
    }

    public class setButtonVisible extends Action {
        TextButton button = null;
        boolean visible = false;

        public setButtonVisible(TextButton button, boolean visible) {
            this.button = button;
            this.visible = visible;
        }

        @Override
        public boolean act (float delta) {
            button.setVisible(visible);
            return true; // An action returns true when it's completed
        }
    }

    public class setLabelText extends Action {
        Label label = null;
        String text = "";

        public setLabelText(Label label, String text) {
            this.label = label;
            this.text = text;
        }

        @Override
        public boolean act (float delta) {
            label.setText(text);
            return true; // An action returns true when it's completed
        }
    }

    public class setTextAreaVisible extends Action {
        MyTextField textArea = null;
        boolean visible = false;

        public setTextAreaVisible(MyTextField textArea, boolean visible) {
            this.textArea = textArea;
            this.visible = visible;
        }

        @Override
        public boolean act (float delta) {
            textArea.setVisible(visible);
            return true; // An action returns true when it's completed
        }
    }

    public class setTableVisible extends Action {
        Table table = null;
        boolean visible = false;

        public setTableVisible(Table table, boolean visible) {
            this.table = table;
            this.visible = visible;
        }

        @Override
        public boolean act (float delta) {
            table.setVisible(visible);
            return true; // An action returns true when it's completed
        }
    }

    public class enabledScrollPane extends Action {
        ScrollPane pane = null;
        boolean enable = false;

        public enabledScrollPane(ScrollPane pane, boolean enable) {
            this.pane = pane;
            this.enable = enable;
        }

        @Override
        public boolean act (float delta) {
            if (enable)
                pane.setTouchable(Touchable.enabled);
            else
                pane.setTouchable(Touchable.disabled);

            return true; // An action returns true when it's completed
        }
    }

    public class closeBattleTextAction extends Action {
        BattleTextArea battleTextArea = null;

        public closeBattleTextAction(BattleTextArea battleTextArea) {
            this.battleTextArea = battleTextArea;
        }

        @Override
        public boolean act (float delta) {
            if (battleTextArea.isVisible()) {
                battleTextArea.hide();
            }
            return true; // An action returns true when it's completed
        }
    }

    public class setTextFieldPositionAndSize extends Action {
        MyTextField textField = null;
        float width;
        float height;
        float x;
        float y;

        public setTextFieldPositionAndSize(MyTextField textField, float x, float y, float width, float height) {
            this.textField = textField;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean act (float delta) {
            textField.setX(x);
            textField.setY(y);
            textField.setWidth(width);
            textField.setHeight(height);
            return true; // An action returns true when it's completed
        }
    }

    public class setCameraPosition extends Action {
        OrthographicCamera camera;
        float x;
        float y;

        public setCameraPosition(OrthographicCamera camera, float x, float y) {
            this.camera = camera;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean act (float delta) {
            this.camera.position.set(x, y, 0f);
            return true; // An action returns true when it's completed
        }
    }
}

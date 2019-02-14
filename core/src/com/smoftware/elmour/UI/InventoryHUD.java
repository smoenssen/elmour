package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smoftware.elmour.Utility;

/**
 * Created by steve on 2/10/19.
 */

public class InventoryHUD implements Screen, InventoryHudSubject {
    private static final String TAG = InventoryHUD.class.getSimpleName();

    private final String BTN_NAME_EQUIPMENT = "Equipment";
    private final String BTN_NAME_CONSUMABLES = "Consumables";
    private final String BTN_EXIT = "Exit";

    private Stage stage;
    private Array<InventoryHudObserver> observers;

    private Table mainTable;

    private TextButton equipmentButton;
    private TextButton consumablesButton;
    private TextButton exitButton;

    public InventoryHUD(Stage stage) {

        this.stage = stage;
        observers = new Array<>();

        mainTable = new Table();

        equipmentButton = new TextButton(BTN_NAME_EQUIPMENT, Utility.ELMOUR_UI_SKIN, "battle");
        consumablesButton = new TextButton(BTN_NAME_CONSUMABLES, Utility.ELMOUR_UI_SKIN, "battle");
        exitButton = new TextButton(BTN_EXIT, Utility.ELMOUR_UI_SKIN, "battle");

        //equipmentButton.setWidth(200);
        //equipmentButton.setHeight(50);
        //equipmentButton.setPosition(0, 0);
        //equipmentButton.setVisible(true);

        mainTable.row().width(stage.getWidth()).height(50);
        mainTable.add(equipmentButton).pad(-2).width(stage.getWidth() / 3);
        mainTable.add(consumablesButton).pad(-2).width(stage.getWidth() / 3);
        mainTable.add(exitButton).pad(-2).width(stage.getWidth() / 3);

        mainTable.pack();

        mainTable.setPosition(6, 6);

        equipmentButton.addListener(new ClickListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        Gdx.app.log(TAG, "equipmentButton up");
                                        if (touchPointIsInButton(equipmentButton)) {

                                        }
                                    }
                                }
        );

        exitButton.addListener(new ClickListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            Gdx.app.log(TAG, "exitButton up");
                                            if (touchPointIsInButton(equipmentButton)) {
                                                hide();
                                            }
                                        }
                                    }
        );
    }

    private boolean touchPointIsInButton(TextButton button) {
        // Get touch point
        Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Convert the touch point into local coordinates
        Vector2 localPos = new Vector2(screenPos);
        localPos = stage.screenToStageCoordinates(localPos);

        Rectangle buttonRect = new Rectangle(button.getX(), button.getY(), button.getWidth(), button.getHeight());

        return Utility.pointInRectangle(buttonRect, localPos.x, localPos.y);
    }

    @Override
    public void show() {
        stage.addActor(mainTable);
        notify(InventoryHudObserver.InventoryHudEvent.INVENTORY_HUD_SHOWN);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        mainTable.remove();
        notify(InventoryHudObserver.InventoryHudEvent.INVENTORY_HUD_HIDDEN);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void addObserver(InventoryHudObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(InventoryHudObserver observer) {
        observers.removeValue(observer, true);
    }

    @Override
    public void notify(InventoryHudObserver.InventoryHudEvent event) {
        for(InventoryHudObserver observer: observers){
            observer.onNotify(event);
        }
    }
}

package com.smoftware.elmour.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityFactory;
import com.smoftware.elmour.GraphicsComponent;
import com.smoftware.elmour.maps.Map;

import java.util.Hashtable;
import java.util.concurrent.Semaphore;

public class StatusArrows {
    private static final String TAG = StatusArrows.class.getSimpleName();

    private Stage stage;
    private Array<ArrowItem> currentList;
    private float frameTime;
    private Vector2 arrowGroupPosition;
    Semaphore listMutex = new Semaphore(1, true);

    class ArrowItem {
        public EntityFactory.EntityName name;
        public Animation<TextureRegion> animation;
        public TextureRegion currentFrame;

        public ArrowItem(EntityFactory.EntityName name, Animation<TextureRegion> animation) {
            this.name = name;
            this.animation = animation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArrowItem that = (ArrowItem) o;
            return name.equals(that.name);
        }
    }

    public StatusArrows(Stage stage) {
        this.stage = stage;
        currentList = new Array<>();
        frameTime = 0;
        arrowGroupPosition = new Vector2(0, 0);
    }

    public void setPosition(float x, float y) {
        arrowGroupPosition.x = x;
        arrowGroupPosition.y = y;
    }

    public void add(EntityFactory.EntityName name) {
        boolean inList = false;
        for (int i = 0; i < currentList.size; i++) {
            ArrowItem item = currentList.get(i);
            if (item.name.equals(name)) {
                inList = true;
                break;
            }
        }

        if (!inList) {
            Hashtable<Entity.AnimationType, Animation<TextureRegion>> animations;
            animations = GraphicsComponent.loadAnimationsByName(name);
            ArrowItem newItem = new ArrowItem(name, animations.get(Entity.AnimationType.STAT_ARROW));

            if (newItem != null) {
                try {
                    listMutex.acquire(1);
                    currentList.add(newItem);
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Exception caught in add(): " + e.toString());
                } finally {
                    listMutex.release(1);
                }
            }
        }
    }

    public void remove(EntityFactory.EntityName name) {
        for (int i = 0; i < currentList.size; i++) {
            ArrowItem item = currentList.get(i);
            if (item.name.equals(name)) {
                try {
                    listMutex.acquire(1);
                    currentList.removeIndex(i);
                }
                catch (Exception e) {
                    Gdx.app.error(TAG, "Exception caught in remove(): " + e.toString());
                }
                finally {
                    listMutex.release(1);
                }

                break;
            }
        }
    }

    public void clear() {
        try {
            listMutex.acquire(1);
            currentList.clear();
        }
        catch (Exception e) {
            Gdx.app.error(TAG, "Exception caught in clear(): " + e.toString());
        }
        finally {
            listMutex.release(1);
        }
    }

    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        frameTime = (frameTime + delta) % 5;

        renderer.getBatch().begin();

        for (ArrowItem arrow : currentList) {
            arrow.currentFrame = arrow.animation.getKeyFrame(frameTime, true);

            if (arrow.currentFrame != null) {
                //draw animation
                float regionWidth = arrow.currentFrame.getRegionWidth() * Map.UNIT_SCALE;
                float regionHeight = arrow.currentFrame.getRegionHeight() * Map.UNIT_SCALE;

                renderer.getBatch().draw(arrow.currentFrame, arrowGroupPosition.x, arrowGroupPosition.y, regionWidth, regionHeight);
            }
        }

        renderer.getBatch().end();
    }
}

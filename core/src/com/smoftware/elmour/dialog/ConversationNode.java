package com.smoftware.elmour.dialog;

import com.badlogic.gdx.utils.Array;

/**
 * Created by moenssr on 12/6/2017.
 */

public class ConversationNode {
    public enum NodeType {
        NPC,
        CMD,
        CHOICE
    }

    public String id;
    public NodeType type;
    public String data;
    public Array<String> previous;
    public Array<String> next;

    public ConversationNode() {
        previous = new Array<>();
        next = new Array<>();
    }
}

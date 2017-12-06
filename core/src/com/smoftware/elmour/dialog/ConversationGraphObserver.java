package com.smoftware.elmour.dialog;

public interface ConversationGraphObserver {
    public static enum ConversationCommandEvent {
        LOAD_STORE_INVENTORY,
        EXIT_CONVERSATION,
        ACCEPT_QUEST,
        ADD_ENTITY_TO_INVENTORY,
        RETURN_QUEST,
        NO_CHOICE,
        SET_C1_CH1,
        NONE
    }

    void onNotify(final ConversationGraph graph, ConversationCommandEvent event);
}

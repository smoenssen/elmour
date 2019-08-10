package com.smoftware.elmour.profile;

public interface ProfileObserver {
    public static enum ProfileEvent{
        PROFILE_LOADED,
        SAVING_PROFILE,
        CLEAR_CURRENT_PROFILE,
        SAVED_PROFILE
    }

    void onNotify(final ProfileManager profileManager, ProfileEvent event);
}
